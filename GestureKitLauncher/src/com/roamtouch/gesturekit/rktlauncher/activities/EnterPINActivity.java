package com.roamtouch.gesturekit.rktlauncher.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.roamtouch.gesturekit.GestureKit;
import com.roamtouch.gesturekit.data.GKPreferences;
import com.roamtouch.gesturekit.rktlauncher.auxiliary.HomeKeyLocker;
import com.roamtouch.gesturekit.rktlauncherandroid.R;

public class EnterPINActivity extends Activity
{
	public static final String kPrefsCurrentPIN				= "securityCurrentPIN";
	
	public static final String kEnterPINActivityStageKey 	= "EnterPINStage";
	public static final String kEnterPINTitleKey			= "Title";
	public static final String kEnterPINLastPINKey			= "LastPIN";
	
	public static final int kEnterPINActivityStageCreatePIN = 0;
	public static final int kEnterPINActivityStageConfirmPIN = 1;
	public static final int kEnterPINActivityStageUnlockPIN	= 2;
	
	private int stage = 0;
	private String lastPIN;
	private TextView tvTitle;
	
	private EditText etPIN;
	
	private HomeKeyLocker mHomeKeyLocker;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		
		setContentView(R.layout.activity_enterpin);
		
		stage = getIntent().getIntExtra(kEnterPINActivityStageKey, 0);
		lastPIN = getIntent().getStringExtra(kEnterPINLastPINKey);
		
		tvTitle = (TextView)findViewById(R.id.enterpin_tv_title);
		String title = getIntent().getStringExtra(kEnterPINTitleKey);
		tvTitle.setText(title);
		
		etPIN = (EditText)findViewById(R.id.enterpin_et);
		
//		if (stage == kEnterPINActivityStageUnlockPIN)
//		{
//			getHomeKeyLocker().lock(this);
//		}
			
		findViewById(R.id.enterpin_btn_ok).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Log.d("TEST", "STAGE IS: " + stage);
				if (stage == kEnterPINActivityStageCreatePIN)
				{
					
					Intent i = new Intent(EnterPINActivity.this, EnterPINActivity.class);
					i.putExtra(kEnterPINLastPINKey, etPIN.getText().toString());
					i.putExtra(kEnterPINTitleKey, "Please repeat the PIN");
					i.putExtra(kEnterPINActivityStageKey, 1);
					startActivity(i);
					finish();
				}
				else if (stage == kEnterPINActivityStageConfirmPIN)
				{
					if (lastPIN != null && lastPIN.equals(etPIN.getText().toString()))
					{
						GKPreferences.put(EnterPINActivity.this, kPrefsCurrentPIN, etPIN.getText().toString());
												
						// Record the Unlock all gesture
						Intent prgIntentStrict = new Intent(EnterPINActivity.this, StrictModeActivity.class);
						prgIntentStrict.putExtra("systemMethod", "Unlock_all");
						prgIntentStrict.putExtra("packageName", "Unlock");
						prgIntentStrict.putExtra("UIID", GestureKit.getActiveGID());
						prgIntentStrict.putExtra("recordingUnlockAllAgain", true);
						startActivity(prgIntentStrict);
						
						finish();
					}
					else
					{
						tvTitle.setText("The PIN does not match, try again");
						etPIN.setText("");
					}
				}
				else if (stage == kEnterPINActivityStageUnlockPIN)
				{
					// Coming from lock screen compare
					boolean ok = GKPreferences.getString(EnterPINActivity.this, kPrefsCurrentPIN).equals(etPIN.getText().toString());
					
					if (ok)
					{
						setResult(RESULT_OK);
						getHomeKeyLocker().unlock();
						finish();
					}
					else
					{
						tvTitle.setText("The PIN does not match, try again");
						etPIN.setText("");
					}

				}			
				
			}
		});
		
		findViewById(R.id.enterpin_btn_cancel).setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				setResult(RESULT_CANCELED);
				finish();
			}
		});
	}
	
	private HomeKeyLocker getHomeKeyLocker()
	{
		if (mHomeKeyLocker == null)
		{
			mHomeKeyLocker = new HomeKeyLocker();
		}
		
		return mHomeKeyLocker;
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		getHomeKeyLocker().unlock();
	}
	
	

}
