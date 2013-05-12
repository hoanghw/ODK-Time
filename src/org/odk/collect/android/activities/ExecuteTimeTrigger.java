package org.odk.collect.android.activities;

import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

public class ExecuteTimeTrigger extends BroadcastReceiver {
	public static final int NOTIFICATION_ID = 1234;
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		int icon = android.R.drawable.star_on;
		CharSequence tickerText = "A Friendly Reminder from QT";
		long when = System.currentTimeMillis();
		
		Notification notification = new Notification(icon, tickerText, when);
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		notification.defaults = Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE;
		
		CharSequence contentTitle = "A Friendly Reminder from QT";
		String form = intent.getExtras().getString("form");
		CharSequence contentText = "Please fill out form: "+form;
			
		Intent notificationIntent = getFormEntryIntent(context,form);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		notificationManager.notify(NOTIFICATION_ID, notification);
	}
	
	//consider default form?
	static Intent getFormEntryIntent(Context context, String s){
		Intent intent;
		String where=FormsColumns.JR_FORM_ID+" = '"+s+"'";
		ContentResolver cr = context.getContentResolver();
	    Cursor c = cr.query(FormsColumns.CONTENT_URI, null,where, null, null);
	    if (c.moveToFirst()){
	    	long idFormsTable = c.getLong(c.getColumnIndex("_id"));
	    	Uri formUri = ContentUris.withAppendedId(FormsColumns.CONTENT_URI, idFormsTable);
	    	intent = new Intent(Intent.ACTION_EDIT, formUri);
	    }else{
	    	intent = new Intent(context, FormChooserList.class);
	    }
	    c.close();
	    return intent;
	}
}
