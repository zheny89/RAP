package database;

import java.sql.SQLException;
import java.util.List;

public interface IDatabaseConnector {
	
	void connect() throws SQLException;
	boolean isConnected();
	void close();
	boolean isDatabaseEmpty();
	void clear() throws SQLException;
	int addUser(String login, byte[] pswdHash, String name, boolean isAdmin);
	Worker getUser(int id);
	Worker getUser(String login);
	boolean logUserIn(int id);
	int addMessage(int fromId, String message);
	boolean updateWorktime(int userId, java.util.Date day, short workTime);
	boolean updateUserFlag(int id, short flag, java.util.Date startDate);
	boolean clearUserFlags(int id);
	List<Message> getMessages(short messageStatus);
	
}
