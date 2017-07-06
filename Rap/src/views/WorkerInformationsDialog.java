package views;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import database.LinkConnector;
import database.Worker;

public class WorkerInformationsDialog extends Shell {
	
	private Worker worker;
	
	public WorkerInformationsDialog(Shell parent, Worker worker){
		super(parent, SWT.DIALOG_TRIM);
		this.worker = worker;
		this.setText("Информация о работнике");
		this.setLayout(new GridLayout(1, false));
		setContent();
		int width = 300;
		int height = 200;
		Rectangle displayBounds = getDisplay().getBounds();
		setBounds(displayBounds.width/2 - width/2, displayBounds.height/2 - height/2,width,height);
	}
	
	private void setContent(){
		Composite pane = new Composite(this, SWT.NONE);
		pane.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
		pane.setLayout(new GridLayout(2,false));
		
		Label nameLabel = new Label(pane, SWT.NONE);
		nameLabel.setText(worker.getName());
		nameLabel.setLayoutData(new GridData(SWT.CENTER,SWT.CENTER,true,true,2,1));
		
		Label loginLabel = new Label(pane, SWT.NONE);
		loginLabel.setText("Логин: "+worker.getLogin());
		loginLabel.setLayoutData(new GridData(SWT.LEFT,SWT.CENTER,true,true,2,1));
		
		Label flagLabel = new Label(pane, SWT.NONE);
		flagLabel.setText("Состояние: "+Worker.Flags.toString(worker.getFlag()));
		flagLabel.setLayoutData(new GridData(SWT.LEFT,SWT.CENTER,true,true,2,1));
		
		Label adminLabel = new Label(pane, SWT.NONE);
		adminLabel.setText("Является админом: ");
		adminLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,true,true,1,1));
		
		Composite buttonPane = new Composite(pane, SWT.NONE);
		buttonPane.setLayout(new GridLayout(2,true));
		buttonPane.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true,1,1));

		int currentWorkerID = Integer.valueOf(RWT.getSettingStore().getAttribute("userID"));
		
		Button yesButton = new Button(buttonPane,SWT.RADIO);
		yesButton.setText("Да");
		if(worker.getId() == currentWorkerID)
			yesButton.setEnabled(false);
		yesButton.setLayoutData(new GridData(SWT.LEFT,SWT.CENTER,true,true));
		yesButton.addListener(SWT.MouseUp, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				LinkConnector.updateWorkerAdmin(worker.getId(), true);
				
			}
		});
		
		
		Button noButton = new Button(buttonPane,SWT.RADIO);
		noButton.setText("Нет");
		if(worker.getId() == currentWorkerID)
			noButton.setEnabled(false);
		noButton.setLayoutData(new GridData(SWT.LEFT,SWT.CENTER,true,true));
		noButton.addListener(SWT.MouseUp, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				LinkConnector.updateWorkerAdmin(worker.getId(), false);
				
			}
		});
		
		Button closeButton = new Button(pane, SWT.PUSH);
		closeButton.setText("Закрыть");
		closeButton.setLayoutData(new GridData(SWT.CENTER,SWT.CENTER,true,true,2,1));
		closeButton.addListener(SWT.MouseUp, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				close();
				pane.dispose();
				
			}
		});
		
		yesButton.setSelection(worker.isAdmin());
		noButton.setSelection(!worker.isAdmin());
	}

}
