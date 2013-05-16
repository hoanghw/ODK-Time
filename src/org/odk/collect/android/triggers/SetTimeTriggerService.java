package org.odk.collect.android.triggers;

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
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

public class SetTimeTriggerService extends Service {
	
	private PowerManager.WakeLock wakeLock;
	private WifiManager.WifiLock wifiLock;
	
	@Override
	public void onCreate(){
		Log.i("t","SetTimeServiceCalled");
		super.onCreate();
		
		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
	    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SetTimeService");

	    WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
	    wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL , "SetTimeSerive");
	    
	    wakeLock.acquire();
	    wifiLock.acquire();
	    
		AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		Map <String,List<Calendar>> triggers = getTimeTrigger(this);
		
		if (triggers == null){
			Log.i("t","SetTimeServiceRetry");
			Utils.retryLater(this,SetTimeTrigger.class,1);
			releaseLocks();
			stopSelf();
			return;
		}
		
		Calendar now = Calendar.getInstance();
		for (Map.Entry<String, List<Calendar>> entry : triggers.entrySet()) {
		    String form = entry.getKey();
		    List<Calendar> calendars = entry.getValue();
			Intent intent = new Intent(this, ExecuteTimeTrigger.class);
			intent.putExtra("form", form);
			
			for (int i = 0; i<calendars.size(); i++)
				if (calendars.get(i).after(now)){
					Calendar calendar = calendars.get(i);
					int id = form.length()*10000+calendar.get(Calendar.HOUR_OF_DAY)*100+calendar.get(Calendar.MINUTE);
					PendingIntent pi = PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
					am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
				}
		}
		Log.i("t","SetTimeServiceDone");
		releaseLocks();
		stopSelf();
	}
	
	//return null if Internet error
	static public Map <String,List<Calendar>> getTimeTrigger(Context context){
		Map<String,List<Calendar>> triggers = new HashMap<String,List<Calendar>>();
		ConnectivityManager connectivityManager =
	            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
	    
		if (ni == null || !ni.isConnected()){
        	return null;
		}
		HttpURLConnection urlConnection = null;
		try{
			URL url = new URL("http://23.23.166.34/gettime/?id=13");
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setConnectTimeout(2000);
			urlConnection.setReadTimeout(2000);
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
	            if ((value != null) && (value.length()!=0))
	            	triggers.put(key, parseCalendars(value));
	        }
		} catch (Exception e) {
			triggers = null;
		}
		finally {
			if (urlConnection != null)
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
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	public void releaseLocks(){
		wakeLock.release();
		wifiLock.release();
	}

}
