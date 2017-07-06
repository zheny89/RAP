package views;

import java.io.IOException;

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import database.LinkConnector;
import database.Worker;
import exception.EntryAlreadyExistsException;
import exception.EntryNotExistsException;
import ldap.LdapAuthentication;
import rap.BasicEntryPoint;

public class LoginView implements View {
	private Button loginButton;
	private Label loginLabel,pswdLabel,titleLabel, subtitleLabel;
	private Text loginText,pswdText;
	private Composite loginComposite;
	private BasicEntryPoint enterPoint;
	private String titleString = "Вход в систему";
	private String subtitleString = "";
	private String loginString = "Логин";
	private String pswdString = "Пароль";
	private String buttonString = "Войти";
	
	public LoginView(Composite parent, BasicEntryPoint enterPoint) {
		this.enterPoint = enterPoint;
		parent.setLayout(new GridLayout());
		loginComposite = new Composite(parent,SWT.BORDER);
		GridData compositeData = new GridData(SWT.CENTER,SWT.CENTER,true,true);
		compositeData.widthHint = 250;
		compositeData.heightHint = 190;
		loginComposite.setLayoutData(compositeData);
		GridLayout gridLay = new GridLayout(2,false);
		loginComposite.setLayout(gridLay);
		
		
		titleLabel = new Label(loginComposite,SWT.NONE);
		titleLabel.setLayoutData(new GridData(SWT.CENTER,SWT.CENTER,true,true,2,1));
		titleLabel.setText(titleString);
		
		subtitleLabel = new Label(loginComposite,SWT.NONE);
		subtitleLabel.setLayoutData(new GridData(SWT.CENTER,SWT.CENTER,true,true,2,1));
		subtitleLabel.setText(subtitleString);
		subtitleLabel.setVisible(false);
		
		loginLabel = new Label(loginComposite,SWT.NONE);
		loginLabel.setText(loginString);
		
		loginText = new Text(loginComposite, SWT.BORDER);
		loginText.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,true));
		
		pswdLabel = new Label(loginComposite,SWT.NONE);
		pswdLabel.setText(pswdString);
		
		pswdText = new Text(loginComposite,SWT.BORDER|SWT.PASSWORD);
		pswdText.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,true));
		pswdText.addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR){
					login();
				}
				
			}
		});
		
		loginButton = new Button(loginComposite, SWT.PUSH);
		loginButton.setText(buttonString);
		loginButton.setLayoutData(new GridData(SWT.CENTER,SWT.CENTER,true,true,2,1));
		loginButton.addListener(SWT.MouseUp, new Listener() {
			@Override
			public void handleEvent(Event event) {
				login();
			}
		});
	}
	
	private void login(){
		String pswd = pswdText.getText();
		String login = loginText.getText();
		boolean authSucc = authenticateWorker(login, pswd);
		if (!authSucc) {
			subtitleLabel.setText(subtitleString);
			subtitleLabel.setVisible(true);
			loginComposite.pack();
			loginComposite.requestLayout();
		}
	}
	
	@Override
	public void dispose() {
		loginComposite.dispose();
	}
	
	private boolean authenticateWorker(String login, String pswd) {
		LdapContext connection = authByLdap(login, pswd);
		if (connection == null) {
			subtitleString = "Неверный логин или пароль";
			return false;
		}
		LdapAuthentication.getUsersAttribute(login, connection);
		String name = LdapAuthentication.getCommonName();
		Worker worker = LinkConnector.getWorker(login);
		if (worker == null) {
			createWorkerInDatabase(login, name);
			worker = LinkConnector.getWorker(login);
			if (worker == null) {
				subtitleString = "Unknown error";
				return false;
			}
		}
		// отмечаем работника, если что обновляем имя
		try {
			LinkConnector.logWorkerIn(worker.getId());
			if (!worker.getName().equals(name))
				LinkConnector.updateWorkerName(worker.getId(), name);
		} catch (EntryNotExistsException e1) {
			e1.printStackTrace(); // should never fire
			subtitleString = "Unknown error";
			return false;
		}
		try {
			RWT.getSettingStore().setAttribute("userID", String.valueOf(worker.getId()));
			if (worker.isAdmin())
				enterPoint.changeView(View.Id.ADMIN_VIEW);
			else enterPoint.changeView(View.Id.CLIENT_VIEW);
		} catch (IOException e) {
			subtitleString = "ID storing error";
			return false;
		}
		LdapAuthentication.closeLdapConnection();
		return true;
	}

	private void createWorkerInDatabase(String login, String name) {
		try {
			boolean isAdmin = LinkConnector.getWorkersCount() == 0;
			LinkConnector.addWorker(login, name, isAdmin);
		} catch (EntryAlreadyExistsException e) {
			e.printStackTrace(); // should never fire
		}
	}

	private LdapContext authByLdap(String login, String pswd) {
		if (login == null || pswd == null || login.isEmpty() || pswd.isEmpty())
			return null;
		try {
			return LdapAuthentication.getConnection(login, pswd);
		} catch (NamingException e) {
			System.err.println(e.getMessage());
			subtitleString = "Неверный логин или пароль";
			return null;
		}
	}
}
