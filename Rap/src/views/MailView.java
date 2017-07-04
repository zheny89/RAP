package views;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import database.LinkConnector;
import database.Message;
import rap.BasicEntryPoint;

public class MailView implements View {

	BasicEntryPoint enterPoint;
	Label dayLabel, senderLabel, messageLabel;
	Font headerFont, normalBoldFont;
	
	public MailView(BasicEntryPoint enterPoint, Composite parent) {
		this.enterPoint = enterPoint;
		
		Composite paneComposite = new Composite(parent, SWT.BORDER);
		paneComposite.setLayoutData(new GridData(SWT.CENTER,SWT.CENTER,true,true));
		paneComposite.setLayout(new GridLayout(2, false));
		
		Label headerLabel = new Label(paneComposite, SWT.CENTER);
		headerLabel.setText("Сообщения");
		FontData fontData = headerLabel.getFont().getFontData()[0];
		headerFont = new Font(headerLabel.getFont().getDevice(), 
				new FontData(fontData.getName(), fontData.getHeight(), SWT.BOLD));
		headerLabel.setFont(headerFont);
		headerLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 2, 1));
		
		Label headerSeparator = new Label(paneComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		headerSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		ScrolledComposite mailListHolderComposite = new ScrolledComposite(paneComposite, SWT.V_SCROLL);
		mailListHolderComposite.setLayoutData(new GridData(SWT.LEFT, SWT.UP, false, true));
		Composite mailListComposite = new Composite(mailListHolderComposite, SWT.NONE);
		mailListHolderComposite.setContent(mailListComposite);
		
		ScrolledComposite mailDetailHolderComposite = new ScrolledComposite(paneComposite, SWT.V_SCROLL);
		mailDetailHolderComposite.setLayoutData(new GridData(SWT.FILL, SWT.UP, false, true));
		Composite mailDetailComposite = new Composite(mailDetailHolderComposite, SWT.NONE);
		mailDetailHolderComposite.setContent(mailDetailComposite);
		mailDetailComposite.setLayout(new RowLayout(SWT.VERTICAL));
		dayLabel = new Label(mailDetailComposite, SWT.LEFT);
		senderLabel = new Label(mailDetailComposite, SWT.LEFT);
		messageLabel = new Label(mailDetailComposite, SWT.LEFT);
				
		mailListComposite.setLayout(new GridLayout(1, false));
		fillMailList(this, mailListComposite, mailDetailComposite);
		mailListComposite.setSize(mailListComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		Label footerSeparator = new Label(paneComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		footerSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		Button backButton = new Button(paneComposite, SWT.PUSH);
		backButton.setText("Назад");
		backButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		backButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				enterPoint.changeView(View.Id.ADMIN_VIEW);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {/*never called*/}
		});
	}
	
	private void drawMailDetail(Composite mailDetailComposite, Message mail) {
		dayLabel.setText("Дата отправления: " + mail.getDay().toString());
		senderLabel.setText("Отправитель: " + mail.getSender().getName());
		messageLabel.setText("Сообщение:\n" + mail.getMessage());
		mailDetailComposite.setSize(mailDetailComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}
	
	private void fillMailList(MailView parent, Composite mailListComposite, Composite mailDetailComposite) {
		List<Message> unreadMessages = LinkConnector.getMessages(Message.Status.UNREAD);
		List<Message> readMessages = LinkConnector.getMessages(Message.Status.READ);
		for (Message msg : unreadMessages) {
			Button mailButton = new Button(mailListComposite, SWT.PUSH);
			mailButton.setText(msg.getDay().toString() + "\n" + msg.getSender().getName());
			mailButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			mailButton.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					mailButton.setBackground(new Color(null, 255, 255, 255));
					parent.drawMailDetail(mailDetailComposite, msg);
					LinkConnector.updateMessageStatus(msg.getId(), Message.Status.READ);
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {/* never called */}			
			});
		}
		for (Message msg : readMessages) {
			Button mailButton = new Button(mailListComposite, SWT.PUSH);
			mailButton.setText(msg.getDay().toString() + "\n" + msg.getSender().getName());
			mailButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			mailButton.setBackground(new Color(null, 255, 255, 255));
			mailButton.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					parent.drawMailDetail(mailDetailComposite, msg);
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {/* never called */}			
			});
		}
	}
	
	@Override
	public void dispose() {
		headerFont.dispose();
	}

}
