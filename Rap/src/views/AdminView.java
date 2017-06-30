package views;

import java.awt.Color;
import java.time.LocalDate;
import java.util.Date;

import org.eclipse.rap.rwt.client.service.ExitConfirmation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class AdminView implements View {
	private Composite adminComposite, upperComposite, lowerComposite, lowerHeaderComposite, listComposite;
	private ScrolledComposite listHolderComposite;
	private Button reportsButton,changeBaseButton,messageButton;
	private Label titleLabel;
	private int userID;
	private ImageData iconData;
	
	public AdminView(Composite parent) {
		iconData = new ImageData(AdminView.class.getResourceAsStream("green.png"));
		
		adminComposite = new Composite(parent, SWT.BORDER);
		adminComposite.setLayoutData(new GridData(SWT.CENTER,SWT.CENTER,true,true));
		adminComposite.setLayout(new GridLayout(1, true));
		
		upperComposite = new Composite(adminComposite, SWT.NONE);
		upperComposite.setLayout(new GridLayout(3, true));
		adminComposite.setLayoutData(new GridData(SWT.CENTER,SWT.CENTER,true,true));
		
		reportsButton = new Button(upperComposite, SWT.PUSH);
		reportsButton.setText("Отчеты");
		reportsButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		changeBaseButton = new Button(upperComposite, SWT.PUSH);
		changeBaseButton.setText("Редактирование базы");
		changeBaseButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		messageButton = new Button(upperComposite, SWT.PUSH);
		messageButton.setText("10");
		messageButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		lowerComposite = new Composite(adminComposite, SWT.NONE);
		lowerComposite.setLayout(new GridLayout(1, false));
		lowerComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		
		lowerHeaderComposite = new Composite(lowerComposite, SWT.BORDER);
		lowerHeaderComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		GridLayout lowerHeaderLayout = new GridLayout(2, false);
		lowerHeaderLayout.marginLeft = lowerHeaderLayout.marginRight = 0;
		lowerHeaderComposite.setLayout(lowerHeaderLayout);
		Label currentDateLabel = new Label(lowerHeaderComposite, SWT.NONE);
		currentDateLabel.setText("Текущая дата: " + LocalDate.now().toString());
		Text searchField = new Text(lowerHeaderComposite, SWT.BORDER);
		searchField.setText("поиск...");
		searchField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		
		listHolderComposite = new ScrolledComposite(lowerComposite, SWT.V_SCROLL);
		listHolderComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		listComposite = new Composite(listHolderComposite, SWT.NONE);
		listHolderComposite.setContent(listComposite);
		
		listComposite.setLayout(new GridLayout(2, false));
		fillUsersList();
		listComposite.setSize(listComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		adminComposite.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent event) {
				//iconData.			
			}
		});
	}
	
	private void fillUsersList() {
		//TODO: get worker list from DB
		//List<Worker> workers = BasicApplication.database.getPresentUsers();
		for(int i = 0; i < 50; i++) { // for (Worker worker : workers)
			Composite compos = new Composite(listComposite, SWT.NONE);
			compos.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			compos.setLayout(new GridLayout(2, false));
			
			Label iconHolder = new Label(compos, SWT.NONE);
			// if (database.getUserPresent(worker.getId(), new Date()))
			Image icon = new Image(null, iconData);
			// else icon = new Image(null, blackIconData);
			iconHolder.setImage(icon);
			
			Button button = new Button(compos, SWT.PUSH);
			button.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, true));
			button.setText("Василченко Василий Васильевич"); // worker.getName()
		}
	}
	
	@Override
	public void dispose() {
		adminComposite.dispose();		
	}
}
