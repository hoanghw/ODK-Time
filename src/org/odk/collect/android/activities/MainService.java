package org.odk.collect.android.activities;

import java.util.Calendar;

import org.odk.collect.android.receivers.DownloadRequest;
import org.odk.collect.android.receivers.SetTimeTrigger;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class MainService extends Service {

	@Override
	public void onCreate(){
		super.onCreate();
		//These receiver are called below when getBroadcast
		//context.sendBroadcast(new Intent("settimetrigger"));
        //context.sendBroadcast(new Intent("downloadrequest"));
        
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		
		AlarmManager cron = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        
		//Schedule settimetrigger task
		Intent timeTrigger = new Intent(this, SetTimeTrigger.class);
		PendingIntent pTimeTrigger = PendingIntent.getBroadcast(this, 0, timeTrigger, PendingIntent.FLAG_UPDATE_CURRENT);
		cron.setRepeating(AlarmManager.RTC_WAKEUP, 
				calendar.getTimeInMillis(),
				AlarmManager.INTERVAL_DAY, 
				pTimeTrigger);
		
		//Schedule download task
		Intent downloadRequest = new Intent(this, DownloadRequest.class);
		PendingIntent pDownloadRequest = PendingIntent.getBroadcast(this, 0, downloadRequest, PendingIntent.FLAG_UPDATE_CURRENT);
		cron.setRepeating(AlarmManager.RTC_WAKEUP,
				calendar.getTimeInMillis(),
				AlarmManager.INTERVAL_DAY,
				pDownloadRequest);
		
	}
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
