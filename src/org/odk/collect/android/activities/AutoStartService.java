package org.odk.collect.android.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AutoStartService extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		 Intent startServiceIntent = new Intent(context, ServiceManager.class);
	     context.startService(startServiceIntent);
	}
}
