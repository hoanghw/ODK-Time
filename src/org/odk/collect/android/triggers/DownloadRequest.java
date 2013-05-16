package org.odk.collect.android.triggers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class DownloadRequest extends BroadcastReceiver {
	private WifiManager.WifiLock wifiLock;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("t","DownloadRequestReceived");
		WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	    wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL , "SetTime");
	    
	    wifiLock.acquire();
	    
	    ConnectivityManager connectivityManager =
	            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
		
		if (ni == null || !ni.isConnected()){
			Log.i("t","downloadRequestRetry");
			Utils.retryLater(context, DownloadRequest.class, 1);
		}else{
			Log.i("t","CallDownloadService");
			context.startService(new Intent("downloadservice"));
		}
		
		wifiLock.release();
	}
}
