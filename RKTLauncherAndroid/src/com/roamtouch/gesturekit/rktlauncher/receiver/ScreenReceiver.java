package com.roamtouch.gesturekit.rktlauncher.receiver;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.roamtouch.gesturekit.rktlauncher.auxiliary.KeyConstants;
import com.roamtouch.gesturekit.data.GKPreferences;
import com.roamtouch.gesturekit.rktlauncher.LockScreenActivity;

public class ScreenReceiver extends BroadcastReceiver
{

	@Override
	public void onReceive(Context context, Intent intent)
	{
		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
		{
			if (GKPreferences.getBoolean(context, KeyConstants.kShouldShowLockScreen, false))
			{

				KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Activity.KEYGUARD_SERVICE);
				KeyguardLock lock = keyguardManager.newKeyguardLock(Context.KEYGUARD_SERVICE);
				lock.disableKeyguard();
				Intent i = new Intent(context, LockScreenActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(i);

			}
		}
	}
}
