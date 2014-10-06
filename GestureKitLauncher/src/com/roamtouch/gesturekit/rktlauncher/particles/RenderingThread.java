package com.roamtouch.gesturekit.rktlauncher.particles;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.TextureView;

import com.roamtouch.gesturekit.rktlauncherandroid.R;

public class RenderingThread extends Thread
{
	private Context				context;
	private final TextureView	mSurface;
	private volatile boolean	mRunning		= true;
	private Bitmap				mImage[]		= new Bitmap[Particle.kNumberOfParticles];
	private Bitmap				background;
	private Bitmap				rocket;
	private Bitmap				launchedRocket;
	private Paint				paint;
	
	private int 				backgroundResourceId;
	private int 				rocketResourceId;
	private float 				density;
	private int 				lastParticleCount;

	public ArrayList<Particle>	mParticleList	= new ArrayList<Particle>();
	
	private Bitmap getBackground()
	{
		if (background == null)
		{
			background = getImgResouce(backgroundResourceId);
		}
		
		return background;
	}
	
	private Bitmap getParticleImage(int index)
	{
		if (mImage == null || mImage.length - 1 < index || mImage[index] == null)
		{
			mImage = new Bitmap[Particle.kNumberOfParticles];
			
			mImage[0] = getImgResouce(R.drawable.p1);
			mImage[1] = getImgResouce(R.drawable.p2);
			mImage[2] = getImgResouce(R.drawable.p3);
			mImage[3] = getImgResouce(R.drawable.p4);
			mImage[4] = getImgResouce(R.drawable.p5);
		}
		
		return mImage[index];
	}
	
	private Bitmap getRocket()
	{
		if (rocketResourceId != 0 && rocket == null)
		{
			if  (Particle.getLock() == true)
			{
				rocket = getImgResouce(rocketResourceId);
			}
		}
		
		return rocket;
	}
	
	private Bitmap getLaunchedRocket()
	{
		// if we should show a rocket
		if (launchedRocket == null && rocketResourceId != 0)
		{
			launchedRocket = getImgResouce(R.drawable.rocket_fire);
		}
		
		return launchedRocket;
	}
	
	private Paint getPaint()
	{
		if (paint == null)
		{
			paint = new Paint();
			paint.setColor(0xff00ff00);
		}
		
		return paint;
	}

	public RenderingThread(TextureView surface, Context context, int backgroundResourceId, int rocketResourceId)
	{
		mSurface = surface;
		this.context = context;

		mImage[0] = getImgResouce(R.drawable.p1);
		mImage[1] = getImgResouce(R.drawable.p2);
		mImage[2] = getImgResouce(R.drawable.p3);
		mImage[3] = getImgResouce(R.drawable.p4);
		mImage[4] = getImgResouce(R.drawable.p5);
		
		this.backgroundResourceId = backgroundResourceId;
		this.rocketResourceId = rocketResourceId;
		
		background = getImgResouce(backgroundResourceId);

		if (rocketResourceId != 0)
		{
			rocket = getImgResouce(rocketResourceId);
		}
		
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		density = 	metrics.density;

		paint = new Paint();
		paint.setColor(0xff00ff00);
	}

	private Bitmap getImgResouce(int res)
	{
		return BitmapFactory.decodeResource(context.getResources(), res);
	}

	@Override
	public void run()
	{
		int movementOffset = 0;
		
		while (mRunning && !Thread.interrupted())
		{
			final Canvas canvas = mSurface.lockCanvas(null);
			// draw only if something has changed
			if (canvas != null)
			{

				canvas.drawBitmap(getBackground(), null, new Rect(0, 0, canvas.getWidth(), canvas.getHeight()), getPaint());
				if (rocket != null)
				{
					int baseSpeed = 16;
					// If the particles are exploded, make the rocket launch
					if (Particle.getLock() == true)
					{
						if (density == 0)
						{
							movementOffset -= baseSpeed;
						}
						else
						{
							movementOffset -= baseSpeed * density;
						}
					}
					
					int xOffset = (canvas.getWidth() / 2) - (getRocket().getWidth() / 2);
					int yOffset = (canvas.getHeight() - getRocket().getHeight()) - (int)(80 * density) + movementOffset;
					
					if (Particle.getLock() == true)
					{
						canvas.drawBitmap(getLaunchedRocket(), xOffset, yOffset, getPaint());
					}
					else
					{
						canvas.drawBitmap(getRocket(), xOffset, yOffset, getPaint());
					}
				}
				try
				{
					synchronized (mParticleList)
					{
						Particle p = null;
						if (Particle.getShouldVanish())
						{
							mParticleList.clear();
						}
						else
						{
							for (int i = 0; i < mParticleList.size(); i++)
							{
								p = mParticleList.get(i);
								p.move();
								canvas.drawBitmap(getParticleImage(p.color), p.x - 10, p.y - 10, getPaint());
							}
						}
						lastParticleCount = mParticleList.size();
						Thread.sleep(15);
					}
				}
				catch (Exception e) {
					// TODO: handle exception
				}
				finally
				{
					mSurface.unlockCanvasAndPost(canvas);
				}
			}
		}
	}

	void stopRendering()
	{
		interrupt();
		mRunning = false;
	}
}
