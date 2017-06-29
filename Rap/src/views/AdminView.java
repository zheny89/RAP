package views;

import org.eclipse.rap.rwt.client.service.ExitConfirmation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

public class AdminView implements View {
	private Composite adminComposite,leftComposite,rightComposite;
	private Button reportsButton,changeBaseButton,messageButton;
	private Label titleLabel;
	private int userID;
	
	
	public AdminView(Composite parent){
		adminComposite = new Composite(parent, SWT.BORDER);
		adminComposite.setLayoutData(new GridData(SWT.CENTER,SWT.CENTER,true,true));
		adminComposite.setLayout(new GridLayout(2, true));
		
		leftComposite = new Composite(adminComposite, SWT.NONE);
		leftComposite.setLayout(new GridLayout(2,true));
		
		reportsButton = new Button(leftComposite, SWT.PUSH);
		reportsButton.setText("Отчеты");
		reportsButton.setLayoutData(new GridData(SWT.CENTER,SWT.CENTER,true,true,1,1));
		
		messageButton = new Button(leftComposite, SWT.PUSH);
		messageButton.setText("10");
		messageButton.setLayoutData(new GridData(SWT.CENTER,SWT.CENTER,true,true,1,2));
		
		changeBaseButton = new Button(leftComposite, SWT.PUSH);
		changeBaseButton.setText("Редактирование базы");
		changeBaseButton.setLayoutData(new GridData(SWT.CENTER,SWT.CENTER,true,true,1,1));
		
		
		rightComposite = new Composite(adminComposite, SWT.BORDER);
		rightComposite.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,true));
		rightComposite.setLayout(new GridLayout(1, true));
		
		for(int i = 0; i < 10; i++){
			Composite compos = new Composite(rightComposite, SWT.NONE);
			compos.setLayout(new GridLayout(2,false));
			Label imageLabel = new Label(compos,SWT.NO_BACKGROUND);
			imageLabel.setLayoutData(new GridData(SWT.CENTER,SWT.CENTER,true,true));
			imageLabel.setText("ONLINE");
			Button button = new Button(compos, SWT.PUSH);
			GridData data = new GridData(SWT.FILL,SWT.CENTER,true,true);
			button.setLayoutData(data);
			button.setText("Василченко Василий Васильевич 27.06.17");
		}
	}
	
	@Override
	public void dispose() {
		adminComposite.dispose();
		
	}
}
