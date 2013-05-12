package org.odk.collect.android.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.odk.collect.android.listeners.FormDownloaderListener;
import org.odk.collect.android.listeners.FormListDownloaderListener;
import org.odk.collect.android.logic.FormDetails;
import org.odk.collect.android.tasks.DownloadFormListTask;
import org.odk.collect.android.tasks.DownloadFormsTask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;

public class AutoDownload extends Service implements FormListDownloaderListener, FormDownloaderListener {

	private DownloadFormListTask mDownloadFormListTask;
    private DownloadFormsTask mDownloadFormsTask;
    private HashMap<String, FormDetails> mFormNamesAndURLs = new HashMap<String,FormDetails>();
    
    @Override
	public void onCreate() {
		// TODO Auto-generated method stub
    	super.onCreate();
    	ConnectivityManager connectivityManager =
	            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

	        if (ni == null || !ni.isConnected()) {
	        	stopSelf();
	        	return;
	        } else {
	            mFormNamesAndURLs = new HashMap<String, FormDetails>();
	            if (mDownloadFormListTask != null &&
	            	mDownloadFormListTask.getStatus() != AsyncTask.Status.FINISHED) {
	            	return; // we are already doing the download!!!
	            } else if (mDownloadFormListTask != null) {
	            	mDownloadFormListTask.setDownloaderListener(null);
	            	mDownloadFormListTask.cancel(true);
	            	mDownloadFormListTask = null;
	            }
	            mDownloadFormListTask = new DownloadFormListTask();
	            mDownloadFormListTask.setDownloaderListener(this);
	            mDownloadFormListTask.execute();
	        }
	}

    @Override
	public void formsDownloadingComplete(HashMap<FormDetails, String> result) {
		// TODO Auto-generated method stub
		if (mDownloadFormsTask != null) {
            mDownloadFormsTask.setDownloaderListener(null);
        }
		stopSelf();
	}
	@Override
	public void progressUpdate(String currentFile, int progress, int total) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void formListDownloadingComplete(HashMap<String, FormDetails> result) {
		// TODO Auto-generated method stub
		if (result.containsKey(DownloadFormListTask.DL_AUTH_REQUIRED)) {
            // need authorization
			// refer to FormDownloadList/onCreateDiaglog(AUTH_DIALOG)
			// then call downloadForms() again
            return;
        } else if (result.containsKey(DownloadFormListTask.DL_ERROR_MSG)) {
            // Download failed
            return;
        } else {
            // Everything worked. Clear the list and add the results.
            mFormNamesAndURLs = result;
        }
		downloadAllFiles();
	}
	@SuppressWarnings("unchecked")
	private void downloadAllFiles() {
        ArrayList<FormDetails> filesToDownload = new ArrayList<FormDetails>();
        for (Map.Entry<String, FormDetails> entry : mFormNamesAndURLs.entrySet()) {
		    //String key = entry.getKey();
		    FormDetails value = entry.getValue();
            filesToDownload.add(value);
        }
        mDownloadFormsTask = new DownloadFormsTask();
        mDownloadFormsTask.setDownloaderListener(this);
        mDownloadFormsTask.execute(filesToDownload); 
    }

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
