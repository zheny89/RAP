package database;

import java.sql.*;

import org.apache.derby.jdbc.EmbeddedDriver;

import exception.IdleUpdateException;
import exception.LoginMismatchException;
import exception.PasswordMismatchException;

public class DBConnector {

	private boolean debugMode = true;
	
	public final static String EMBEDDED_DERBY_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
	public final static String DERBY_PROTOCOL = "jdbc:derby:";

	private String driver;
	private String protocol;
	private String dbName;

	private Connection connection;
	
	/**
	 * Создаёт объект коннектора к указанной базе данных и регистрирует встроенный драйвер
	 * @param driverName название (встроенного) драйвера базы данных
	 * @param protocol протокол подключения к базе данных
	 * @param databaseName название базы данных
	 * @throws SQLException 
	 */
	public DBConnector(String driverName, String protocol, String databaseName) 
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException 
	{
		this.driver = driverName;
		this.protocol = protocol;
		this.dbName = databaseName;
		registerDerbyDriverInstance();
	}
	
	/** Регистрирует встроенный драйвер базы данных 
	 * @throws SQLException */
	public void registerDerbyDriverInstance()
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException 
	{
		DriverManager.registerDriver((Driver) Class.forName(driver).newInstance());
		if (debugMode)
			System.out.println("Registered database driver instance");
	}
	
	/** Осуществляет подключение к базе данных */
	public void connect() throws SQLException 
	{
		connection = DriverManager.getConnection(protocol + dbName + ";create=true");
		if (debugMode)
			System.out.println("Connected to / created database " + dbName);
		if (isDatabaseEmpty()) {
			createTables();
			if (debugMode)
				System.out.println("Created tables");
		}
	}

