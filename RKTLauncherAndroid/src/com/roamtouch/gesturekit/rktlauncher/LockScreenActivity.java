package com.roamtouch.gesturekit.rktlauncher;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;

import com.roamotuch.gesturekit.plugin.GestureKitPlugins;
import com.roamotuch.gesturekit.plugin.PluginInterface;
import com.roamtouch.gesturekit.GestureKit;
import com.roamtouch.gesturekit.GestureKit.GestureKitListener;
import com.roamtouch.gesturekit.data.GKPreferences;
import com.roamtouch.gesturekit.rktlauncher.activities.EnterPINActivity;
import com.roamtouch.gesturekit.rktlauncher.activities.ProgramsGesturesActivity;
import com.roamtouch.gesturekit.rktlauncher.activities.SettingsActivity;
import com.roamtouch.gesturekit.rktlauncher.activities.TutorialActivity;

public class LockScreenActivity extends Activity implements OnClickListener
{
	public static int		counter			= 0;
	public boolean			isHomePressed	= false;
	private HomeKeyLocker	mHomeKeyLocker;
	public boolean			isNormalQuit	= false;
	Button					btn;
	
	boolean						inDragMode;
	int							selectedImageViewX;
	int							selectedImageViewY;

	int							home_x, home_y;
	int[]						droidpos;

	private GestureKit	gestureKit;
	private GestureKitLauncherLayout launcher;
	private HomeTrackerActivity tracker;
	

	private Activity	mActivity;
	
	private BroadcastReceiver	receiver;
	
	private String helparray;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		getWindow()
				.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		LockScreenActivity.counter = 1;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mHomeKeyLocker = new HomeKeyLocker();
		mHomeKeyLocker.lock(this);

		// Store package name
		GKPreferences.put(getApplicationContext(),
				"gk_application_gid",
				((RKTApplication) this.getApplication()).getGid());

		helparray = ((RKTApplication) this.getApplication()).getGid() + "_help_array";

		this.mActivity = this;

		setContentView(R.layout.main);

		Intent i = this.getIntent();

		/**
		 * REMOVE ME
		 * 
		 * Critical bug fix. When someone installing, closing the app before
		 * unlocking otherwise GestureKit core handles.
		 */
		// Package Name
		String PACKAGE_NAME = getApplicationContext().getPackageName().toString();
		// Store local for service
		GKPreferences.put(getApplicationContext(), PACKAGE_NAME + "_local", true);

		/**
				 */
		//
		if (i.getAction() == null)
		{
			this.gestureKit = new GestureKit(this, ((RKTApplication) this.getApplication()).getGid(), null, true);
			launcher = new GestureKitLauncherLayout(this, this.gestureKit);
			this.gestureKit.setPlugin((PluginInterface) launcher);

			// Implement GestureAction Plugin for taking selfie.
			this.gestureKit.setGestureKitListener(new GestureKitListener()
			{
				@Override
				public void onGestureKitLoaded()
				{
					GestureKitPlugins.getInstance().setAction(new GestureActionTakeSelfie(gestureKit, mActivity), "Selfie");
				};
			});
		}
		else
		{

			// Loads System gestures from JSON files.
			loadLocalJSON();

			Log.d("ACTION", "ACTION: " + i.getAction());
			boolean isItTheFirstTimeRunningTheApp = !GKPreferences.getBoolean(this,
					TutorialActivity.kDidRunApplicationBefore,
					false);
			if (isItTheFirstTimeRunningTheApp)
			{
				startTutorialActivity();
			}
			else
			{
				StartProgramActivity();
			}
		}
		
		tracker = new HomeTrackerActivity();

	}
	
