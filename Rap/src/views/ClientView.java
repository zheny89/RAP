package views;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import database.LinkConnector;
import exception.EntryNotExistsException;
import rap.BasicEntryPoint;

public class ClientView implements View {
	private Button sendRequestButton,sendVocation;
	private Label statusLabel;
	private Composite clientComposite;
	private int userID;
	
	public ClientView(Composite parent){
		userID = BasicEntryPoint.getUserID();
		clientComposite = new Composite(parent, SWT.BORDER);
		clientComposite.setLayoutData(new GridData(SWT.CENTER,SWT.CENTER,true,true));
		clientComposite.setLayout(new GridLayout(1,true));
		
		statusLabel = new Label(clientComposite,SWT.NONE);
		statusLabel.setText("Вы были отмечены на рабочем месте\nВременная метка: " + RWT.getSettingStore().getAttribute("validDate"));
		
		sendRequestButton = new Button(clientComposite, SWT.PUSH);
		sendRequestButton.setLayoutData(new GridData(SWT.CENTER,SWT.CENTER,true,true));
		sendRequestButton.setText("Отправить запрос");
		sendRequestButton.addListener(SWT.MouseUp, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				// Отправка запроса
				Shell requestShell = getRequestShell(parent.getShell());
				requestShell.setVisible(true);
			}
		});;
		
		sendVocation = new Button(clientComposite, SWT.PUSH);
		sendVocation.setLayoutData(new GridData(SWT.CENTER,SWT.CENTER,true,true));
		sendVocation.setText("Попросить отпуск");
		sendVocation.addListener(SWT.MouseUp, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				// Отправка запроса
				String message = "Хочу отпуск";
				try {
					LinkConnector.addMessage(userID, message);
				} catch (EntryNotExistsException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});;
		clientComposite.pack();
	}
	
	@Override
	public void dispose() {
		clientComposite.dispose();
	}
	
	private Shell getRequestShell (Shell parent){
			int width = 400;
			int height = 400;
			Text requestText;
			Button sendButton;
			Shell shell = new Shell(parent);
			shell.setText("Ваш запрос");
			shell.setBounds(parent.getBounds().width/2-width/2,parent.getBounds().height/2-height/2,width,height);
			shell.setLayout(new GridLayout(1,false));
			
			Composite textCompos = new Composite(shell,SWT.NONE);
			textCompos.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
			requestText = new Text(textCompos, SWT.BORDER|SWT.WRAP);
			
			Composite composite = new Composite(shell, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL,SWT.BOTTOM,true,false));
			composite.setLayout(new GridLayout(1,true));
			
			sendButton = new Button(composite, SWT.PUSH);
			sendButton.setText("Отправить");
			sendButton.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,true));
			sendButton.addListener(SWT.MouseUp, new Listener() {
				
				@Override
				public void handleEvent(Event event) {
					String message = requestText.getText();
					try {
						LinkConnector.addMessage(userID, message);
					} catch (EntryNotExistsException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			
			composite.pack();
			requestText.setSize(width-16,height - composite.getBounds().height*3);
			return shell;
	}
}
