package org.odk.collect.android.activities;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class ServiceManager extends Service {

	public ServiceManager(){
		super();
	}
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	public void onCreate(){
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		Intent intent = new Intent(this, ScheduledTask.class);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, 0);
		AlarmManager cron = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		cron.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(),AlarmManager.INTERVAL_FIFTEEN_MINUTES, pi);
	}

}
