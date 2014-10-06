package com.roamtouch.gesturekit.rktlauncher.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.roamtouch.gesturekit.rktlauncherandroid.R;

public class StrictModeActivity extends Activity
{
	String name;
	String packageName;
	String guid;
	
	boolean strictModeEnforced;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_strictmode);
		
		Intent prgIntent = getIntent();
		
		name = prgIntent.getStringExtra("name");
		packageName = prgIntent.getStringExtra("packageName");
		guid = prgIntent.getStringExtra("UIID");
		
		CheckBox cbStrict = (CheckBox)findViewById(R.id.strict_checkbox);
		cbStrict.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				strictModeEnforced = isChecked;
			}
		});
		
		final String systemMethod = getIntent().getStringExtra("systemMethod");
		final boolean recordingUnlockAgain = getIntent().getBooleanExtra("recordingUnlockAllAgain", false);
		
		Button btnOk = (Button)findViewById(R.id.strict_btn_ok);
		btnOk.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent prgIntent = new Intent(StrictModeActivity.this, RKTGesturesRecordingActivity.class);
				prgIntent.putExtra("name", name);
				prgIntent.putExtra("packageName", packageName);
				prgIntent.putExtra("UIID", guid);
				prgIntent.putExtra("systemMethod", systemMethod);
				prgIntent.putExtra("strictMode", strictModeEnforced);
				prgIntent.putExtra("recordingUnlockAllAgain", recordingUnlockAgain);
				
				startActivity(prgIntent);
				finish();								
			}
		});
	}
	
}
