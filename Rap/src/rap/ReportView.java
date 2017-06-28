package rap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class ReportView implements View {

	private Composite reportComposite, headerComposite, tableComposite, footerComposite;
	
	public ReportView(Composite parent) {
		reportComposite = new Composite(parent, SWT.BORDER);
		reportComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		reportComposite.setLayout(new GridLayout(1, false));
		
		headerComposite = new Composite(reportComposite, SWT.NONE);
		headerComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		headerComposite.setLayout(new FillLayout());
		Label headerLabel = new Label(headerComposite, SWT.NONE);
		headerLabel.setText("Отчет за период");
		
		tableComposite = new Composite(reportComposite, SWT.BORDER);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tableComposite.setLayout(new FillLayout());
		
		Table table = new Table (tableComposite, SWT.VIRTUAL | SWT.BORDER);
		  table.setItemCount (1000);
		  table.addListener (SWT.SetData, new Listener() {
			  @Override
			  public void handleEvent (Event event) {
		          TableItem item = (TableItem) event.item;
		          int index = table.indexOf (item);
		          item.setText ("Item " + index);
		          System.out.println (item.getText ());
		      }
		  }); 

		
		footerComposite = new Composite(reportComposite, SWT.NONE);
		footerComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		footerComposite.setLayout(new RowLayout());
		Button backButton = new Button(footerComposite, SWT.PUSH);
		backButton.setText("Назад");
	}
	
	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

}
