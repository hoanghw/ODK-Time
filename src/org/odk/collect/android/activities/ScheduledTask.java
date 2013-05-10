package org.odk.collect.android.activities;

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
import android.net.Uri;

public class ScheduledTask extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent arg1) {
		// TODO Auto-generated method stub
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Map <String,List<Calendar>> triggers = getTimeTrigger();
		Calendar now = Calendar.getInstance();
		for (Map.Entry<String, List<Calendar>> entry : triggers.entrySet()) {
		    String form = entry.getKey();
		    List<Calendar> calendars = entry.getValue();
			Intent intent = new Intent(context, SetNotification.class);
			intent.putExtra("form", form);
			
			for (int i = 0; i<calendars.size(); i++)
				if (calendars.get(i).after(now)){
					Calendar calendar = calendars.get(i);
					intent.setData(Uri.parse("custom://"+form+'_'+calendar.getTimeInMillis()));
					PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
					am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
				}
		}
	}
	static public Map <String,List<Calendar>> getTimeTrigger(){
		Map<String,List<Calendar>> triggers = new HashMap<String,List<Calendar>>();
		HttpURLConnection urlConnection = null;
		try{
			URL url = new URL("http://23.23.166.34/gettime/?id=13");
			urlConnection = (HttpURLConnection) url.openConnection();
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
	            	triggers.put(key, getCalendars(value));
	        }
		} catch (Exception e) {
			triggers.put("mobile_survey_21c",getCalendars("1200 1700"));
		}
		finally {
		     urlConnection.disconnect();
		}

		return triggers;
	}
	static public List<Calendar> getCalendars(String s){
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
	
}
