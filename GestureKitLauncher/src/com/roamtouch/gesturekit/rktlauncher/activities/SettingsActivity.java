package com.roamtouch.gesturekit.rktlauncher.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.roamtouch.gesturekit.data.GKPreferences;
import com.roamtouch.gesturekit.rktlauncher.auxiliary.KeyConstants;
import com.roamtouch.gesturekit.rktlauncherandroid.R;

public class SettingsActivity extends Activity
{
	public static final String kPrefsSecurityHigh = "RKTisSecurityHigh";
	public static final String kPrefsSound		  = "RKTisSoundOn";
	
	
	private boolean  currentSecurity;
	private CheckBox cbSound;
	private CheckBox cbActiveLockScreen;
	private Button	 btnSetPIN;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		final int checkedIdLowSecurity = R.id.settings_rb_low_security;
		final int checkedIdHighSecurity = R.id.settings_rb_high_security;
		
		btnSetPIN = (Button)findViewById(R.id.settings_btn_set_pin);
		btnSetPIN.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(SettingsActivity.this, EnterPINActivity.class);
				intent.putExtra(EnterPINActivity.kEnterPINTitleKey, "Please Enter a PIN Code");
				intent.putExtra(EnterPINActivity.kEnterPINActivityStageKey, EnterPINActivity.kEnterPINActivityStageCreatePIN);
				startActivity(intent);
			}
		});
		
		RadioButton btnLow = (RadioButton)findViewById(checkedIdLowSecurity);
		RadioButton btnHigh = (RadioButton)findViewById(checkedIdHighSecurity);
				
		boolean isSecurityHigh = GKPreferences.getBoolean(this, SettingsActivity.kPrefsSecurityHigh, false);
		
		if (isSecurityHigh)
		{
			btnHigh.toggle();
		}
		else
		{
			btnLow.toggle();
		}
		
		RadioGroup radioGroup = (RadioGroup)findViewById(R.id.settings_rg);
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId)
			{
				if (checkedId == checkedIdLowSecurity)
				{
					setSecurityToLow();
				}
				else if (checkedId == checkedIdHighSecurity)
				{
					setSecurityToHigh();
				}
			}
		});
		
		
		findViewById(R.id.settings_btn_ok).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (!currentSecurity)
				{
					GKPreferences.put(SettingsActivity.this, SettingsActivity.kPrefsSecurityHigh, false);
					finish();
				}
				// If security is set to high and the user inputed a pin, finish
				else if (currentSecurity && 
						!GKPreferences.getString(SettingsActivity.this, EnterPINActivity.kPrefsCurrentPIN).isEmpty())
				{
					GKPreferences.put(SettingsActivity.this, SettingsActivity.kPrefsSecurityHigh, true);
					finish();
				}
				// If not, prompt him to put a pin
				else
				{
					Intent intent = new Intent(SettingsActivity.this, EnterPINActivity.class);
					intent.putExtra(EnterPINActivity.kEnterPINTitleKey, "You have set the security to high, but did not choose a pin, please Enter a PIN Code");
					intent.putExtra(EnterPINActivity.kEnterPINActivityStageKey, EnterPINActivity.kEnterPINActivityStageCreatePIN);
					startActivity(intent);
				}
			}
		});
				
		cbSound = (CheckBox)findViewById(R.id.settings_checkbox);
		if (GKPreferences.getBoolean(this, kPrefsSound, false))
		{
			cbSound.setChecked(true);
		}
		else
		{
			cbSound.setChecked(false);
		}
		cbSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				if (isChecked)
				{
					GKPreferences.put(SettingsActivity.this, kPrefsSound, true);
				}
				else
				{
					GKPreferences.put(SettingsActivity.this, kPrefsSound, false);
				}
			}
		});
		
		cbActiveLockScreen = (CheckBox)findViewById(R.id.settings_checkbox_start_now);
		if (GKPreferences.getBoolean(this, KeyConstants.kShouldShowLockScreen, false))
		{
			cbActiveLockScreen.setChecked(true);
		}
		else
		{
			cbActiveLockScreen.setChecked(false);
		}
		cbActiveLockScreen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				GKPreferences.put(SettingsActivity.this, KeyConstants.kShouldShowLockScreen, isChecked);
			}
		});
	
	}
	
	private void setSecurityToLow()
	{
		currentSecurity = false;
		GKPreferences.put(this, EnterPINActivity.kPrefsCurrentPIN, "");
		btnSetPIN.setEnabled(false);
		
	}
	
	private void setSecurityToHigh()
	{
		GKPreferences.put(this, EnterPINActivity.kPrefsCurrentPIN, "");
		currentSecurity = true;
		Intent intent = new Intent(SettingsActivity.this, EnterPINActivity.class);
		intent.putExtra(EnterPINActivity.kEnterPINTitleKey, "Please Enter a PIN Code");
		intent.putExtra(EnterPINActivity.kEnterPINActivityStageKey, EnterPINActivity.kEnterPINActivityStageCreatePIN);
		startActivity(intent);
		btnSetPIN.setEnabled(true);
	}
	
}
