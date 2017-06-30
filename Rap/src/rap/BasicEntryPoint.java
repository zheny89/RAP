package rap;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Calendar;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.rap.rwt.client.service.JavaScriptExecutor;
import org.eclipse.rap.rwt.service.SettingStore;
import org.eclipse.swt.widgets.Composite;

import views.AdminView;
import views.ClientView;
import views.LoginView;
import views.ReportView;
import views.View;


public class BasicEntryPoint extends AbstractEntryPoint {
	
	private Composite parent;
	private View currentView;
	private int currentViewID;
	private SettingStore store;
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
		case View.Id.LOGIN_VIEW: viewLogin(); break;
		case View.Id.CLIENT_VIEW: viewReportPanel(); break;
		case View.Id.ADMIN_VIEW: viewReportPanel(); break;
		default: throw new RuntimeException("Неизвестное значение: "+currentViewID);
		}
    }
    
    private void viewLogin(){
    	currentView = new LoginView(parent,this);
    }
    
    private void viewClient(){
    	currentView = new ClientView(parent);
    }
    
    private void viewAdminPanel(){
    	currentView = new AdminView(parent);
    }
    
    private void viewReportPanel(){
    	currentView = new ReportView(parent, LocalDate.of(2017, 5, 1), LocalDate.of(2017, 12, 12));
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
    			System.err.println("Не смог сохранить данные. Идентификация невозможна.");
    		}
    	JavaScriptExecutor jsExecutor = RWT.getClient().getService(JavaScriptExecutor.class);
    	jsExecutor.execute("location.reload();");
    }
    
    public static int getUserID(){
    	return Integer.valueOf(RWT.getSettingStore().getAttribute("userID"));
    }

}
