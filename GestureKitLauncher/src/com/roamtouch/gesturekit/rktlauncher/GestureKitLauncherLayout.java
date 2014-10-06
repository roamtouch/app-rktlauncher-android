package com.roamtouch.gesturekit.rktlauncher;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.roamotuch.gesturekit.plugin.PluginInterface;
import com.roamtouch.gesturekit.GestureKit;
import com.roamtouch.gesturekit.data.GKPreferences;
import com.roamtouch.gesturekit.data.PluginParams;
import com.roamtouch.gesturekit.rktlauncher.activities.SettingsActivity;
import com.roamtouch.gesturekit.rktlauncher.particles.ParticleViewTexture;
import com.roamtouch.gesturekit.rktlauncherandroid.R;

import cz.vhrdina.textclockbackport.TextClock;

public class GestureKitLauncherLayout extends FrameLayout implements PluginInterface {

	GestureKit gesturekit;
	Activity activity;
	GestureKit gk;
	
	MediaPlayer mediaPlayer;
		
	private ParticleViewTexture particles; 	
	
	private PluginParams pluginparams = new PluginParams();
	
	public Context context;
		
	int screenDensity;
	int leftR, topR, bottomR, rightR;
	
	public SelfieView sV;
	
	private ImageButton selfieButton; 
	
	 private SelfieBitmapStorage sStorage;
	 
	private String storedImage;
	
	private Handler thumbnailHandler = new Handler();
			
	@SuppressLint("SimpleDateFormat")
	public GestureKitLauncherLayout(Context context, GestureKit gk) {
		super(context);
		
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		float density = metrics.density;
				
		this.gk = gk;
		
		this.context = context;
		
		this.activity = (Activity) context;
		
		this.screenDensity = gk.getScreenDensity();
			
		this.pluginparams.GESTUREKIT_ENABLE_GRID = false;	
		
		// Bitmap Storage
		sStorage = SelfieBitmapStorage.getInstance();
		sStorage.setContext(this.context);
			    		
		particles = new ParticleViewTexture(context, this, gk);			
		particles.serGestureKit(gk);	
		
	    Calendar c = Calendar.getInstance();
		
        TextClock textClock = new TextClock(context);
        textClock.setTextColor(Color.WHITE);
        textClock.setTextSize(50.0f);
	    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
	    FrameLayout.LayoutParams.WRAP_CONTENT,
	    FrameLayout.LayoutParams.WRAP_CONTENT,
	    Gravity.CENTER_HORIZONTAL);
	    params.topMargin = (int)(density * 25.0f);
	    textClock.setLayoutParams(params);
	    addView(textClock);
        	    
	    SimpleDateFormat df2 = new SimpleDateFormat ("dd");
	    
