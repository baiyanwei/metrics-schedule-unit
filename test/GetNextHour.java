import java.util.Date;

import org.quartz.CronExpression;

public class GetNextHour {

	public static void main(String[] args) {
		try {
			long currentTime = System.currentTimeMillis();
			String scheduel = "0 12 18 * * ?";
//			String scheduel = "0 10 * * * ?";
			CronExpression cron = new CronExpression(scheduel);
			long nextPoint = cron.getNextValidTimeAfter(new Date(currentTime)).getTime();
			long flowTime = nextPoint - currentTime;
			// 1 hour.
			boolean isSchedule = true;
			if (flowTime < 0 || flowTime > 3600000L) {
				isSchedule = false;
			}
			System.out.println(scheduel + ">>>isSchedule:" + isSchedule);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