public void loadLocalJSON(){
		
		try {
			
			String local_gesture_in_cache = GKPreferences.getString(this, "local_gesture_stored");	
						
			if (!local_gesture_in_cache.equals("true")){
				
				try {
		 	    	String check = GKPreferences.getString(this, ((RKTApplication) this.getApplication()).getGid());
		 	    	if ( check.equals("") )
		 	    		initDataSet();
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
			
				// LOAD HELP SAMPLES 
				JSONObject help_gesture_sample_1 = new JSONObject(loadFile("help_sample_1.json"));
				JSONObject help_gesture_sample_2 = new JSONObject(loadFile("help_sample_2.json"));
				JSONObject help_gesture_sample_3 = new JSONObject(loadFile("help_sample_3.json"));
				
				// LOAD LOCK SAMPLES 
				JSONObject unlock_gesture_sample_1 = new JSONObject(loadFile("unlock_sample_1.json"));
				JSONObject unlock_gesture_sample_2 = new JSONObject(loadFile("unlock_sample_2.json"));
				JSONObject unlock_gesture_sample_3 = new JSONObject(loadFile("unlock_sample_3.json"));
				
				// LOAD SELFIE SAMPLE 
				JSONObject selfie_gesture_sample_1 = new JSONObject(loadFile("selfie_sample_1.json"));
				JSONObject selfie_gesture_sample_2 = new JSONObject(loadFile("selfie_sample_2.json"));
				JSONObject selfie_gesture_sample_3 = new JSONObject(loadFile("selfie_sample_3.json"));
				
				// STORED GESTURES
				String s_stored = GKPreferences.getString(this, ((RKTApplication) this.getApplication()).getGid() );									
				JSONObject s_all = new JSONObject(s_stored);
				JSONArray s_gestures = s_all.getJSONObject("gestureset").getJSONArray("gestures");		
				
				s_gestures.put(help_gesture_sample_1);		
				s_gestures.put(help_gesture_sample_2);		
				s_gestures.put(help_gesture_sample_3);	
				
				s_gestures.put(unlock_gesture_sample_1);		
				s_gestures.put(unlock_gesture_sample_2);		
				s_gestures.put(unlock_gesture_sample_3);
				
				s_gestures.put(selfie_gesture_sample_1);		
				s_gestures.put(selfie_gesture_sample_2);		
				s_gestures.put(selfie_gesture_sample_3);
				
				// COMMIT GESTURES
				GKPreferences.put(this, ((RKTApplication) this.getApplication()).getGid(), s_all.toString());	
								
				// LOAD HELP SAMPLES 
				JSONObject help_draw = new JSONObject(loadFile("help_image.json"));
				JSONObject unlock_draw = new JSONObject(loadFile("unlock_image.json"));
				JSONObject selfie_draw = new JSONObject(loadFile("selfie_image.json"));		
							
				// DRAW IMAGESW
				String s_storedraw = GKPreferences.getString(this, helparray);									
				JSONObject s_alldraw = new JSONObject(s_storedraw);
				JSONArray s_gesturesdraw = s_alldraw.getJSONObject("gestureset").getJSONArray("gestures");		
				
				s_gesturesdraw.put(help_draw);		
				s_gesturesdraw.put(unlock_draw);
				s_gesturesdraw.put(selfie_draw);
								
				// COMMIT IMAGES
				GKPreferences.put(this, helparray, s_alldraw.toString());
				
				GKPreferences.put(this, "local_gesture_stored", "true");	
				
			}
			
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	
	private void initDataSet() throws JSONException {	
		
		JSONObject n_set = new JSONObject();
		JSONObject n_gestureset = new JSONObject();
		n_gestureset.put("gestureset_name", "rktlauncher");
		n_gestureset.put("gid", ((RKTApplication) this.getApplication()).getGid());			
		n_gestureset.put("gestures", new JSONArray());		
		
		n_set.put("gestureset", n_gestureset);
		
		// STORE GESTURESET HEADER			
		GKPreferences.put(this, ((RKTApplication) this.getApplication()).getGid(), n_set.toString());
		
		// STORE HELP HEADER
		GKPreferences.put(this, helparray, n_set.toString());			
	}
	
	private String loadFile(String assetName){
		Reader reader = null;
		try{
			InputStream is = this.getApplicationContext().getAssets().open(assetName);
			reader = new InputStreamReader(is);

			StringBuffer string = new StringBuffer();
			char[]buffer = new char[1024];
			int count = 0;
			while((count = reader.read(buffer)) != -1)
				string.append(new String(buffer, 0, count));
			reader.close();

			return string.toString();

		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public void Help(){
		Log.v("","");
	}
	
	private void startTutorialActivity()
	{
		Intent programIntent = new Intent(this, TutorialActivity.class);

		finish();
		this.startActivity(programIntent);
	}

	public void StartProgramActivity()
	{
		Intent programIntent = new Intent(this, ProgramsGesturesActivity.class);

		finish();
		this.startActivity(programIntent);

	}	


	@Override
	public void onClick(View v)
	{
		if (v.getId() == R.id.btnclose)
		{
			Log.d("Tag", "Unlock Screen");
			mHomeKeyLocker.unlock();
			isNormalQuit = true;
			finish();
		}
	}

	@Override
	protected void onStop()
	{
		super.onStop();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		LockScreenActivity.counter = 1;
		
		if (launcher != null)
		{
			launcher.onResume();
		}
		
		if (gestureKit != null)
		{
			gestureKit.onResume();
		}

		if (receiver == null)
		{
			receiver = new BroadcastReceiver()
			{
				@Override
				public void onReceive(Context context, Intent intent)
				{					
					if (intent.getAction() == "UNLOCK_ALL")
					{
//						getHomeKeyLocker().unlock();
						isNormalQuit = true;
						forceDefHomescreen();
						finish();
						
					}
					else if (intent.getAction() == "UNLOCK_SCREEN")
					{
//						getHomeKeyLocker().unlock();
						isNormalQuit = true;
						finish();
					}
					else if (intent.getAction() == "HELP_RECOGNIZED")
					{
						isNormalQuit = true;
						boolean isSecurityHigh = GKPreferences.getBoolean(LockScreenActivity.this, SettingsActivity.kPrefsSecurityHigh, false) &&
								!GKPreferences.getString(LockScreenActivity.this, EnterPINActivity.kPrefsCurrentPIN).isEmpty();
												
						if (!isSecurityHigh)
						{
//							getHomeKeyLocker().unlock();
							StartProgramActivity();
						}
						else
						{
							Intent settingsIntent = new Intent(LockScreenActivity.this, EnterPINActivity.class);
							settingsIntent.putExtra(EnterPINActivity.kEnterPINTitleKey, "Please Enter the PIN Code to unlock.");
							settingsIntent.putExtra(EnterPINActivity.kEnterPINActivityStageKey, EnterPINActivity.kEnterPINActivityStageUnlockPIN);
							startActivityForResult(settingsIntent, 500);

						}
					}
				}
			};
		}

		IntentFilter filter = new IntentFilter();
		filter.addAction("VANISH_PARTICLES");
		filter.addAction("UNLOCK_SCREEN");
		filter.addAction("HELP_RECOGNIZED");
		filter.addAction("UNLOCK_ALL");

		registerReceiver(receiver, filter);
	}
	
	private final static String SELECTED_HOMESCREEN_PREF_FILE = "selected_homescreen_file";
	private final static String SELECTED_HOMESCREEN_KEY = "selected_homescreen";
	
	public boolean forceDefHomescreen()
	{
		String intentURI = this.getSharedPreferences(SELECTED_HOMESCREEN_PREF_FILE, Context.MODE_PRIVATE).getString(SELECTED_HOMESCREEN_KEY,
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
	protected void onDestroy()
	{
		super.onDestroy();
		LockScreenActivity.counter = 0;
		mHomeKeyLocker.unlock();
		mHomeKeyLocker = null;
	}

	public static boolean isInit()
	{
		return LockScreenActivity.counter > 0;
	}

	@Override
	public void onPause()
	{
		super.onPause();
		
		if (launcher != null)
		{
			launcher.onDestroy();
		}
					
		if (receiver != null)
		{
			unregisterReceiver(receiver);
		}
		
		boolean isSecurityHigh = GKPreferences.getBoolean(LockScreenActivity.this, SettingsActivity.kPrefsSecurityHigh, false) &&
				!GKPreferences.getString(LockScreenActivity.this, EnterPINActivity.kPrefsCurrentPIN).isEmpty();
		
//    	}
		
		if (!isNormalQuit && isSecurityHigh)
		{
			Intent i = new Intent(this, LockScreenActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(i);
		}
	}

	@Override
	public void onBackPressed()
	{
		Log.d("Tag", "Locker Init : KEYCODE_BACK");
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		// PIN 
		if (requestCode == 500)
		{
			if (resultCode == RESULT_OK)
			{
				mHomeKeyLocker.unlock();
				finish();
			}
		}
	}

}
