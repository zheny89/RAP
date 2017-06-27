package rap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

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
				enterPoint.changeView(1);
			}
		});
	}
	
	@Override
	public void dispose() {
		loginComposite.dispose();
	}
}
