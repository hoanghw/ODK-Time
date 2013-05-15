package org.odk.collect.android.activities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

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
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

public class DownloadService extends Service implements FormListDownloaderListener, 
FormDownloaderListener, DeleteFormsListener {
	@Override
	public void onCreate(){
		super.onCreate();
		Log.i("t", "DownloadServiceCalled");
		
		Calendar now = Calendar.getInstance();
		//Only delete before 4am
		if (now.get(Calendar.HOUR_OF_DAY)<4){
			//After deletion, it will start fetching forms
			deleteForms();
		}else{
			fetchingForms();
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
		if (result == null){
			stopSelf();
			return;
		}
		if (result.containsKey(DownloadFormListTask.DL_AUTH_REQUIRED)) {
            // need authorization
			// refer to FormDownloadList/onCreateDiaglog(AUTH_DIALOG)
			// then call downloadForms() again
			stopSelf();
            return;
        } else if (result.containsKey(DownloadFormListTask.DL_ERROR_MSG)) {
            // Download failed
        	stopSelf();
            return;
        } else {
            // Everything worked. Clear the list and add the results.
            mFormNamesAndURLs = result;
            downloadAllFiles();
        }
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
	
	DeleteFormsTask mDeleteFormsTask = null;
	private void deleteForms() {	
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
}
