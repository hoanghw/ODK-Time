package org.odk.collect.android.receivers;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class DownloadRequest extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("t","DownloadRequestReceive");
	    ConnectivityManager connectivityManager =
	            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
		
		if (ni == null || !ni.isConnected()){
			Log.i("t","DownloadRetry");
			
			Calendar todayEnd = Calendar.getInstance();
			todayEnd.set(Calendar.HOUR_OF_DAY, 22);
			todayEnd.set(Calendar.MINUTE, 59);
			todayEnd.set(Calendar.SECOND, 59);
			
			Calendar now = Calendar.getInstance();
			
			if (now.before(todayEnd)){
				AlarmManager nextAlarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
				Intent downloadRequest = new Intent(context, DownloadRequest.class);
				PendingIntent nextDownloadRequest = PendingIntent.getBroadcast(context, 7, downloadRequest, PendingIntent.FLAG_UPDATE_CURRENT);
				nextAlarm.set(AlarmManager.RTC_WAKEUP,
						now.getTimeInMillis()+3600*1000,
						nextDownloadRequest);
			}
		}else{
			Log.i("t","CallDownloadService");
			context.startService(new Intent("downloadservice"));
		}
		
	}
}
