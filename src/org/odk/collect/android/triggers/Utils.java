package org.odk.collect.android.triggers;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Utils {
	static public void retryLater(Context context, Class<?> cls, int hours){
		Log.i("t","Retry "+ cls.toString());
		
		Calendar todayEnd = Calendar.getInstance();
		todayEnd.set(Calendar.HOUR_OF_DAY, 22);
		todayEnd.set(Calendar.MINUTE, 59);
		todayEnd.set(Calendar.SECOND, 59);
		
		Calendar now = Calendar.getInstance();
		
		if (now.before(todayEnd)){
			AlarmManager nextAlarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			Intent downloadRequest = new Intent(context, cls);
			PendingIntent nextCheckRequest = PendingIntent.getBroadcast(context, 6, downloadRequest, PendingIntent.FLAG_UPDATE_CURRENT);
			nextAlarm.set(AlarmManager.RTC_WAKEUP,
					now.getTimeInMillis()+hours*3600*1000,
					nextCheckRequest);
		}
	}
}
