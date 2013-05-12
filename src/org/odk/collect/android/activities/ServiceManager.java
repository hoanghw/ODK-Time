package org.odk.collect.android.activities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.odk.collect.android.R;
import org.odk.collect.android.logic.FormDetails;
import org.odk.collect.android.tasks.DownloadFormListTask;
import org.odk.collect.android.tasks.DownloadFormsTask;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.Toast;

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
		super.onCreate();
		//android.os.Debug.waitForDebugger();
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		
		AlarmManager cron = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		
        sendBroadcast(new Intent("settimetrigger"));
        sendBroadcast(new Intent("downloadtask"));
        
		//Schedule settimetrigger task
		Intent timeTrigger = new Intent(this, SetTimeTrigger.class);
		PendingIntent pTimeTrigger = PendingIntent.getBroadcast(this, 0, timeTrigger, PendingIntent.FLAG_CANCEL_CURRENT);
		cron.setRepeating(AlarmManager.RTC_WAKEUP, 
				calendar.getTimeInMillis(),
				AlarmManager.INTERVAL_HALF_DAY, 
				pTimeTrigger);
		
		//Schedule download task
		Intent downloadTask = new Intent(this, DownloadTask.class);
		PendingIntent pDownloadTask = PendingIntent.getBroadcast(this, 0, downloadTask, PendingIntent.FLAG_CANCEL_CURRENT);
		cron.setRepeating(AlarmManager.RTC_WAKEUP,
				calendar.getTimeInMillis(),
				AlarmManager.INTERVAL_HALF_DAY,
				pDownloadTask);
	}

}
