package views;

public interface View {
	
	static interface Id {
		static final int LOGIN_VIEW = 0;
		static final int CLIENT_VIEW = 1;
		static final int ADMIN_VIEW = 2;
		static final int REPORT_VIEW = 3;
		static final int MAIL_VIEW = 4;
	}
	
	void dispose();
}
