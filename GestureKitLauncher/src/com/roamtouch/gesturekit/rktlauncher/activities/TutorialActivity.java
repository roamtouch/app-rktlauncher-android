package com.roamtouch.gesturekit.rktlauncher.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.roamtouch.gesturekit.data.GKPreferences;
import com.roamtouch.gesturekit.rktlauncher.auxiliary.KeyConstants;
import com.roamtouch.gesturekit.rktlauncherandroid.R;

public class TutorialActivity extends Activity
{
	public static final String kDidRunApplicationBefore = "RKTDidRunApplicationBefore";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tutorial);
		
		boolean isFirst = false;		
		boolean isStep1 = false;
		
		Intent intent = getIntent();
		
		if (intent.hasExtra("first"))
			isFirst = intent.getExtras().getBoolean("first");		
		
		if (intent.hasExtra("step1"))
			isStep1 = intent.getExtras().getBoolean("step1");			
		
		
		if (isFirst){
			
			ImageView gkimage = (ImageView)findViewById(R.id.gk_tutorial_iv_logo); 
			gkimage.setVisibility(View.VISIBLE);
					
			TextView tvTutorialLock = (TextView)findViewById(R.id.tutorial_lock);			
			tvTutorialLock.setVisibility(View.VISIBLE);
			setStyledHtmlTextToTextView(R.string.tutorial_tv_activate_lock, tvTutorialLock);	
			
			ImageView lockimage = (ImageView)findViewById(R.id.tutorial_lock_image); 
			lockimage.setVisibility(View.VISIBLE);	
			
			TextView tvActivateLauncher = (TextView)findViewById(R.id.tutorial_tv_activate_launcher);			
			tvActivateLauncher.setVisibility(View.VISIBLE);					
			
			ImageView launcherimage = (ImageView)findViewById(R.id.tutorial_launcher_image);			
			launcherimage.setVisibility(View.VISIBLE);			
			
		} else if (isStep1) {
			
			
			ImageView rktimage = (ImageView)findViewById(R.id.rkt_tutorial_iv_logo); 
			rktimage.setVisibility(View.VISIBLE);
			
			TextView tvDone = (TextView)findViewById(R.id.tutorial_tv_done);
			tvDone.setVisibility(View.VISIBLE);
			setStyledHtmlTextToTextView(R.string.tutorial_tv_done, tvDone);
			
			TextView tvSettigns = (TextView)findViewById(R.id.tutorial_tv_settings);
			tvSettigns.setVisibility(View.VISIBLE);
			setStyledHtmlTextToTextView(R.string.tutorial_tv_settings, tvSettigns);
			
			TextView tvTutorialTutorial = (TextView)findViewById(R.id.tutorial_tv_tutorial);
			tvTutorialTutorial.setVisibility(View.VISIBLE);
			setStyledHtmlTextToTextView(R.string.tutorial_tv_tutorial, tvTutorialTutorial);
			
			//TextView tvTutorialLowSecurity = (TextView)findViewById(R.id.tutorial_tv_lowsecurity);		
			//tvTutorialLowSecurity.setVisibility(View.VISIBLE);
			//setStyledHtmlTextToTextView(R.string.tutorial_tv_low_security, tvTutorialLowSecurity);
						
			//TextView tvTutorialHeightSecurity = (TextView)findViewById(R.id.tutorial_tv_height_security);		
			//tvTutorialHeightSecurity.setVisibility(View.VISIBLE);
			//setStyledHtmlTextToTextView(R.string.tutorial_tv_low_security, tvTutorialHeightSecurity);
			
			RelativeLayout startLock = (RelativeLayout)findViewById(R.id.tutorial_rl_check_start_now);
			startLock.setVisibility(View.VISIBLE);
			
			RelativeLayout rememberTutorialLock = (RelativeLayout)findViewById(R.id.tutorial_rl_check);
			rememberTutorialLock.setVisibility(View.VISIBLE);
			
			Button nextButton = (Button)findViewById(R.id.tutorial_btn_next);
			nextButton.setVisibility(View.VISIBLE);
			
			nextButton.setOnClickListener(new View.OnClickListener()
			{
				
				@Override
				public void onClick(View v)
				{
					Intent programIntent = new Intent(TutorialActivity.this, ProgramsGesturesActivity.class);
					
					finish();
					startActivity(programIntent);
				}
			});
			
			CheckBox cbStrict = (CheckBox)findViewById(R.id.tutorial_checkbox);
			if (GKPreferences.getBoolean(this, kDidRunApplicationBefore, false))
			{
				cbStrict.setChecked(true);
			}
			else
			{
				cbStrict.setChecked(false);
			}
			cbStrict.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
			{
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				{
					GKPreferences.put(TutorialActivity.this, kDidRunApplicationBefore, isChecked);
				}
			});
			
			CheckBox cbStartLock = (CheckBox)findViewById(R.id.tutorial_checkbox_start_now);
			cbStartLock.setChecked(true);
			GKPreferences.put(TutorialActivity.this, KeyConstants.kShouldShowLockScreen, true);
			/*if (GKPreferences.getBoolean(this, KeyConstants.kShouldShowLockScreen, false))
			{
				cbStartLock.setChecked(true);
			}
			else
			{
				cbStartLock.setChecked(false);
			}*/
			
			cbStartLock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
			{
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				{
					GKPreferences.put(TutorialActivity.this, KeyConstants.kShouldShowLockScreen, isChecked);
				}
			});
			
			
		} 
		
		
		
		
	}
	
	private void setStyledHtmlTextToTextView(int stringRes, TextView textView)
	{
		CharSequence styledText = Html.fromHtml(getString(stringRes));
		textView.setText(styledText);
	}
	
}
