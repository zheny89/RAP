package rap;

import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;


public class BasicEntryPoint extends AbstractEntryPoint {
	private Composite parent;
	private View currentView;
    @Override
    protected void createContents(Composite parent) {
    	currentView = null;
        this.parent = parent;
        viewLogin();
    }
    
    private void viewLogin(){
    	currentView = new LoginView(parent,this);
    }
    
    private void viewClient(){
    	if(currentView != null) currentView.dispose();
    	currentView = new Client(parent);
    }
    
    public void changeView(int viewID){
    	viewClient();
    }

}
