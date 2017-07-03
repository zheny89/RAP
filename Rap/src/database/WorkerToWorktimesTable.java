package database;

import java.time.LocalDate;
import java.util.Hashtable;

public class WorkerToWorktimesTable extends Hashtable<Worker, Worktime[]> {
	private final LocalDate fromDay, toDay;
	
	public WorkerToWorktimesTable(LocalDate fromDay, LocalDate toDay) {
		super();
		this.fromDay = fromDay;
		this.toDay = toDay;
	}
	
	/*public String[] getTableItemContent(Worker worker) {
		int numDayColumns = (int) (toDay.toEpochDay() - fromDay.toEpochDay() + 1);
		String[] itemContent = new String[1 + numDayColumns];
		itemContent[0] = worker.getName();
		Worktime[] workerTimes = this.get(worker);
		LocalDate day = LocalDate.ofEpochDay(fromDay.toEpochDay());
		int index = 1;
		while (day.isBefore(toDay) || day.isEqual(toDay)) {
			if (workerTimes.length == 0 || workerTimes[index].getDay().toLocalDate().isAfter(day))
				itemContent[index] = "0";
			else {
				//itemContent[index] = workerTimes[index];
				++index;
			}
			day = day.plusDays(1);
		}
	}*/
	
	public Worktime getWorktime(Worker worker, LocalDate day) {
		Worktime[] wts = this.get(worker);
		for (int i = 0; i < wts.length; ++i) {
			LocalDate wtDay = wts[i].getDay().toLocalDate();
			if (wtDay.isAfter(day)) return null; // TODO: should be special Worktime value
			if (wtDay.isEqual(day)) return wts[i];
		}
		return null;
	}
}
