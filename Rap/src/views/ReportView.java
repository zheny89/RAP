package views;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridColumnGroup;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.template.Template;
import org.eclipse.rap.rwt.template.TextCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import database.LinkConnector;
import database.Worker;
import database.WorkerToWorktimesTable;
import exception.EntryNotExistsException;
import rap.BasicEntryPoint;

public class ReportView implements View {

	//private final BasicEntryPoint enterPoint;
	private Composite parent, reportComposite, headerComposite, tableComposite, footerComposite;
	private List<Worker> workers;
	private WorkerToWorktimesTable wwt;
	
	public ReportView(BasicEntryPoint enterPoint, Composite parent, LocalDate fromDay, LocalDate toDay, boolean editMode) {
		//this.enterPoint = enterPoint;
		this.parent = parent;
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
		if (editMode)
			createEditableNebulaTable(tableComposite, fromDay, toDay);
		else createNebulaTable(tableComposite, fromDay, toDay, true);
		
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
	
	@SuppressWarnings("unused")
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
	
	private Grid createNebulaTable(Composite parent, LocalDate fromDay, LocalDate toDay, boolean collapsable) {
		Grid grid = new Grid(reportComposite,SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
	    grid.setHeaderVisible(true);
	    grid.setLinesVisible(true);
	    
	    GridColumn column = new GridColumn(grid,SWT.NONE);
	    column.setText("Работник");
	    column.setWidth(200);
	    
	    LocalDate day = LocalDate.ofEpochDay(fromDay.toEpochDay());
	    Month month = null;
	    List<GridColumnGroup> monthGroups = new ArrayList<GridColumnGroup>();
	    // columns
		while (day.isBefore(toDay) || day.isEqual(toDay)) {
			if (!day.getMonth().equals(month)) {
				month = day.getMonth();
				monthGroups.add(addMonthGroup(grid, month, day.getYear()));
			}
			GridColumnGroup currentGroup = monthGroups.get(monthGroups.size() - 1);
			addDateColumn(currentGroup, day, collapsable);
			day = day.plusDays(1);
			if (collapsable && !day.getMonth().equals(month))
				addSummaryColumn(currentGroup);
		};
		if (collapsable) addSummaryColumn(monthGroups.get(monthGroups.size() - 1));
	    // заполнение
		for (int index = 0; index < workers.size(); ++index) {
			GridItem item = new GridItem(grid, SWT.NONE);
			String[] content = wwt.getNebulaItemContent(workers.get(index), collapsable);
			for (int j = 0; j < content.length; ++j)
				item.setText(j, content[j]);
			item.setData("workerID", workers.get(index).getId());
		}
		return grid;
	}
	
	private Grid createEditableNebulaTable(Composite parent, LocalDate fromDay, LocalDate toDay) {
		
		Grid table = createNebulaTable(parent, fromDay, toDay, false);
		Template template = new Template();
		
		TextCell nameCell = new TextCell(template);
		int width = table.getColumn(0).getWidth();
		nameCell.setLeft(0).setWidth(width).setTop(3).setBottom(0);
		int cellBegin = width;
		nameCell.setBindingIndex(0);
		nameCell.setVerticalAlignment(SWT.CENTER);
		
		for (int i = 1; i < table.getColumnCount(); ++i)  {
			TextCell textCell = new TextCell(template);
			width = table.getColumn(i).getWidth();
			textCell.setLeft(cellBegin).setWidth(width)
				.setTop(5).setBottom(0)
				.setBindingIndex(i)
				.setSelectable(true)
				.setHorizontalAlignment(SWT.CENTER).setVerticalAlignment(SWT.CENTER)
				.setName(Integer.toString(i));
			cellBegin += width;
		}
		table.setData(RWT.ROW_TEMPLATE, template);
		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.detail == RWT.CELL) {
					int index = Integer.parseInt(e.text);
					LocalDate day = fromDay.plusDays(index - 1);
					GridItem item = (GridItem) e.item;
					int id = (Integer) item.getData("workerID");
					openCellEditDialog(id, day, item, index);
				}
			}
		});
		return table;
	}
	
	private GridColumnGroup addMonthGroup(Grid grid, Month month, int year) {
		GridColumnGroup monthGroup = new GridColumnGroup(grid, SWT.TOGGLE);
	    monthGroup.setText(monthName(month) + " " + Integer.toString(year));
	    return monthGroup;
	}

	private void addDateColumn(GridColumnGroup currentGroup, LocalDate day, boolean collapsable) {
		GridColumn dateColumn = new GridColumn(currentGroup, SWT.CENTER);
		dateColumn.setWidth(100);
		dateColumn.setSummary(!collapsable);
		dateColumn.setText(day.format(DateTimeFormatter.ofPattern("dd.MM"))
				+ ", " + dayOfWeekName(day.getDayOfWeek()));
		dateColumn.setData("date", day.toString());
	}

	private void addSummaryColumn(GridColumnGroup currentGroup) {
		GridColumn summaryColumn = new GridColumn(currentGroup, SWT.NONE);
	    summaryColumn.setText("сумма");
	    summaryColumn.setWidth(125);
	    summaryColumn.setDetail(false);
	    summaryColumn.setSummary(true);
	}

	private static String monthName(Month month) {
		switch (month) {
		case JANUARY: return "январь";
		case FEBRUARY: return "февраль";
		case MARCH: return "март";
		case APRIL: return "апрель";
		case MAY: return "май";
		case JUNE: return "июнь";
		case JULY: return "июль";
		case AUGUST: return "август";
		case SEPTEMBER: return "сентябрь";
		case OCTOBER: return "октябрь";
		case NOVEMBER: return "ноябрь";
		case DECEMBER: return "декабрь";
		default: return "";
		}
	}
	
	private static String dayOfWeekName(DayOfWeek dow) {
		switch (dow) {
		case MONDAY: return "ПН";
		case TUESDAY: return "ВТ";
		case WEDNESDAY: return "СР";
		case THURSDAY: return "ЧТ";
		case FRIDAY: return "ПТ";
		case SATURDAY: return "СБ";
		case SUNDAY: return "ВС";
		default: return "";
		}
	}
	
	private void openCellEditDialog(int workerId, LocalDate day, GridItem selectedRow, int columnIndex) {
		Shell shell = new Shell(parent.getShell(), SWT.DIALOG_TRIM);
		shell.setText(selectedRow.getText(0) + ", " + day.toString());
		shell.setLayout(new FillLayout());
		
		Composite pane = new Composite(shell, SWT.NONE);
		pane.setLayout(new GridLayout(1, false));
		
		Composite flagCompos = new Composite(pane, SWT.NONE);
		flagCompos.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		flagCompos.setLayout(new GridLayout(2, false));
		
		Label flagLabel = new Label(flagCompos, SWT.NONE);
		flagLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		flagLabel.setText("Состояние:");
		
		Combo flagCombo = new Combo(flagCompos, SWT.DROP_DOWN);
		flagCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		flagCombo.add(Worker.Flags.toString(Worker.Flags.NONE), Worker.Flags.NONE);
		flagCombo.add(Worker.Flags.toString(Worker.Flags.TIME_OFF), Worker.Flags.TIME_OFF);
		flagCombo.add(Worker.Flags.toString(Worker.Flags.SICK_LEAVE), Worker.Flags.SICK_LEAVE);
		flagCombo.add(Worker.Flags.toString(Worker.Flags.VACATION), Worker.Flags.VACATION);
		flagCombo.add(Worker.Flags.toString(Worker.Flags.FIRED), Worker.Flags.FIRED);
		flagCombo.select(0);
		
		Composite hoursCompos = new Composite(pane, SWT.NONE);
		hoursCompos.setLayout(new GridLayout(2, false));
		hoursCompos.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		Label hoursLabel = new Label(hoursCompos, SWT.NONE);
		hoursLabel.setText("Количество часов: ");
		hoursLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		
		Spinner hoursSpinner = new Spinner(hoursCompos, SWT.BORDER);
		hoursSpinner.setMaximum(24);
		hoursSpinner.setMinimum(0);
		hoursSpinner.setSelection(8);
		hoursSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		flagCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int flag = flagCombo.getSelectionIndex();
				hoursCompos.setEnabled(flag == Worker.Flags.NONE || flag == Worker.Flags.TIME_OFF);
			}
		});
		
		Composite buttonsCompos = new Composite(pane, SWT.NONE);
		buttonsCompos.setLayout(new GridLayout(2, true));
		buttonsCompos.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		Button okButton = new Button(buttonsCompos, SWT.PUSH);
		okButton.setText("Применить");
		okButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		
		Button cancelButton = new Button(buttonsCompos, SWT.PUSH);
		cancelButton.setText("Отмена");
		cancelButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.setVisible(false);
				shell.close();
				shell.dispose();
			}
		});
		okButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				short newFlag = (short) flagCombo.getSelectionIndex();
				boolean isWorkFlag = (newFlag == Worker.Flags.NONE || newFlag == Worker.Flags.TIME_OFF);
				short newHours = (short) hoursSpinner.getSelection();
				try {
					LinkConnector.updateWorktimeHours(workerId, day, (isWorkFlag) ? newHours : 0, newFlag);
				} catch (EntryNotExistsException e1) {
					e1.printStackTrace(); // should never fire
				}
				selectedRow.setText(columnIndex, isWorkFlag
						? Short.toString(newHours) : Worker.Flags.toSmallString(newFlag));
				shell.setVisible(false);
				shell.close();
				shell.dispose();
			}
		});
		
		shell.pack();
		Point center = new Point(parent.getBounds().width / 2, parent.getBounds().height / 2);
		center.x -= shell.getSize().x / 2;
		center.y -= shell.getSize().y / 2;
		shell.setLocation(center);
		shell.setVisible(true);
		shell.open();
	}

	@Override
	public void dispose() {
	}

}