	/**
	 * Проверка, не закрыто ли соединение с базой данных
	 * @return false, если соединение закрыто, иначе true
	 */
	public boolean isConnected() {
		try {
			return !connection.isClosed();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/** Закрывает соединение с базой данных */
	public void close() {
		try {
			if (isConnected())
				connection.close();
			if (debugMode)
				System.out.println("Disconnected from database");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Проверяет, есть ли в базе таблицы, выводит их в консоль отладки
	 * @return true если база не пуста, иначе false
	 */
	public boolean isDatabaseEmpty() {
		boolean result = true;
		try {
			ResultSet res = connection.getMetaData().getTables(null, null, null, new String[]{"TABLE"});
			if (debugMode)
				System.out.println("List of tables: ");
			while (res.next()) {
				result = false;
				if (debugMode)
					System.out.println(res.getString(3));
			}
			res.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	private void createTables() throws SQLException {
		try {
			Statement statement = connection.createStatement();
			System.out.println("Creating Table Users");
			statement.executeUpdate(SQLStatements.CREATE_TABLE_USERS);
			System.out.println("Creating Table Worktime");
			statement.executeUpdate(SQLStatements.CREATE_TABLE_WORKTIME);
			System.out.println("Creating Table Messages");
			statement.executeUpdate(SQLStatements.CREATE_TABLE_MESSAGES);
		} catch (SQLException sqle) {
			connection.rollback();
			throw sqle;
		}
		connection.commit();
	}
	
	public void clear() throws SQLException {
		String[] sqls = { "DROP TABLE USERS", "DROP TABLE WORKTIME", "DROP TABLE MESSAGES" };
		for (String sql : sqls)
			try {
				connection.createStatement().executeUpdate(sql);
				if (debugMode)
					System.out.println(sql);
			} catch (SQLException sqle) {
				System.out.println(sqle.getSQLState());
				// 42Y55 - STATEMENT cannot be performed on TABLE because it does not exist
				if (sqle.getSQLState() != "42Y55") {
					connection.rollback();
					throw sqle;
				}
			}
	}

	public int addUser(String login, byte[] pswdHash, String name, boolean isAdmin) {
		final String sql = "INSERT INTO USERS(login, password_hash, privilege, name, flag, flag_start_date) VALUES(?, ?, ?, ?, ?, ?)";
		try {
			final PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			preparedStatement.setString(1, login);
			preparedStatement.setBytes(2, pswdHash);
			preparedStatement.setShort(3, (isAdmin) ? (short)1 : (short)0);
			preparedStatement.setString(4, (name != null) ? name : login);
			preparedStatement.setShort(5, Types.SMALLINT);
			preparedStatement.setNull(6, Types.DATE);
			preparedStatement.executeUpdate();
			ResultSet rs = preparedStatement.getGeneratedKeys();
            if (rs.next()) {
            	connection.commit();
            	return rs.getInt(1);
            }
            else return 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public User getUser(String login) {
		User result = null;
		final String sql = "SELECT * FROM USERS WHERE login = \'" + login + "\'";
		try {
			ResultSet rs = connection.createStatement().executeQuery(sql);
			if (rs.next()) {
				result = new User();
				result.setId(rs.getInt("user_id"));
				result.setLogin(rs.getString("login"));
				result.setPswdHash(rs.getBytes("password_hash"));
				result.setName(rs.getString("name"));
				result.setAdmin(rs.getShort("privilege") != 0);
				result.setFlag(rs.getShort("flag"), rs.getDate("flag_start_date"));
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Удаляет запись пользователя из базы
	 * @param id user id
	 * @throws SQLException произошла ошибка базы данных во время выполнения запроса
	 * @throws IdleUpdateException запрос не привел к изменению базы
	 */
	public void removeUser(int id) throws SQLException, IdleUpdateException {
		String sql = "DELETE FROM USERS WHERE user_id = " + id;
		performUpdate(sql);
	}
	
	/**
	 * Отмечает присутсвие пользователя на работе (8)
	 * @param id user id
	 * @param login имя пользователя
	 * @param paswdHash хеш пароля
	 * @throws PasswordMismatchException 
	 * @throws LoginMismatchException 
	 */
	public boolean logUserIn(int id, String login, byte[] pswdHash) throws PasswordMismatchException, LoginMismatchException {
		User user = getUser(login);
		if (user == null)
			throw new LoginMismatchException();
		if (!user.assertPswdHash(pswdHash))
			throw new PasswordMismatchException();
		String sql = "INSERT INTO WORKTIME(day, user_id, worktime_hours) VALUES(?, ?, ?, ?)";
		try {
			final PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setDate(1, new Date(new java.util.Date().getTime()));
			preparedStatement.setInt(2, id);
			preparedStatement.setShort(3, (short)8);
			preparedStatement.executeUpdate();
			connection.commit();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Добавляет сообщение от работника администратору
	 * @param fromId id отправителя
	 * @param message текст сообщения
	 * @return id записи в БД
	 */
	public int addMessage(int fromId, String message) {
		final String sql = "INSERT INTO MESSAGES(day, sender_id, message, is_read) VALUES(?, ?, ?, ?)";
		try {
			final PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			preparedStatement.setDate(1, new Date(new java.util.Date().getTime()));
			preparedStatement.setInt(2, fromId);
			preparedStatement.setString(3, message);
			preparedStatement.setShort(4, (short)0);
			preparedStatement.executeUpdate();
			ResultSet rs = preparedStatement.getGeneratedKeys();
            if (rs.next()) {
            	connection.commit();
            	return rs.getInt(1);
            }
            else return 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	/**
	 * Изменяет часы работы сотрудника в определенный день
	 * @param id id работника
	 * @param day дата
	 * @param workTime новое время
	 * @return true при успешном изменении, иначе false
	 */
	public boolean updateUserWorktime(int id, java.util.Date day, short workTime) {
		final String sql = "UPDATE WORTIME SET worktime_hours=? WHERE day=? AND user_id=?";
		try {
			final PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setShort(1, workTime);
			preparedStatement.setDate(2, new Date(day.getTime()));
			preparedStatement.setInt(3, id);
			int result = preparedStatement.executeUpdate();
			return result != 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Выставить флаг пользователю (возможно отложенный)
	 * @param id работника
	 * @param flag флаг из User.Flags
	 * @param startDate начало действия флага, null - с текущей даты
	 * @return
	 */
	public boolean updateUserFlag(int id, short flag, java.util.Date startDate) {
		final String sql = "UPDATE USERS SET flag=?, flag_start_time=? WHERE user_id=?";
		try {
			final PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setShort(1, flag);
			preparedStatement.setDate(2, new Date(startDate.getTime()));
			preparedStatement.setInt(3, id);
			int result = preparedStatement.executeUpdate();
			return result != 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean clearUserFlags(int id) {
		return updateUserFlag
	}
	
	private void performUpdate(String sql) throws SQLException, IdleUpdateException {
		int result = connection.createStatement().executeUpdate(sql);
		if (result == 0) throw new IdleUpdateException("No updates where made");
		connection.commit(); 
	}
	
	
}
