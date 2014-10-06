package com.roamtouch.gesturekit.rktlauncher.activities;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.roamtouch.gesturekit.rktlauncherandroid.R;

public class AboutActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		TextView tvGkLink = (TextView)findViewById(R.id.about_tv_gesturekit_link);
		tvGkLink.setMovementMethod(LinkMovementMethod.getInstance());
		
		TextView tvRmLink = (TextView)findViewById(R.id.about_tv_roamtouch_link);
		tvRmLink.setMovementMethod(LinkMovementMethod.getInstance());
		
		findViewById(R.id.about_btn_close).setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				finish();
			}
		});
	}

}
