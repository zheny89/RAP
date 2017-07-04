package views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class ReportDialog extends Shell {

	public ReportDialog(Shell parent) {
		super(parent);
		this.setText("Отчет");

		Composite pane = new Composite(parent, SWT.NONE);
		
		Point size = this.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		this.setSize(size.x, size.y);
	}
}
