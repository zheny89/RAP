package views;

import java.io.IOException;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import database.IDatabaseConnector;
import database.User;
import rap.BasicEntryPoint;

public class LoginView implements View {
	private Button loginButton;
	private Label loginLabel,pswdLabel,titleLabel;
	private Text loginText,pswdText;
	private Composite loginComposite;
	private BasicEntryPoint enterPoint;
	private String titleString = "Вход в систему";
	private String loginString = "Логин";
	private String pswdString = "Пароль";
	private String buttonString = "Войти";
	private IDatabaseConnector dbConnector;
	
	public LoginView(Composite parent, BasicEntryPoint enterPoint){
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
		
		loginLabel = new Label(loginComposite,SWT.NONE);
		loginLabel.setText(loginString);
		
		loginText = new Text(loginComposite, SWT.BORDER);
		loginText.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,true));
		
		pswdLabel = new Label(loginComposite,SWT.NONE);
		pswdLabel.setText(pswdString);
		
		pswdText = new Text(loginComposite,SWT.BORDER|SWT.PASSWORD);
		pswdText.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,true));
		
		loginButton = new Button(loginComposite, SWT.PUSH);
		loginButton.setText(buttonString);
		loginButton.setLayoutData(new GridData(SWT.CENTER,SWT.CENTER,true,true,2,1));
		loginButton.addListener(SWT.MouseUp,new Listener() {
			@Override
			public void handleEvent(Event event) {
				String pswd = pswdText.getText();
				String login = loginText.getText();
				byte[] pasHash = encryptionPSWD(pswd);
				identification(login, pasHash);
			}
		});
	}
	
	@Override
	public void dispose() {
		loginComposite.dispose();
	}
	
	private boolean identification(String login, byte[] pswd){
		//тест логин - пароль
		if(false) return false;
		User user = new User();
		user.setId(1);
		if(login.equals("admin")) user.setAdmin(true);
		else user.setAdmin(false);
		//User user = dbConnector.getUser(login);
		try{
		RWT.getSettingStore().setAttribute("userID", String.valueOf(user.getId()));
		//dbConnector.logUserIn(user.getId());
		if(!user.isAdmin()) enterPoint.changeView(1);
		else enterPoint.changeView(2);
		return true;
		}catch (IOException e) {
			System.err.println("Не смог сохранить id пользователя. Авторизация невозможна.");
			return false;
		}
	}
	
	private byte[] encryptionPSWD(String pswd){
		return new byte[1];
	}
}
