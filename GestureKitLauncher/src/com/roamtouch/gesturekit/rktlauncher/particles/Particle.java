package com.roamtouch.gesturekit.rktlauncher.particles;

import java.util.Random;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Particle extends BroadcastReceiver
{

	public static final int kNumberOfParticles = 5;
	
	public int				distFromOrigin	= 0;
	private double			direction;
	private double			directionCosine;
	private double			directionSine;
	public int				color;
	public int				x;
	public int				y;
	private int				initX;
	private int				initY;
	private static boolean	lock;
	private static boolean  shouldVanish;
	
	public boolean			BRAKE_APPART;

	public Particle()
	{

	}

	public Particle(int x, int y)
	{
		init(x, y);
		this.direction = 2 * Math.PI * new Random().nextInt(NO_OF_DIRECTION) / NO_OF_DIRECTION;
		this.directionCosine = Math.cos(direction);
		this.directionSine = Math.sin(direction);
		this.color = new Random().nextInt(kNumberOfParticles);
	}

	public void init(int x, int y)
	{
		distFromOrigin = 0;
		this.initX = this.x = x;
		this.initY = this.y = y;
	}

	public synchronized void move()
	{

		if (lock)
		{
			distFromOrigin += 4;
			moveXY();
		}

		// else
		//
		// if (distFromOrigin<10){
		// moveXY();
		// }
	}

	public void setLock(boolean b)
	{
		Particle.lock = b;
	}
	
	public static synchronized boolean getLock()
	{
		return Particle.lock;
	}
	
	public void setShouldVanish(boolean b)
	{
		Particle.shouldVanish = b;
	}
	
	public static synchronized boolean getShouldVanish()
	{
		return Particle.shouldVanish;
	}

	private void moveXY()
	{
		x = (int) (initX + distFromOrigin * directionCosine);
		y = (int) (initY + distFromOrigin * directionSine);
	}

	private final static int	NO_OF_DIRECTION	= 400;

	@Override
	public void onReceive(Context arg0, Intent arg1)
	{
		if (arg1.getAction().equals("STOP_BREAK_PARTICLES"))
		{
			setLock(false);
		}
		else if (arg1.getAction().equals("BREAK_PARTICLES"))
		{
			setLock(true);
		}
		else if (arg1.getAction().equals("VANISH_PARTICLES"))
		{
			setShouldVanish(true);
		}
		else if (arg1.getAction().equals("STOP_VANISH_PARTICLES"))
		{
			setShouldVanish(false);
		}
	}

}
