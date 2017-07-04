package views;

import java.time.LocalDate;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import rap.BasicEntryPoint;

public class ReportDialog extends Shell {
	
	//private BasicEntryPoint enterPoint;
	private Button dayButton, periodButton;
	private DateTime dayCalendar, periodStartCalendar, periodEndCalendar;

	public ReportDialog(BasicEntryPoint enterPoint, Shell parent) {
		super(parent);
		//this.enterPoint = enterPoint;
		this.setText("Отчет");
		this.setLayout(new GridLayout(1, false));

		Composite pane = new Composite(this, SWT.NONE);
		pane.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
		pane.setLayout(new RowLayout(SWT.VERTICAL));
		
		Composite dayComposite = new Composite(pane, SWT.NONE);
		dayComposite.setLayout(new GridLayout(2, false));
		dayButton = new Button(dayComposite, SWT.RADIO);
		dayButton.setText("за день: ");
		dayButton.setSelection(true);
		dayButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				dayButton.setSelection(true);
				dayCalendar.setEnabled(true);
				periodButton.setSelection(false);
				periodStartCalendar.setEnabled(false);
				periodEndCalendar.setEnabled(false);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {/*never called*/}
			
		});
		dayCalendar = new DateTime(dayComposite, SWT.DATE | SWT.MEDIUM | SWT.BORDER | SWT.DROP_DOWN);
		LocalDate today = LocalDate.now();
		dayCalendar.setDate(today.getYear(),
				today.getMonthValue() - 1, // в DateTime месяцы нумеруются с 0 до 11
				today.getDayOfMonth());
		
		Composite periodComposite = new Composite(pane, SWT.NONE);
		periodComposite.setLayout(new GridLayout(4, false));
		periodButton = new Button(periodComposite, SWT.RADIO);
		periodButton.setText("за период с: ");
		periodButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				dayButton.setSelection(false);
				dayCalendar.setEnabled(false);
				periodButton.setSelection(true);
				periodStartCalendar.setEnabled(true);
				periodEndCalendar.setEnabled(true);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {/*not called*/}
			
		});
		periodStartCalendar = new DateTime(periodComposite, SWT.DATE | SWT.MEDIUM | SWT.BORDER | SWT.DROP_DOWN);
		periodStartCalendar.setDate(today.getYear(), today.getMonthValue() - 1, today.getDayOfMonth());
		Label toLabel = new Label(periodComposite, SWT.CENTER);
		toLabel.setText(" по: ");
		toLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		periodEndCalendar = new DateTime(periodComposite, SWT.DATE | SWT.MEDIUM | SWT.BORDER | SWT.DROP_DOWN);
		periodEndCalendar.setDate(today.getYear(), today.getMonthValue() - 1, today.getDayOfMonth());
		periodStartCalendar.setEnabled(false);
		periodEndCalendar.setEnabled(false);
		
		Button okButton = new Button(pane, SWT.PUSH | SWT.CENTER);
		okButton.setText("OK");
		okButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				DateTime startWidget = (dayButton.getSelection()) ? dayCalendar : periodStartCalendar;
				DateTime endWidget = (dayButton.getSelection()) ? dayCalendar : periodEndCalendar;
				LocalDate periodStart = LocalDate.of(startWidget.getYear(), startWidget.getMonth() + 1, startWidget.getDay());
				LocalDate periodEnd = LocalDate.of(endWidget.getYear(), endWidget.getMonth() + 1, endWidget.getDay());
				enterPoint.storeReportPeriod(periodStart, periodEnd);
				enterPoint.changeView(View.Id.REPORT_VIEW);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {/*not called*/}
			
		});
		
		Point size = this.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		this.setSize(size.x, size.y);
		Point center = new Point(parent.getBounds().width / 2, parent.getBounds().height / 2);
		center.x -= this.getSize().x / 2;
		center.y -= this.getSize().y / 2;
		this.setLocation(center);
	}
}
