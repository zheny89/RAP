package views;

public interface View {
	
	static interface Id {
		static final int LOGIN_VIEW = 0;
		static final int CLIENT_VIEW = 1;
		static final int ADMIN_VIEW = 2;
		static final int REPORT_VIEW = 3;
		static final int MAIL_VIEW = 4;
		
		static boolean isRestrictedView(int viewId) {
			switch (viewId) {
			case LOGIN_VIEW: return false;
			case CLIENT_VIEW: return false;
			case ADMIN_VIEW: return true;
			case REPORT_VIEW: return true;
			case MAIL_VIEW: return true;
			default: return true;
			}
		}
	}
	
	void dispose();
}
