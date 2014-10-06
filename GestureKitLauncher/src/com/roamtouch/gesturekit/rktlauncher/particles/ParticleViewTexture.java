package com.roamtouch.gesturekit.rktlauncher.particles;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import com.roamtouch.gesturekit.GestureKit;
import com.roamtouch.gesturekit.rktlauncherandroid.R;

public class ParticleViewTexture implements TextureView.SurfaceTextureListener
{
	private TextureView			mTextureView;
	private RenderingThread		mThread;
	private Context				mContext;
	private GestureKit			gestureKit;
	private int					backgroundResourceId;
	private int					rocketResourceId;
	private BroadcastReceiver	receiver;
	private boolean				isScreenOn;
	private boolean				isTextureAvailable;

	private ArrayList<Particle>	mParticleList	= new ArrayList<Particle>();

	// / Constructor
	public ParticleViewTexture(Context context, FrameLayout content, GestureKit gesturekit)
	{

		mContext = context;
		this.gestureKit = gesturekit;
		
		mTextureView = new TextureView(context);
		mTextureView.setSurfaceTextureListener(this);
		mTextureView.setOpaque(false);

		// Point screen = this.gestureKit.getScreenSize();

		content.addView(mTextureView, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT,
				Gravity.CENTER));

		backgroundResourceId = R.drawable.space;
		rocketResourceId = R.drawable.rocket;

	}

	public ParticleViewTexture(Context context, int backgroundResourceId)
	{
		mContext = context;

		mTextureView = new TextureView(context);
		mTextureView.setSurfaceTextureListener(this);
		mTextureView.setOpaque(false);
		
		isScreenOn = true;
		

		this.backgroundResourceId = backgroundResourceId;
	}

	public void addTextureViewToContainer(ViewGroup vg, LayoutParams params)
	{
		mTextureView.setLayoutParams(params);
		vg.addView(mTextureView);
	}

	public void serGestureKit(GestureKit gk)
	{
		this.gestureKit = gk;
	}

	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height)
	{
		isTextureAvailable = true;
		startThread();
	}

	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height)
	{
		// Ignored
	}

	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface)
	{
		isTextureAvailable = false;
		stopThread();
		return true;
	}

	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface)
	{
		// Ignored
	}

	public boolean onTouch(MotionEvent event)
	{
		switch (event.getActionMasked())
		{
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
			case MotionEvent.ACTION_MOVE:
				mParticleList.add(new Particle((int) event.getX(), (int) event.getY()));
				mParticleList.add(new Particle((int) event.getX() + 5, (int) event.getY() + 5));
				// mParticleList.add(new Particle((int)event.getX() + 0,
				// (int)event.getY() + 10));
				// mParticleList.add(new Particle((int)event.getX() + 10,
				// (int)event.getY() + 0));
				break;

			default:
				break;
		}

		return true;
	}
	
	public void onResume()
	{
		isScreenOn = true;
		startThread();
	}

	public void onPause()
	{
		isScreenOn = false;
		stopThread();
		try
		{
			mContext.unregisterReceiver(receiver);
		}
		catch (Exception e)
		{
			
		}
	}

	private void startThread()
	{
		stopThread();

		if (isTextureAvailable && isScreenOn)
		{
			mThread = new RenderingThread(mTextureView, mContext, backgroundResourceId, rocketResourceId);
			mParticleList = mThread.mParticleList;
			mThread.start();
			
		}
	}

	private void stopThread()
	{
		if (mThread != null)
		{
			mThread.stopRendering();
		}
	}
}
