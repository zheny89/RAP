package database;

public interface SQLStatements {
	
	final String CREATE_TABLE_USERS = "CREATE TABLE USERS"
			+ "("
			+ "user_id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
			+ "login VARCHAR(60) UNIQUE NOT NULL,"
			+ "password_hash CHAR(32) FOR BIT DATA,"
			+ "privilege SMALLINT NOT NULL,"
			+ "name VARCHAR(100),"
			+ "flag SMALLINT,"
			+ "flag_start_date DATE,"
			+ "PRIMARY KEY (id, login)"
			+ ")";
	
	final String CREATE_TABLE_WORKTIME = "CREATE TABLE WORKTIME"
			+ "("
			+ "day DATE NOT NULL,"
			+ "user_id INTEGER NOT NULL,"
			+ "worktime_hours SMALLINT NOT NULL,"
			+ "flag SMALLINT NOT NULL,"
			+ "PRIMARY KEY (day, user_id),"
			+ "FOREIGN KEY (user_id) REFERENCES USERS(user_id)"
			+ ")";
	
	final String CREATE_TABLE_MESSAGES = "CREATE TABLE MESSAGES"
			+ "("
			+ "message_id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
			+ "day DATE NOT NULL,"
			+ "sender_id INTEGER NOT NULL,"
			+ "message VARCHAR(1000),"
			+ "is_read SMALLINT NOT NULL,"
			+ "PRIMARY KEY (id),"
			+ "FOREIGN KEY (sender_id) REFERENCES USERS (user_id)"
			+ ")";
}
