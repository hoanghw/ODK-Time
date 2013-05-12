package org.odk.collect.android.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DownloadTask extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent startServiceIntent = new Intent(context, AutoDownload.class);
	    context.startService(startServiceIntent);
	}
}
