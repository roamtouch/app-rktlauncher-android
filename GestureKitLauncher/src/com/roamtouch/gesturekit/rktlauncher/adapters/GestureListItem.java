package com.roamtouch.gesturekit.rktlauncher.adapters;

import android.graphics.Bitmap;

public class GestureListItem
{
	public String programName;
	public Bitmap programImage;
	public String packageName;
	public Bitmap gestureImage;
	public boolean	isStrictModeEnabled;
	public String	systemMethod;
	
	public GestureListItem()
	{
		this.systemMethod = "";
	}
}
