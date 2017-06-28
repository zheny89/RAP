package rap;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

public class ClientView implements View {
	private Button sendRequestButton,sendVocation;
	private Label statusLabel;
	private Composite clientComposite;
	
	public ClientView(Composite parent){
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
				
			}
		});;
		
		sendVocation = new Button(clientComposite, SWT.PUSH);
		sendVocation.setLayoutData(new GridData(SWT.CENTER,SWT.CENTER,true,true));
		sendVocation.setText("Попросить отпуск");
		sendVocation.addListener(SWT.MouseUp, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				// Отправка запроса
				
			}
		});;
		clientComposite.pack();
	}
	
	@Override
	public void dispose() {
		clientComposite.dispose();
	}
}
