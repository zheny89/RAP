package rap;

import java.io.IOException;
import java.time.LocalDate;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.rap.rwt.client.service.JavaScriptExecutor;
import org.eclipse.rap.rwt.service.SettingStore;
import org.eclipse.swt.widgets.Composite;

import views.AdminView;
import views.ClientView;
import views.LoginView;
import views.MailView;
import views.ReportView;
import views.View;


public class BasicEntryPoint extends AbstractEntryPoint {
	
	private Composite parent;
	private View currentView;
	private int currentViewID;
	private SettingStore store;
	
	public void storeReportPeriod(LocalDate fromDay, LocalDate toDay) {
		try {
			store.setAttribute("reportStart", fromDay.toString());
			store.setAttribute("reportEnd", toDay.toString());
		} catch (IOException e) {
			System.err.println("Не удалось сохранить даты отчётов.");
		}	
	}
	
    @Override
    protected void createContents(Composite parent) {
        this.parent = parent;
        store = RWT.getSettingStore();
    	currentView = null;
    	currentViewID = getCurrentViewID();
    	setView(currentViewID);
    }
    
    private void setView(int viewID) {
        switch (currentViewID) {
		case View.Id.LOGIN_VIEW: viewLoginPanel(); break;
		case View.Id.CLIENT_VIEW: viewClientPanel(); break;
		case View.Id.ADMIN_VIEW: viewAdminPanel(); break;
		case View.Id.REPORT_VIEW: viewReportPanel(); break;
		case View.Id.MAIL_VIEW: viewMailPanel(); break;
		default: throw new RuntimeException("Неизвестное значение: " + currentViewID);

		}
    }
    
    private void viewLoginPanel(){
    	currentView = new LoginView(parent,this);
    }
    
    private void viewClientPanel(){
    	currentView = new ClientView(parent);
    }
    
    private void viewAdminPanel(){
    	currentView = new AdminView(this, parent);
    }
    
    private void viewReportPanel(){
    	LocalDate fromDay = LocalDate.parse(store.getAttribute("reportStart"));
    	LocalDate toDay = LocalDate.parse(store.getAttribute("reportEnd"));
    	boolean editMode = Boolean.parseBoolean(store.getAttribute("reportEditMode"));
    	currentView = new ReportView(this, parent, fromDay, toDay, editMode);
    }
    
    private void viewMailPanel(){
    	currentView = new MailView(this, parent);
    }
    
    private int getCurrentViewID(){
    	String currentViewString = store.getAttribute("currentView");
    	if(currentViewString != null){
    		String date = store.getAttribute("validDate");
    		if(date.equals(getCurrentDate())) {
    			return Integer.valueOf(currentViewString);
    		}
    	}
    	return 0;
    }
    private String getCurrentDate(){
    	return LocalDate.now().toString(); // yyyy-mm-dd
    }
    
    public void changeView(int viewID){
    	if(currentView != null) currentView.dispose();
            try {
    			store.setAttribute("currentView", String.valueOf(viewID));
    			store.setAttribute("validDate", getCurrentDate());
    		} catch (IOException e) {
    			System.err.println("Не удалось сохранить дату.");
    		}
    	JavaScriptExecutor jsExecutor = RWT.getClient().getService(JavaScriptExecutor.class);
    	jsExecutor.execute("location.reload();");
    }
    
    public static int getUserID(){
    	return Integer.valueOf(RWT.getSettingStore().getAttribute("userID"));
    }

	public void storeEditMode(boolean editMode) {
		try {
			store.setAttribute("reportEditMode", Boolean.toString(editMode));
		} catch (IOException e) {
			System.err.println("Не удалось сохранить настройки режима редактирования.");
		}	
	}

}
