package rap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class Client implements View {
	private Button button;
	
	
	public Client(Composite parent){
		button = new Button(parent, SWT.PUSH);
		button.setLayoutData(new GridData(SWT.CENTER,SWT.CENTER,true,true));
		button.setText("HELLO");
		button.setVisible(true);
	}
	
	@Override
	public void dispose() {
		button.dispose();
		
	}
}
