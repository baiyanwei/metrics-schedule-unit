import java.util.Date;
import java.util.TimeZone;


public class GetNextHour {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		long currentPoint=System.currentTimeMillis();
//		System.out.println(">>"+new Date());
//		Date aDate=new Date(currentPoint);
//		long nextHourStart=(currentPoint+1000*60*60);
//		nextHourStart=nextHourStart-(nextHourStart%(1000*60*60));
//		System.out.println(">>"+aDate);
//		System.out.println(">>"+new Date(nextHourStart));
//		System.out.println("delayA:"+(3600000 - currentPoint % 3600000));
//		System.out.println("delayB:"+(nextHourStart-currentPoint));
		//86400000
		long currentPoint=System.currentTimeMillis();
		Date aDate=new Date(currentPoint);
		long nextHourStart=currentPoint+(86400000 - currentPoint % 86400000);
		System.out.println(">>"+aDate);
		//
		System.out.println(">>"+new Date(nextHourStart));
		System.out.println(">>"+new Date(0));
		Date tomorrow = new Date(aDate.getYear(), aDate.getMonth(), aDate.getDate() + 1, 0, 0, 0);
		System.out.println(">>"+tomorrow);
		System.out.println("delayA:"+(86400000 - (currentPoint+TimeZone.getDefault().getRawOffset()) % 86400000));
		System.out.println("delayB:"+(nextHourStart-currentPoint));
		System.out.println("delayc:"+(tomorrow.getTime()-aDate.getTime()));
		System.out.println("delayc:"+TimeZone.getTimeZone("America/New_York").getRawOffset());
	}

}
