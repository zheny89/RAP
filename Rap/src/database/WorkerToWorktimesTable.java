package database;

import java.time.LocalDate;
import java.time.Month;
import java.util.Hashtable;

public class WorkerToWorktimesTable extends Hashtable<Worker, Worktime[]> {
	private final LocalDate fromDay, toDay;
	
	public WorkerToWorktimesTable(LocalDate fromDay, LocalDate toDay) {
		super();
		this.fromDay = fromDay;
		this.toDay = toDay;
	}
	
	public String[] getTableItemContent(Worker worker) {
		int numDayColumns = (int) (toDay.toEpochDay() - fromDay.toEpochDay() + 1);
		String[] itemContent = new String[1 + numDayColumns];
		itemContent[0] = worker.getName();
		Worktime[] workerTimes = this.get(worker);
		LocalDate day = LocalDate.ofEpochDay(fromDay.toEpochDay());
		int contentIndex = 1, timesIndex = 0;
		while (day.isBefore(toDay) || day.isEqual(toDay)) {
			if (workerTimes.length == 0 || timesIndex >= workerTimes.length 
					|| workerTimes[timesIndex].getDay().toLocalDate().isAfter(day)) {
				// �� ���������
				short flag = worker.getFlag();
				if (flag != Worker.Flags.NONE && !day.isBefore(worker.getFlagStartDate().toLocalDate()))
					itemContent[contentIndex++] = Worker.Flags.toSmallString(flag);
				else itemContent[contentIndex++] = "0";
			}
			else
				itemContent[contentIndex++] = Short.toString(workerTimes[timesIndex++].getHours());
			day = day.plusDays(1);
		}
		return itemContent;
	}
	
	public Worktime getWorktime(Worker worker, LocalDate day) {
		Worktime[] wts = this.get(worker);
		for (int i = 0; i < wts.length; ++i) {
			LocalDate wtDay = wts[i].getDay().toLocalDate();
			if (wtDay.isAfter(day)) return null; // TODO: should be special Worktime value
			if (wtDay.isEqual(day)) return wts[i];
		}
		return null;
	}

	public String[] getNebulaItemContent(Worker worker, boolean withSumColumns) {
		int numSumColumns = (withSumColumns) ? countMonths() : 0;
		int numDayColumns = (int) (toDay.toEpochDay() - fromDay.toEpochDay() + 1);
		String[] itemContent = new String[1 + numDayColumns + numSumColumns];
		itemContent[0] = worker.getName();
		Worktime[] workerTimes = this.get(worker);
		LocalDate day = LocalDate.ofEpochDay(fromDay.toEpochDay());
		Month curMonth = day.getMonth();
		int contentIndex = 1, timesIndex = 0;
		int monthSum = 0;
		while (day.isBefore(toDay) || day.isEqual(toDay)) {
			if (workerTimes.length == 0 || timesIndex >= workerTimes.length 
					|| workerTimes[timesIndex].getDay().toLocalDate().isAfter(day)) {
				// нет отметки
				short flag = worker.getFlag();
				if (flag != Worker.Flags.NONE && flag != Worker.Flags.TIME_OFF 
						&& !day.isBefore(worker.getFlagStartDate().toLocalDate()))
					itemContent[contentIndex++] = Worker.Flags.toSmallString(flag);
				else itemContent[contentIndex++] = "0";
			}
			else {
				// есть отметка
				short hours = workerTimes[timesIndex].getHours();
				short flag = workerTimes[timesIndex].getFlag();
				boolean isWorkFlag = flag == Worker.Flags.NONE || flag == Worker.Flags.TIME_OFF;
				itemContent[contentIndex++] = isWorkFlag ? Short.toString(hours) : Worker.Flags.toSmallString(flag);
				monthSum += hours;
				++timesIndex;
			}
			day = day.plusDays(1);
			if (withSumColumns && !day.getMonth().equals(curMonth)) {
				itemContent[contentIndex++] = Integer.toString(monthSum);
				monthSum = 0;
				curMonth = day.getMonth();
			}
		}
		if (withSumColumns) itemContent[contentIndex++] = Integer.toString(monthSum);
		return itemContent;
	}

	private int countMonths() {
		LocalDate day = LocalDate.of(fromDay.getYear(), fromDay.getMonthValue(), toDay.getDayOfMonth());
		int monthCount = 1;
		if (day.equals(toDay))
			return monthCount;
		else while (day.isBefore(toDay)) {
			++monthCount;
			day = day.plusMonths(1);
		}
		return monthCount;
	}
}
