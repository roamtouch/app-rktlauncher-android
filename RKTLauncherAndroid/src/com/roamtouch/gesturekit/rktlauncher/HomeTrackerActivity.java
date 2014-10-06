package com.roamtouch.gesturekit.rktlauncher;

import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.roamtouch.gesturekit.data.GKPreferences;
import com.roamtouch.gesturekit.rktlauncher.activities.TutorialActivity;
import com.roamtouch.gesturekit.rktlauncher.adapters.ApplicationListAdapter;
import com.roamtouch.gesturekit.rktlauncher.receiver.ScreenReceiver;

/**
 * Custom home screen of device to deal with home button.
 * 
 * LockerActivity, PasswordActivity
 * 
 * @author pooja
 * 
 * @version 1.0
 */
public class HomeTrackerActivity extends Activity
{
	/** Called when the activity is first created. */
	private final static String SELECTED_HOMESCREEN_PREF_FILE = "selected_homescreen_file";
	private final static String SELECTED_HOMESCREEN_KEY = "selected_homescreen";

	private final static int LIST_ID = 123;

	protected final static int APP_SELECTOR_DIALOG = 1;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		super.onCreate(savedInstanceState);				
		
		Intent serviceIntent = new Intent(this, ScreenReceiver.class);
		startService(serviceIntent);
	}

	@Override
	protected void onResume()
	{		
		super.onResume();
		if (!launchDefHomescreen())
		{
			this.showDialog(APP_SELECTOR_DIALOG);
		}else{
			if(LockScreenActivity.isInit()){
				Intent i = new Intent(HomeTrackerActivity.this, LockScreenActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				startActivity(i);
				overridePendingTransition(0, 0);
			}
			this.finish();
		}
	}
	
	/**
	 * Get intent of device's home screen to handle home button's pressed event.
	 * 
	 * @author pooja
	 * 
	 * @return intent of device's home screen.
	 */
	private Intent getHomeIntent()
	{
		Intent startMain = new Intent(Intent.ACTION_MAIN);
		startMain.addCategory(Intent.CATEGORY_HOME);
		startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		return startMain;
	}

	@Override
	protected Dialog onCreateDialog(int id)
	{
		if (id == APP_SELECTOR_DIALOG)
		{
			return getAppSelectorDialog();
		}

		return super.onCreateDialog(id);
	}

	private Dialog getAppSelectorDialog()
	{
		final Dialog dialog = new Dialog(this);		
		
		dialog.setOnCancelListener(new OnCancelListener()
		{			
			@Override
			public void onCancel(DialogInterface dialog)
			{
				HomeTrackerActivity.this.finish();
			}
		});
		dialog.setTitle("Select launcher for RKT");
		final ListView lstApplications = new ListView(this);
		lstApplications.setId(LIST_ID);
		lstApplications.setAdapter(new ApplicationListAdapter(this));
		lstApplications.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				
				final ResolveInfo info = ((ApplicationListAdapter) arg0.getAdapter()).getItem(arg2);
				setSelectedHomescreenPackage(info);
				HomeTrackerActivity.this.dismissDialog(APP_SELECTOR_DIALOG);
				
				GKPreferences.put(HomeTrackerActivity.this, "InitialTutorial", true);
				
				// Reopen Tutorial Activity again. 
				Intent tutorialIntent = new Intent(getApplicationContext(), TutorialActivity.class);
				tutorialIntent.putExtra("step1", true);
				tutorialIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getApplicationContext().startActivity(tutorialIntent);	
				
				
				
			}
		});
		dialog.setContentView(lstApplications);
		return dialog;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog)
	{
		if (id == APP_SELECTOR_DIALOG)
		{
			final ListView lstApplications = (ListView) dialog.findViewById(LIST_ID);
			final ApplicationListAdapter adapter = (ApplicationListAdapter) lstApplications.getAdapter();
			adapter.setItems(getHomeActivityes());
			adapter.notifyDataSetChanged();
			return;
		}
		super.onPrepareDialog(id, dialog);
	}

	/**
	 * Get list of application that contains device's custom home screen.
	 * 
	 * @author pooja
	 * 
	 * @return list of application that contains device's custom home screen.
	 */
	private List<ResolveInfo> getHomeActivityes()
	{
		final List<ResolveInfo> list = this.getPackageManager().queryIntentActivities(getHomeIntent(), PackageManager.MATCH_DEFAULT_ONLY);
		final String packageName = this.getPackageName();
		for (ResolveInfo info : list)
		{
			if (info.activityInfo.applicationInfo.packageName.contentEquals(packageName))
			{
				list.remove(info);
				break;
			}
		}
		return list;
	}

	public static void clearResetHomescreenSelection(Context theContext)
	{
		theContext.getSharedPreferences(SELECTED_HOMESCREEN_PREF_FILE, Context.MODE_PRIVATE).edit().clear().commit();
	}

	/**
	 * Save current home screen's information into shared preference.
	 * 
	 * @author pooja
	 * 
	 * @param info see ResolveInfo for more information.
	 */
	protected void setSelectedHomescreenPackage(ResolveInfo info)
	{
		final Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setComponent(new ComponentName(info.activityInfo.applicationInfo.packageName, info.activityInfo.name));

		this.getSharedPreferences(SELECTED_HOMESCREEN_PREF_FILE, Context.MODE_PRIVATE).edit().putString(SELECTED_HOMESCREEN_KEY, intent.toURI())
				.commit();
		this.finish();
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
	}
	
	/**
	 * Check for current activity
	 * 
	 * @return boolean if current activity is LockerActivity or PasswordActivity this will return true.
	 */
	public boolean launchDefHomescreen()
	{
		Log.d("Tag", "Locker Init : "+LockScreenActivity.isInit());
		if(LockScreenActivity.isInit())
		{
			return true;
		}
		final String intentURI = this.getSharedPreferences(SELECTED_HOMESCREEN_PREF_FILE, Context.MODE_PRIVATE).getString(SELECTED_HOMESCREEN_KEY,
				null);

		if (intentURI != null)
		{
			Intent intent;
			try
			{
				intent = Intent.parseUri(intentURI, 0);
				startActivity(intent);				
				return true;
			} catch (Exception e)
			{				
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}
	
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_HOME:
				return true;
			case KeyEvent.KEYCODE_MENU:
					return true;
			case KeyEvent.KEYCODE_BACK:
				return true;
			}
		} else if (event.getAction() == KeyEvent.ACTION_UP) {
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_HOME:
				return true;
			case KeyEvent.KEYCODE_MENU:
				return true;
			case KeyEvent.KEYCODE_BACK:
				return true;
			}
		}
		return super.dispatchKeyEvent(event);
	}
}