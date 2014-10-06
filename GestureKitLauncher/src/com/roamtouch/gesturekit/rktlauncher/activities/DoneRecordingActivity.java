package com.roamtouch.gesturekit.rktlauncher.activities;

import com.roamtouch.gesturekit.rktlauncherandroid.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class DoneRecordingActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_done_recording);
		
		findViewById(R.id.done_btn_ok).setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				finish();
			}
		});
	}
}
