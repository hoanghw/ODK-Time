package org.odk.collect.android.triggers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.odk.collect.android.listeners.DeleteFormsListener;
import org.odk.collect.android.listeners.FormDownloaderListener;
import org.odk.collect.android.listeners.FormListDownloaderListener;
import org.odk.collect.android.logic.FormDetails;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.tasks.DeleteFormsTask;
import org.odk.collect.android.tasks.DownloadFormListTask;
import org.odk.collect.android.tasks.DownloadFormsTask;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

public class DownloadService extends Service implements FormListDownloaderListener, 
FormDownloaderListener, DeleteFormsListener {
	
	private PowerManager.WakeLock wakeLock;
	private WifiManager.WifiLock wifiLock;
	
	@Override
	public void onCreate(){
		super.onCreate();
		Log.i("t", "DownloadServiceCalled");
		
		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
	    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DownloadService");

	    WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
	    wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL , "DownloadService");
	    
	    wakeLock.acquire();
	    wifiLock.acquire();
		    
		Calendar now = Calendar.getInstance();
		
		if (hasInternet()){
			//Only delete before 4am
			if (now.get(Calendar.HOUR_OF_DAY)<4){
				//After deletion, it will start fetching forms
				deleteAndDownloadForms();
			}else{
				//FetchingForms will kill service when done 
				fetchingForms();
			}
		}else{
			Log.i("t", "DownloadServiceRetry");
			Utils.retryLater(this, DownloadRequest.class, 1);
			releaseLocks();
			stopSelf();
		}
	}
	
	//There must be a better way to check Internet while Airbears not logged in
	static public boolean hasInternet(){
		HttpURLConnection urlConnection = null;
		try{
			URL url = new URL("http://23.23.166.34/gettime/?id=13");
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setConnectTimeout(600);
			urlConnection.setReadTimeout(600);
			String line;
			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			while((line = reader.readLine()) != null) {
				builder.append(line);
			}
			JSONObject json = new JSONObject(builder.toString());
			return true;
		} catch (Exception e) {
			return false;
		}
		finally {
			if (urlConnection != null)
				urlConnection.disconnect();
		}
	}
	public void fetchingForms(){
		Log.i("t", "fetchingFormsCalled");
		
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

	private DownloadFormListTask mDownloadFormListTask;
    private DownloadFormsTask mDownloadFormsTask;
    private HashMap<String, FormDetails> mFormNamesAndURLs = new HashMap<String,FormDetails>();
    
    @Override
	public void formsDownloadingComplete(HashMap<FormDetails, String> result) {
		// TODO Auto-generated method stub
    	Log.i("t", "downloadFormsDone");
		if (mDownloadFormsTask != null) {
            mDownloadFormsTask.setDownloaderListener(null);
        }
		releaseLocks();
		stopSelf();
	}
	@Override
	public void progressUpdate(String currentFile, int progress, int total) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void formListDownloadingComplete(HashMap<String, FormDetails> result) {
		// TODO Auto-generated method stub
		if (result == null){
			Log.i("t", "fetchingFormsDone No Result");
			releaseLocks();
			stopSelf();
			return;
		}
		if (result.containsKey(DownloadFormListTask.DL_AUTH_REQUIRED)) {
            // need authorization
			// refer to FormDownloadList/onCreateDiaglog(AUTH_DIALOG)
			// then call downloadForms() again
			Log.i("t", "fetchingFormsDone DL_AUTH_REQUIRED");
			releaseLocks();
			stopSelf();
            return;
        } else if (result.containsKey(DownloadFormListTask.DL_ERROR_MSG)) {
            // Download failed
        	Log.i("t", "fetchingFormsDone ERROR_MSG" + result.get(DownloadFormListTask.DL_ERROR_MSG));
        	releaseLocks();
        	stopSelf();
            return;
        } else {
            // Everything worked. Clear the list and add the results.
        	Log.i("t", "fetchingFormsDone Success");
            mFormNamesAndURLs = result;
            downloadAllFiles();
        }
	}
	@SuppressWarnings("unchecked")
	private void downloadAllFiles() {
		Log.i("t", "downloadAllFilesCalled");
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
	
	DeleteFormsTask mDeleteFormsTask = null;
	private void deleteAndDownloadForms() {	
		Log.i("t", "deleteFormsCalled");
		Context context = DownloadService.this;
		ArrayList<Long> allForms = new ArrayList<Long>();
		Cursor c = null;
		try{
			ContentResolver cr = context.getContentResolver();
		    c = cr.query(FormsColumns.CONTENT_URI, null, null, null, null);
		    if (c == null) {
	            Log.e("t", "Forms Content Provider returned NULL");
	            return;
	        }
	        c.moveToPosition(-1);
            while (c.moveToNext()) {
            	long k = c.getLong(c.getColumnIndex(FormsColumns._ID));
            	allForms.add(k);
            }
		} catch (Exception e){
			
		} finally { 
			c.close();
		}
		// only start if no other task is running
		if (mDeleteFormsTask == null) {
			mDeleteFormsTask = new DeleteFormsTask();
			mDeleteFormsTask.setContentResolver(getContentResolver());
			mDeleteFormsTask.setDeleteListener(this);
			mDeleteFormsTask.execute(allForms
					.toArray(new Long[allForms.size()]));
		}
	}
	@Override
	public void deleteComplete(int deletedForms) {
		// TODO Auto-generated method stub
		Log.i("t", "deleteFormsDone");
		fetchingForms();
	}
	public void releaseLocks(){
		wakeLock.release();
		wifiLock.release();
	}
}
