package org.odk.collect.android.receivers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;


//Set alarm notification for today only
public class SetTimeTrigger extends BroadcastReceiver{
	@Override
	public void onReceive(Context context, Intent arg1) {
		// TODO Auto-generated method stub
		Log.i("t","SetTimeReceive");
		
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Map <String,List<Calendar>> triggers = getTimeTrigger(context);
		Calendar now = Calendar.getInstance();
		for (Map.Entry<String, List<Calendar>> entry : triggers.entrySet()) {
		    String form = entry.getKey();
		    List<Calendar> calendars = entry.getValue();
			Intent intent = new Intent(context, ExecuteTimeTrigger.class);
			intent.putExtra("form", form);
			
			for (int i = 0; i<calendars.size(); i++)
				if (calendars.get(i).after(now)){
					Calendar calendar = calendars.get(i);
					int id = form.length()*10000+calendar.get(Calendar.HOUR_OF_DAY)*100+calendar.get(Calendar.MINUTE);
					PendingIntent pi = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
					am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
				}
		}
	}
	static public Map <String,List<Calendar>> getTimeTrigger(Context context){
		Map<String,List<Calendar>> triggers = new HashMap<String,List<Calendar>>();
		ConnectivityManager connectivityManager =
	            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
	    
		if (ni == null || !ni.isConnected()){
			retryLater(context);
        	return triggers;
		}
        	
		HttpURLConnection urlConnection = null;
		try{
			URL url = new URL("http://23.23.166.34/gettime/?id=13");
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setConnectTimeout(2000);
			String line;
			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			while((line = reader.readLine()) != null) {
				builder.append(line);
			}
			
			JSONObject json = new JSONObject(builder.toString());
			
	        Iterator<?> keys = json.keys();
	        String value = null;
	        while( keys.hasNext() ){
	            String key = (String) keys.next();
	            value =(String) json.getString(key);
	            if ((value != null) && value.length()!=0)
	            	triggers.put(key, parseCalendars(value));
	        }
		} catch (Exception e) {
			retryLater(context);
		}
		finally {
			urlConnection.disconnect();
		}
		return triggers;
	}
	static public List<Calendar> parseCalendars(String s){
		String[] time=s.split(" ");
		List<Calendar> calendars = new ArrayList<Calendar>();
		for (int i=0;i<time.length;i++){
			Calendar calendar= Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[i].substring(0,2)));
			calendar.set(Calendar.MINUTE, Integer.parseInt(time[i].substring(2)));
			calendar.set(Calendar.SECOND, 0);
			calendars.add(calendar);
		}	
		return calendars;
	}
	static public void retryLater(Context context){
		Log.i("t","SetTimeRetry");
		
		Calendar todayEnd = Calendar.getInstance();
		todayEnd.set(Calendar.HOUR_OF_DAY, 22);
		todayEnd.set(Calendar.MINUTE, 59);
		todayEnd.set(Calendar.SECOND, 59);
		
		Calendar now = Calendar.getInstance();
		
		if (now.before(todayEnd)){
			AlarmManager nextAlarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			Intent downloadRequest = new Intent(context, SetTimeTrigger.class);
			PendingIntent nextCheckRequest = PendingIntent.getBroadcast(context, 6, downloadRequest, PendingIntent.FLAG_UPDATE_CURRENT);
			nextAlarm.set(AlarmManager.RTC_WAKEUP,
					now.getTimeInMillis()+3600*1000,
					nextCheckRequest);
		}
	}
}
