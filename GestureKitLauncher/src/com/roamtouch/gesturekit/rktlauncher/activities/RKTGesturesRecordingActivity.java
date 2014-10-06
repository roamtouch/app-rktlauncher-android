package com.roamtouch.gesturekit.rktlauncher.activities;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.roamtouch.gesturekit.GestureKit;
import com.roamtouch.gesturekit.data.GKPreferences;
import com.roamtouch.gesturekit.data.GestureSet;
import com.roamtouch.gesturekit.recording.RKTGestureViewListener;
import com.roamtouch.gesturekit.recording.RKTGesturesView;
import com.roamtouch.gesturekit.rktlauncher.particles.ParticleViewTexture;
import com.roamtouch.gesturekit.rktlauncherandroid.R;


@SuppressLint("NewApi")
public class RKTGesturesRecordingActivity extends Activity implements RKTGestureViewListener {

	private static final String kTimesRecordedKey = "RTKNumberOfTimesRecorded";
	
	private ParticleViewTexture particleViewTexture; 

	SparseArray<List<PointF>> mActivePointers;
	SparseArray<List<PointF>> mFinishedPoints;
	
	public Context mContext;
	
	Path mLoaded = new Path();
	
	RKTGesturesView gestureview;
	
	RelativeLayout rlMain;
	TextView tv;
	
	String systemMethod;
	String method;
	String packageName;
	String guid;
	Intent programIntent;
	
	int 	timesRecordedBefore;
	
	public GestureSet gestureSet;
	
	private String helparray;
	
	public RKTGesturesRecordingActivity(Context context) {
		super();
		mContext = context;		
		helparray = GestureKit.getGID() + "_help_array";
	}
	
	public RKTGesturesRecordingActivity(){
		
	}
		
	@Override
	public void onCreate(Bundle savedInstanceState) {

 	    super.onCreate(savedInstanceState);   
 			 	    
 	   // Init preferences
 		GKPreferences.prefs(this);
 		gestureSet = new GestureSet(); 
 		
 	    try {
 	    	String check = GKPreferences.getString(RKTGesturesRecordingActivity.this, GestureKit.getGID());
 	    	if ( check.equals("") )
 	    		initDataSet();
			
		} catch (JSONException e) {
			e.printStackTrace();
		}   
               
		rlMain = new RelativeLayout(this);
        
        ViewGroup.LayoutParams rlMain_Params = new ViewGroup.LayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        
        ViewGroup.LayoutParams gestureView_params = new ViewGroup.LayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        
        RelativeLayout.LayoutParams tv_params = new RelativeLayout.LayoutParams(new ViewGroup.LayoutParams(
        		RelativeLayout.LayoutParams.MATCH_PARENT,
        		RelativeLayout.LayoutParams.WRAP_CONTENT));
        tv_params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        
        rlMain.setLayoutParams(rlMain_Params);
        rlMain.setBackgroundColor(0x88000000);
            
        particleViewTexture = new ParticleViewTexture(this, R.drawable.placeholder);
        particleViewTexture.addTextureViewToContainer(rlMain, gestureView_params);
        
        gestureview = new RKTGesturesView(this, this, getIntent());
        gestureview.setListener(this);
        gestureview.setLayoutParams(gestureView_params);
        rlMain.addView(gestureview);
        
        
        tv = new TextView(this);
        tv.setLayoutParams(tv_params);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(18.0f);
        tv.setGravity(Gravity.CENTER);
        
        boolean isStrictModeActive = getIntent().getBooleanExtra("strictMode", false);
        boolean isRecordingAgainAll = getIntent().getBooleanExtra("recordingUnlockAllAgain", false);
        
        String recordAgainUnlockAll = "";
        if (isRecordingAgainAll)
        {
        	recordAgainUnlockAll = "Please Record again the Unlock all gesture!\n";
        }
        
        
        String textToAppend = "";
        if (isStrictModeActive)
        {
        	textToAppend = "\nRecording gesture for high security mode";
        }
        
        timesRecordedBefore = getIntent().getIntExtra(kTimesRecordedKey, 0);
        switch(timesRecordedBefore)
        {
        	case 0:
        		tv.setText(recordAgainUnlockAll + "Please draw a single stroke gesture" + textToAppend);
        		break;
        		
        	case 1:
        		tv.setText(recordAgainUnlockAll + "Great! Now repeat the same drawing" + textToAppend);
        		break;
        		
        	case 2:
        		tv.setText(recordAgainUnlockAll +"We are almost there, repeat the same drawing one last time!" + textToAppend);
        		break;
        		
        	default:
        		tv.setText("Please draw a single stroke gesture" + textToAppend);
            	break;
        }

        tv.setBackgroundColor(0x00FFFFFF);
        rlMain.addView(tv);
        

        this.setContentView(rlMain);		
	}
	
	private void initDataSet() throws JSONException {	
			
		JSONObject n_set = new JSONObject();
		JSONObject n_gestureset = new JSONObject();
		n_gestureset.put("gestureset_name", "rktlauncher");
		n_gestureset.put("gid", GestureKit.getGID());			
		n_gestureset.put("gestures", new JSONArray());		
		
		n_set.put("gestureset", n_gestureset);
		
		// STORE GESTURESET HEADER			
		GKPreferences.put(RKTGesturesRecordingActivity.this, GestureKit.getGID(), n_set.toString());
		
		// STORE HELP HEADER
		GKPreferences.put(RKTGesturesRecordingActivity.this, helparray, n_set.toString());			
	}

	
	@Override
	public void didRecordGesture(int timesRecordedBefore)
	{
		if (timesRecordedBefore > 1) {
			
			Intent progIntent = new Intent(getApplicationContext(), DoneRecordingActivity.class);								
			startActivity(progIntent);
			finish();
			
		} 
		else 
		{					
		  Intent intent = getIntent();
		  intent.putExtra(kTimesRecordedKey, timesRecordedBefore + 1);
		  finish();
		  startActivity(intent);
		}			
		
	}

	@Override
	public void didMoveTouch(MotionEvent event)
	{
		if (particleViewTexture != null)
		{
			particleViewTexture.onTouch(event);
		}
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		if (particleViewTexture != null)
		{
			particleViewTexture.onPause();
		}
		
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		if (particleViewTexture != null)
		{
			particleViewTexture.onResume();
		}
	}
	
}
