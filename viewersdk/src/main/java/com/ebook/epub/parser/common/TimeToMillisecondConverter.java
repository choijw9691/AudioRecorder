package com.ebook.epub.parser.common;

import java.util.concurrent.TimeUnit;

public class TimeToMillisecondConverter implements IConverter<Long> {

	private final int FULL_CLOCK_VALUE = 3;
	private final int PARTIAL_CLOCK_VALUE = 2;
	private final int TIMECONT_VALUE = 1;
	
	private final String HOUR = "h";
	private final String MINUTE = "min";
	private final String SECOND = "s";
	
	String hours="";
	String minutes="";
	String seconds="";
	String millisec="";
	
	@Override
	public Long convert(Object obj) {
		
		String timeStr = (String)obj;

		String[] timeValue = timeStr.split(":");
		
		if(timeValue.length==FULL_CLOCK_VALUE){
			readFullClockValue(timeValue);
		} else if(timeValue.length==PARTIAL_CLOCK_VALUE){
			readPartialClockValue(timeValue);
		} else if(timeValue.length==TIMECONT_VALUE){
			readTimecountValue(timeStr);
		}
		
		return getTime(hours, minutes, seconds, millisec);
	}
	
	private void readFullClockValue(String[] timeValue){
		//		5:34:31.396 = 5 hours, 34 minutes, 31 seconds and 396 milliseconds
		//		124:59:36 = 124 hours, 59 minutes and 36 seconds
		//		0:05:01.2 = 5 minutes, 1 second and 200 milliseconds
		//		0:00:04 = 4 seconds
		
		hours = timeValue[0];
		minutes =timeValue[1];
		seconds =  String.valueOf((int)Math.round(Double.valueOf(timeValue[2])));
		millisec = calculateSecondAsMillisecond(formattedStr(Double.valueOf(timeValue[2]), seconds));
		
//		Log.d("DEBUG"," Full***** H :: "+hours+" M ::: "+minutes+" S :: "+seconds+" MS ::: "+millisec);
	}

	private void readPartialClockValue(String[] timeValue){
		//		09:58 = 9 minutes and 58 seconds
		//		00:56.78 = 56 seconds and 780 milliseconds
	
		minutes = timeValue[0];
		seconds = String.valueOf((int)Math.round(Double.valueOf(timeValue[1])));
		millisec = calculateSecondAsMillisecond(formattedStr(Double.valueOf(timeValue[1]), seconds));
		
//		Log.d("DEBUG"," Partial***** M :: "+minutes+" S :: "+seconds+" MS ::: "+millisec);
	}

	private void readTimecountValue(String timeValue){
		//		76.2s = 76.2 seconds = 76 seconds and 200 milliseconds
		//		7.75h = 7.75 hours = 7 hours and 45 minutes
		//		13min = 13 minutes
		//		2345ms = 2345 milliseconds
		
		//		12.345 = 12 seconds and 345 milliseconds
		
		double time = 0;
		

		if(timeValue.endsWith(HOUR)){

			time = Double.valueOf(timeValue.replace(HOUR, ""));

			hours = String.valueOf((int)Math.round(time));
			minutes = calculateHourAsMinute(formattedStr(time, hours));
				
//			Log.d("DEBUG"," TimeCount***** H :: "+hours+" M ::: "+minutes);

		} else if(timeValue.endsWith(MINUTE)){

			time = Double.valueOf(timeValue.replace(MINUTE, ""));

			minutes = String.valueOf((int)Math.round(time));
			seconds = calculateMinuteAsSecond(formattedStr(time, minutes));

//			Log.d("DEBUG"," TimeCount***** M :: "+minutes+" S ::: "+seconds);

		} else if(timeValue.endsWith(SECOND)){

			time = Double.valueOf(timeValue.replace(SECOND, ""));

			seconds = String.valueOf((int)Math.round(time));
			millisec = calculateSecondAsMillisecond(formattedStr(time, seconds));

//			Log.d("DEBUG"," TimeCount***** S :: "+seconds+" MS ::: "+millisec);

		} else{

			time = Double.valueOf(timeValue);

			seconds = String.valueOf((int)Math.round(time));
			millisec = calculateSecondAsMillisecond(formattedStr(time, seconds));

//			Log.d("DEBUG"," TimeCount***** S :: "+seconds+" MS ::: "+millisec);
		}
	}
	
	private boolean hasFraction(String timeValue){
		return timeValue.contains(".");
	}
	
	private long getTime(String h, String m, String s, String ms){

		long hourToMs = 0;
		long minToMs = 0;
		long secToMs = 0;
		long msToMs =0;
		
		if(!h.isEmpty()) {
			hourToMs = TimeUnit.HOURS.toMillis(Long.parseLong(h)); 
//			Log.d("DEBUG","hourToMs ::: "+hourToMs);
		}
		if(!m.isEmpty()){
			minToMs = TimeUnit.MINUTES.toMillis(Long.parseLong(m));   
//			Log.d("DEBUG","minToMs ::: "+minToMs);
		}
		if(!s.isEmpty()){
			secToMs = TimeUnit.SECONDS.toMillis(Long.parseLong(s));   
//			Log.d("DEBUG","secToMs ::: "+secToMs);
		}
		if(!ms.isEmpty()){
			msToMs=Long.parseLong(ms);
		}
		
		long total = hourToMs+minToMs+secToMs+msToMs;
		
//		Log.d("DEBUG", " total ::: "+total);
		
		return total;
	}
	
	 private String calculateHourAsMinute(String hh) {
		 return String.valueOf((int)(Double.valueOf(hh)*60));
     }

	 private String calculateMinuteAsSecond(String mm) {
         return String.valueOf((int)(Double.valueOf(mm)*60));
     }
	 
     private String calculateSecondAsMillisecond(String ss) {
    	 return String.valueOf((int)(Double.valueOf(ss)*1000));
     }
	
     private String formattedStr(double doubleVal, String intVal){
    	 return String.format("%f", doubleVal- Integer.parseInt(intVal));
	}
	
//	public static String getElapsedMilliseconds(long milliseconds) {
//		long hour = TimeUnit.MILLISECONDS.toHours(milliseconds);
//		long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) - TimeUnit.HOURS.toMinutes(hour);
//		long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) 
//				- TimeUnit.HOURS.toSeconds(hour)
//				- TimeUnit.MINUTES.toSeconds(minutes);
//
//		return String.format(TIME_ELAPSED_FORMAT, hour, minutes, seconds);
//	}
}
