import org.junit.Assert;
import org.junit.Test;


public class WorkTimeTest {
	
	@Test
	public void convertMinuteToStringTest() {
		Assert.assertEquals("0시간 0분", WorkTime.convertMinuteToString(0));
		Assert.assertEquals("0시간 1분", WorkTime.convertMinuteToString(1));
		Assert.assertEquals("1시간 0분", WorkTime.convertMinuteToString(60));
		Assert.assertEquals("1시간 1분", WorkTime.convertMinuteToString(61));
		Assert.assertEquals("4시간 15분", WorkTime.convertMinuteToString(255));
		Assert.assertEquals("8시간 0분", WorkTime.convertMinuteToString(480));
	}
	
	@Test
	public void parseHourAndMinuteTest() {
		{
			int[] time = WorkTime.parseHourAndMinute("08시 00분");
			Assert.assertEquals(8, time[0]);
			Assert.assertEquals(0, time[1]);
		}
		{
			int[] time = WorkTime.parseHourAndMinute("08:00");
			Assert.assertEquals(8, time[0]);
			Assert.assertEquals(0, time[1]);
		}
		Assert.assertNull(WorkTime.parseHourAndMinute(null));
		Assert.assertNull(WorkTime.parseHourAndMinute(""));
		Assert.assertNull(WorkTime.parseHourAndMinute("--:--"));
	}
	
	@Test
	public void parseTimeToMinuteTest() {
		Assert.assertEquals(0, WorkTime.parseTimeToMinute(0, 0));
		Assert.assertEquals(60, WorkTime.parseTimeToMinute(1, 0));
		Assert.assertEquals(240, WorkTime.parseTimeToMinute(4, 0));
		Assert.assertEquals(480, WorkTime.parseTimeToMinute(8, 0));
		Assert.assertEquals(540, WorkTime.parseTimeToMinute(9, 0));
		Assert.assertEquals(2400, WorkTime.parseTimeToMinute(40, 0));
	}
	
	@Test
	public void calcDayWorkTimeAsMinuteTest() {
		Assert.assertEquals(239, WorkTime.calcDayWorkTimeAsMinute(8, 0, 11, 59));
		
		Assert.assertEquals(240, WorkTime.calcDayWorkTimeAsMinute(8, 0, 12, 0));    // break time
		Assert.assertEquals(240, WorkTime.calcDayWorkTimeAsMinute(8, 0, 12, 15));   // break time
		Assert.assertEquals(240, WorkTime.calcDayWorkTimeAsMinute(8, 0, 12, 30));   // break time
		
		Assert.assertEquals(255, WorkTime.calcDayWorkTimeAsMinute(8, 0, 12, 45));
		
		Assert.assertEquals(450, WorkTime.calcDayWorkTimeAsMinute(8, 0, 16, 0));    // break time
		Assert.assertEquals(450, WorkTime.calcDayWorkTimeAsMinute(8, 0, 16, 15));   // break time
		Assert.assertEquals(450, WorkTime.calcDayWorkTimeAsMinute(8, 0, 16, 30));   // break time
		
		Assert.assertEquals(465, WorkTime.calcDayWorkTimeAsMinute(8, 0, 16, 45));
		Assert.assertEquals(480, WorkTime.calcDayWorkTimeAsMinute(8, 0, 17, 0));
		Assert.assertEquals(495, WorkTime.calcDayWorkTimeAsMinute(8, 0, 17, 15));
		Assert.assertEquals(510, WorkTime.calcDayWorkTimeAsMinute(8, 0, 17, 30));
	}
	
	@Test
	public void calcTodayWorkTimeAsPercentTest() {
		Assert.assertEquals(0.0,  WorkTime.calcTodayWorkTimeAsPercent(0), 0.0);
		Assert.assertEquals(0.5,  WorkTime.calcTodayWorkTimeAsPercent(240), 0.0);
		Assert.assertEquals(1.0,  WorkTime.calcTodayWorkTimeAsPercent(480), 0.0);
		Assert.assertEquals(1.0,  WorkTime.calcTodayWorkTimeAsPercent(500), 0.0);
	}
	
	@Test
	public void calcWeekWorkTimeAsPercentTest() {
		Assert.assertEquals(0.0,  WorkTime.calcWeekWorkTimeAsPercent(0), 0.0);
		Assert.assertEquals(0.5,  WorkTime.calcWeekWorkTimeAsPercent(1200), 0.0);
		Assert.assertEquals(1.0,  WorkTime.calcWeekWorkTimeAsPercent(2400), 0.0);
		Assert.assertEquals(1.0,  WorkTime.calcWeekWorkTimeAsPercent(3000), 0.0);
	}
	
}
