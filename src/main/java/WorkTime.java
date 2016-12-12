import java.time.LocalDateTime;
import java.util.Arrays;


public class WorkTime {

	public static String convertMinuteToString(int minute) {
		return String.format("%d%s %d%s", (int)(minute/60), "시간", (int)(minute%60), "분");
	}
	
	/**
	 * @param value A string value to parse to hour and minute.
	 * @return Returns <tt>null</tt> when cannot parse hour and minute from given string value. otherwise, returns two values as list [0: hour, 1: minute]
	 */
	public static int[] parseHourAndMinute(String value) {
		if (value == null) {
			return null;
		} else {
			String[] time = value.replaceAll("[^?0-9]+", " ").trim().split(" ");
			
			if (time.length == 2) {
				return Arrays.asList(time).stream().mapToInt(Integer::parseInt).toArray();
			}
		}
		
		return null;
	}
	
	/**
	 * Returns parsed minutes using given <code>hour</code> and <code>minute</code> without calculation.
	 */
	public static int parseTimeToMinute(int hour, int minute) {
		return (hour * 60) + minute;
	}
	
	public static int calcTodayWorkTimeAsMinute(int startHour, int startMinute) {
		LocalDateTime current = LocalDateTime.now();
		return calcDayWorkTimeAsMinute(startHour, startMinute, current.getHour(), current.getMinute());
	}
	
	public static int calcDayWorkTimeAsMinute(int startHour, int startMinute, int endHour, int endMinute) {
		int dayWorkTimeAsMinute = parseTimeToMinute(endHour, endMinute) - parseTimeToMinute(startHour, startMinute);
		
		int breakTimeAsMinute = 0;
		for (int i=1, j=dayWorkTimeAsMinute/240; i<=j; i++) {
			if (((240*i)+30) <= dayWorkTimeAsMinute) {
				breakTimeAsMinute += 30;
			} else if ((240*i) < dayWorkTimeAsMinute && dayWorkTimeAsMinute < ((240*i)+30)) {
				breakTimeAsMinute += dayWorkTimeAsMinute - (240*i);
			}
		}
		
		return dayWorkTimeAsMinute - breakTimeAsMinute;
	}
	
	public static double calcTodayWorkTimeAsPercent(int startHour, int startMinute) {
		LocalDateTime current = LocalDateTime.now();
		return calcTodayWorkTimeAsPercent(calcDayWorkTimeAsMinute(startHour, startMinute, current.getHour(), current.getMinute()));
	}
	
	public static double calcTodayWorkTimeAsPercent(int todayWorkTimeMinute) {
		if (todayWorkTimeMinute <= 0) {
			return 0.0;
		}
		
		if (todayWorkTimeMinute >= 480) {
			return 1.0;
		}
		
		return todayWorkTimeMinute / 480.0;
	}
	
	public static double calcWeekWorkTimeAsPercent(int totalWorkTimeMinute) {
		if (totalWorkTimeMinute <= 0) {
			return 0.0;
		}
		
		if (totalWorkTimeMinute >= 2400) {
			return 1.0;
		}
		
		return totalWorkTimeMinute / 2400.0;
	}
	
}