	    TextView tvSecond = new TextView(context);
	    tvSecond.setText(parseDayOfWeek(c.get(Calendar.DAY_OF_WEEK)) + " " + df2.format(c.getTime()));
	    tvSecond.setTextColor(Color.WHITE);
	    tvSecond.setTextSize(10.0f);
//	    tvSecond.setTypeface(fontMolot);
	    FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_HORIZONTAL);
	    params2.topMargin = (int)(density * 85);
	    tvSecond.setLayoutParams(params2);
	    addView(tvSecond);
	    
	    String alarmString = Settings.System.getString(context.getContentResolver(), Settings.System.NEXT_ALARM_FORMATTED);
	    
	    if (alarmString != null && !alarmString.isEmpty())
	    {
			TextView tvAlarm = new TextView(context);
			tvAlarm.setText("ALARM: " + Settings.System.getString(context.getContentResolver(),
					Settings.System.NEXT_ALARM_FORMATTED));
			tvAlarm.setTextColor(Color.WHITE);
			tvAlarm.setTextSize(12.0f);
//			tvAlarm.setTypeface(fontMolot);
			FrameLayout.LayoutParams params3 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
					FrameLayout.LayoutParams.WRAP_CONTENT,
					Gravity.CENTER);
			tvAlarm.setLayoutParams(params3);
			addView(tvAlarm);
	    }
	    
	    // Selfie square
	    Point p = gk.getScreenSize();
	    leftR = 0; topR = 0; bottomR = p.x; rightR = p.y;
	    sV = new SelfieView(context);
	    addView(sV);
	    	    
	    if (GKPreferences.getBoolean(context, SettingsActivity.kPrefsSound, false))
	    {
	    	mediaPlayer = MediaPlayer.create(context, R.raw.gesture_sound);
	    }
	    
	    selfieButton = new ImageButton(this.context);
	    selfieButton.setVisibility(View.GONE);
	    
	    FrameLayout.LayoutParams params4 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT,
				Gravity.CENTER);
	    
	    selfieButton.setLayoutParams(params4);  	    	    
	    selfieButton.setBackgroundColor(Color.TRANSPARENT);
    
	    addView(selfieButton);
	    
	    selfieButton.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {        	
	        	Intent intent = new Intent();
	            intent.setAction(Intent.ACTION_VIEW);
	            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	            intent.setDataAndType(Uri.fromFile(new File(storedImage)), "image/*");
	            activity.startActivity(intent);
	        	
	        }
	    });  
				
	}
	
	public boolean showThumbnail;
	
	public boolean takeselfie;

	public void setTakeselfie(boolean takeselfie) {
		this.takeselfie = takeselfie;
	}

	@SuppressLint("DrawAllocation")
	public class SelfieView extends View {

        private Context mContext;

        public SelfieView(Context context) {
            super(context);

            this.mContext = context;
            
        }
                
        int countSelfieButton;

        @Override
        public void onDraw(Canvas canvas) {            
            super.onDraw(canvas);
          
            if (takeselfie){          	
            	
	            Paint paint = new Paint();
	            paint.setColor(Color.WHITE);
	            paint.setStyle(Paint.Style.STROKE); 
	            paint.setStrokeWidth(7);
	                      
	            int left = topR+7;
	            int right = topR+7; 
	            int width = bottomR-7;
	            int height = rightR-50;
	            
	            Rect selfieRect = new Rect(left, right, width, height);           
	            canvas.drawRect(selfieRect, paint);
	            
	            invalidate();
	            
	            // we do not render the thubnail until it is locally stored. 
	            if (sStorage.getSelfieBitmap()!=null) {	            	
	            	thumbnailHandler.postDelayed(thumbnailRunnable, 600);	            	
	            }
            } 
            
            if (showThumbnail){
            	
            	countSelfieButton++;
            	
            	if (countSelfieButton==300){
            		
            		sStorage.setSelfieBitmap(null);
            		selfieButton.setVisibility(View.GONE);
            		showThumbnail=false;  
            		countSelfieButton=0;
            		
            	}
            	
            	invalidate();
            	
            }           
        }
    }
	
	private Runnable thumbnailRunnable = new Runnable() {
		   @Override
		   public void run() {		      
		      
				takeselfie= false;       	
			 		 
	       		sStorage.getInstance();	        		
	       		File selfieImageFile =  sStorage.getOutputMediaFile();
	       		storedImage = sStorage.getImagePath();
	       		 
	       		selfieButton.setVisibility(View.VISIBLE);
	       		selfieButton.setImageBitmap(sStorage.getSelfieBitmap());  		 
	       		 
	       		showThumbnail=true;			   
	       		
	       		//handler.postDelayed(this, 100);
		   }
		};
	
	@Override
	public void onGesturesIcon(GestureKit gk, Map<String, String> icons) {
				
	}

	@Override
	public void proccessTouchEvent(MotionEvent event) {	

		if (particles != null)
		{
			particles.onTouch(event);
		}
		
		if (mediaPlayer != null && !mediaPlayer.isPlaying())
		{
			mediaPlayer.start();
		}
	}

	@Override
	public void clear() {
		
		
	}

	@Override
	public void showErrorLogo() {	
		
	}

	@Override
	public void showLoadingLogo() {
		
		
	}

	@Override
	public void showOkLogo() {
		
		
	}

	@Override
	public PluginParams getPluginParams() {
		return pluginparams;
	}


	@Override
	public void setPluginParams(PluginParams params) {
		this.pluginparams = params;
	}
	
	public void onResume()
	{
		if (particles != null)
		{
			particles.onResume();
		}
	}
	
	public void onDestroy()
	{
		if (particles != null)
		{
			particles.onPause();
		}
	}
	
	private String parseDayOfWeek(int dayOfWeek)
	{
		switch (dayOfWeek)
		{
			case Calendar.MONDAY:
				return "MONDAY";
				
			case Calendar.TUESDAY:
				return "TUESDAY";
				
			case Calendar.WEDNESDAY:
				return "WEDNESDAY";
				
			case Calendar.THURSDAY:
				return "THURSDAY";
				
			case Calendar.FRIDAY:
				return "FRIDAY";
				
			case Calendar.SATURDAY:
				return "SATURDAY";
				
			case Calendar.SUNDAY:
				return "SUNDAY";
				
			default:
				return "";
		}
	}
	

	@Override
	public void onSendCommand(Object... params) {
		if (params[0].toString().equals("selfie")){
			takeselfie  = true;
			new Runnable() {
				  public void run() {
					  sV.invalidate();
				  }
			}.run();		
		}
	}

}
