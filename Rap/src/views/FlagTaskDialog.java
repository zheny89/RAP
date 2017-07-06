package views;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import autoflagschanger.FlagTask;
import autoflagschanger.FlagsChanger;
import database.LinkConnector;
import database.Worker;
import rap.BasicEntryPoint;

public class FlagTaskDialog extends Shell {
	
	//private BasicEntryPoint enterPoint;
	private Composite listComposite;
	private List<Worker> workers;
	private Worker[] workersByComboIndex;
	private List<Button> radioButtons;
	private DateTime dateField;
	private Combo workerBox;
	
	public FlagTaskDialog(BasicEntryPoint enterPoint, Shell parent) {
		super(parent);
		this.workers = LinkConnector.getWorkers();
		this.setText("Задачи");
		this.setLayout(new GridLayout(1, false));

		Composite pane = new Composite(this, SWT.NONE);
		pane.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
		pane.setLayout(new GridLayout(3, false));
		
		createTaskListComposite(pane);
		Label separator = new Label(pane, SWT.SEPARATOR | SWT.VERTICAL);
		separator.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false, true));
		createTaskAddComposite(pane);		
				
		setLocationToCenter();
	}

	private void setLocationToCenter() {
		Point size = this.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		this.setSize(size.x, size.y);
		Point center = new Point(this.getParent().getBounds().width / 2,
				this.getParent().getBounds().height / 2);
		center.x -= this.getSize().x / 2;
		center.y -= this.getSize().y / 2;
		this.setLocation(center);
	}

	private void createTaskAddComposite(Composite pane) {
		Composite rightComposite = new Composite(pane, SWT.NONE);
		rightComposite.setLayout(new GridLayout(1, false));
		rightComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Label header = new Label(rightComposite, SWT.NONE);
		header.setText("Новая задача");
		header.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
		
		Composite workerComposite = new Composite(rightComposite, SWT.NONE);
		workerComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		workerComposite.setLayout(new GridLayout(2, false));
		
		Label workerLabel = new Label(workerComposite, SWT.NONE);
		workerLabel.setText("Работник:");
		workerLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		
		workerBox = new Combo(workerComposite, SWT.NONE);
		fillWorkerComboBox(workerBox);
		workerBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,true,true));
		workerBox.setVisibleItemCount(10);

		Composite flagComposite = new Composite(rightComposite,SWT.NONE);
		flagComposite.setLayoutData(new GridData(SWT.LEFT,SWT.CENTER,true,true));
		flagComposite.setLayout(new GridLayout(1, false));
		
		Label flagLabel = new Label(flagComposite, SWT.NONE);
		flagLabel.setText("Состояние:");
		flagLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		
		radioButtons = new LinkedList<Button>();
		Button noneButton = createFlagRadioButton(flagComposite, Worker.Flags.NONE);
		noneButton.setSelection(true);
		radioButtons.add(noneButton);
		radioButtons.add(createFlagRadioButton(flagComposite, Worker.Flags.TIME_OFF));
		radioButtons.add(createFlagRadioButton(flagComposite, Worker.Flags.SICK_LEAVE));
		radioButtons.add(createFlagRadioButton(flagComposite, Worker.Flags.VACATION));
		radioButtons.add(createFlagRadioButton(flagComposite, Worker.Flags.FIRED));
		
		Composite dateComposite = new Composite(rightComposite, SWT.NONE);
		dateComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		dateComposite.setLayout(new GridLayout(2, false));
		
		Label dateLabel = new Label(dateComposite, SWT.NONE);
		dateLabel.setText("Когда:");
		dateLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		
		dateField = new DateTime(dateComposite, SWT.DATE | SWT.MEDIUM | SWT.BORDER | SWT.DROP_DOWN);
		dateField.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		LocalDate today = LocalDate.now();
		dateField.setYear(today.getYear());
		dateField.setMonth(today.getMonthValue() - 1);
		dateField.setDay(today.getDayOfMonth());
		
		createOkButton(rightComposite);
		
	}

	private void createOkButton(Composite rightComposite) {
		Button okButton = new Button(rightComposite, SWT.PUSH);
		okButton.setText("Добавить задачу");
		okButton.setLayoutData(new GridData(SWT.CENTER,SWT.BOTTOM,true,true));
		Label resultLabel = new Label(rightComposite, SWT.NONE);
		resultLabel.setText("                                     ");
		resultLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		okButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int workerIndex = workerBox.getSelectionIndex();
				if (workerIndex < 0) {
					resultLabel.setText("Работник не выбран");
					resultLabel.pack();
					return;
				}
				int workerId = workersByComboIndex[workerIndex].getId();
				short flag = -1;
				for (Button rb : radioButtons)
					if (rb.getSelection()) {
						flag = (Short) rb.getData("flag");
						break;
					}
				if (flag < 0) {
					resultLabel.setText("Состояние не выбрано");
					resultLabel.pack();
					return;
				}
				LocalDate date = LocalDate.of(dateField.getYear(), dateField.getMonth() + 1, dateField.getDay());
				if (date.isEqual(LocalDate.now())) { // выполнить сейчас
					LinkConnector.updateWorkerFlag(workerId, flag, null);
					resultLabel.setText("Состояние пользователя обновлено");
				} else if (date.isBefore(LocalDate.now())) 
					resultLabel.setText("Выбранное время находится в прошлом");
				else {
					FlagTask task = new FlagTask(workerId, flag, date);
					FlagsChanger.getInstance().addTask(task);
					fillTasksList(rightComposite);
					resultLabel.setText("Задача занесена в список");
				}
				resultLabel.pack();
			}
		});
	}

	private void fillWorkerComboBox(Combo workerBox) {
		workersByComboIndex = new Worker[workers.size()];
		int i = 0;
		for(Worker w : workers) {
			workerBox.add(w.getName(), i);
			workersByComboIndex[i++] = w;
		}
	}

	private Button createFlagRadioButton(Composite flagComposite, short flag) {
		Button flagButton = new Button(flagComposite,SWT.RADIO);
		flagButton.setText(Worker.Flags.toString(flag));
		flagButton.setData("flag", flag);
		flagButton.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,true));
		return flagButton;
	}

	private void createTaskListComposite(Composite pane) {
		Composite leftComposite = new Composite(pane, SWT.NONE);
		leftComposite.setLayout(new GridLayout(1, false));
		leftComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Label header = new Label(leftComposite, SWT.NONE);
		header.setText("Запланированные задачи");
		header.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
		
		ScrolledComposite listHolderComposite = new ScrolledComposite(leftComposite, SWT.V_SCROLL);
		listHolderComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		listComposite = new Composite(listHolderComposite, SWT.NONE);
		listHolderComposite.setContent(listComposite);
		
		listComposite.setLayout(new GridLayout(1, true));
		fillTasksList(listComposite);
		listComposite.setSize(listComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	private void fillTasksList(Composite listComposite) {
		Control[] children = listComposite.getChildren();
		for (Control child: children) child.dispose();
		List<FlagTask> taskList = new ArrayList<FlagTask>();
		for (FlagTask task : taskList) {
			Composite taskComposite = new Composite(listComposite, SWT.NONE);
			taskComposite.setLayoutData(new GridData(SWT.FILL, SWT.UP, true, false));
			taskComposite.setLayout(new GridLayout(1, false));
			Label dateLabel = new Label(taskComposite, SWT.NONE);
			dateLabel.setText(task.getDate().toString());
			dateLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
			Label taskLabel = new Label(taskComposite, SWT.NONE);
			String workerName = getWorkerName(task.getWorkerID());
			String flagName = Worker.Flags.toString(task.getFlag());
			taskLabel.setText(workerName + " - " + flagName);
			taskLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
			Button cancelButton = new Button(taskComposite, SWT.PUSH);
			cancelButton.setText("Отменить");
			cancelButton.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, true, false));
			cancelButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					FlagsChanger.getInstance().removeTask(task);
					taskComposite.dispose();
					GridData data = (GridData) taskComposite.getLayoutData();
					taskComposite.setVisible(false);
					data.exclude = true;
					listComposite.layout(true);
				}
			});
		}
	}

	private String getWorkerName(long workerID) {
		for (Worker w : workers)
			if (w.getId() == workerID)
				return w.getName();
		return "???";
	}
}
