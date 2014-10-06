package com.roamtouch.gesturekit.rktlauncher;

import android.app.Activity;
import android.app.Application;
import android.sax.StartElementListener;

public class RKTApplication extends Application
{
	private static HomeKeyLocker mHomeKeyLocker;
	
	public static final String GID = "16ec1986-5f54-436f-ad09-ef26126e0c20";
	
	public static String getGid() {
		return GID;
	}

	@Override
	public void onCreate()
	{
		// TODO Auto-generated method stub
		super.onCreate();
	}
	
	private static HomeKeyLocker getHomeKeyLocker()
	{
		if (mHomeKeyLocker == null)
		{
			mHomeKeyLocker = new HomeKeyLocker();
		}
		
		return mHomeKeyLocker;
	}
	
	public static void lockHome(Activity activity)
	{
		getHomeKeyLocker().lock(activity);
		
	}

	public static void unlockHome()
	{
		getHomeKeyLocker().unlock();
	}
}
