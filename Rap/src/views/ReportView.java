package views;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import database.LinkConnector;
import database.Worker;
import database.WorkerToWorktimesTable;
import rap.BasicEntryPoint;

public class ReportView implements View {

	//private final BasicEntryPoint enterPoint;
	private Composite reportComposite, headerComposite, tableComposite, footerComposite;
	private List<Worker> workers;
	private WorkerToWorktimesTable wwt;
	
	public ReportView(BasicEntryPoint enterPoint, Composite parent, LocalDate fromDay, LocalDate toDay) {
		//this.enterPoint = enterPoint;
		reportComposite = new Composite(parent, SWT.BORDER);
		reportComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		reportComposite.setLayout(new GridLayout(1, false));
		
		headerComposite = new Composite(reportComposite, SWT.NONE);
		headerComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		headerComposite.setLayout(new FillLayout());
		Label headerLabel = new Label(headerComposite, SWT.CENTER);
		if (fromDay.isEqual(toDay))
			headerLabel.setText("Отчет за " + fromDay.toString());
		else headerLabel.setText("Отчет за период с " + fromDay.toString() + " по " + toDay.toString());
		
		tableComposite = new Composite(reportComposite, SWT.BORDER);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tableComposite.setLayout(new FillLayout());
		
		workers = LinkConnector.getWorkers();
		wwt = LinkConnector.getWorkerToWorktimes(fromDay, toDay);		
		createTable(tableComposite, fromDay, toDay);
		
		footerComposite = new Composite(reportComposite, SWT.NONE);
		footerComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		footerComposite.setLayout(new RowLayout());
		Button backButton = new Button(footerComposite, SWT.PUSH);
		backButton.setText("Назад");
		backButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				enterPoint.changeView(View.Id.ADMIN_VIEW);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) { /* not called */ }			
		});
	}
	
	private Table createTable(Composite parent, LocalDate fromDay, LocalDate toDay) {
		Table table = new Table (tableComposite, SWT.VIRTUAL | SWT.BORDER);
		table.setLinesVisible(true);
		TableColumn nameColumn = new TableColumn(table, SWT.LEFT);
		nameColumn.setResizable(true);
		nameColumn.setWidth(200);
		List<TableColumn> dateColumns = new ArrayList<TableColumn>();
		LocalDate day = LocalDate.ofEpochDay(fromDay.toEpochDay());
		while (day.isBefore(toDay) || day.isEqual(toDay)) {
			TableColumn dateColumn = new TableColumn(table, SWT.CENTER);
			dateColumn.setWidth(100);
			dateColumn.setResizable(true);
			dateColumn.setText(day.toString());
			dateColumns.add(dateColumn);
			day = day.plusDays(1);
		};
		table.setHeaderVisible(true);
		table.setItemCount(workers.size());
		table.addListener(SWT.SetData, new Listener() {
			  @Override
			  public void handleEvent (Event event) {
		          TableItem item = (TableItem) event.item;
		          int index = table.indexOf(item);
		          Worker w = workers.get(index);
		          item.setText(wwt.getTableItemContent(w));
		      }
		  }); 
		return table;
	}

	@Override
	public void dispose() {
	}

}
