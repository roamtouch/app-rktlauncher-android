package com.roamtouch.gesturekit.rktlauncher.receiver;

import com.roamtouch.gesturekit.rktlauncher.service.ScreenService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Intent i = new Intent(context, ScreenService.class);
			context.startService(i);
		}
	}
}
