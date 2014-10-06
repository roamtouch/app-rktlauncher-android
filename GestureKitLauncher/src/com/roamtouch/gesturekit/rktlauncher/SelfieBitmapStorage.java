package com.roamtouch.gesturekit.rktlauncher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

public class SelfieBitmapStorage {
	
	private static SelfieBitmapStorage instance;	
		
	private Context context;
	
	private Bitmap selfieBitmap;
	
	private String imagePath;

	public void setContext(Context context) {
		this.context = context;		
	}
	
	public static synchronized SelfieBitmapStorage getInstance() {
	    if (instance == null) {
	    	instance = new SelfieBitmapStorage();
	    }
	    return instance;
	  }
	
	public void storeImage(Bitmap image) {
		
		selfieBitmap = image;
		
	    File pictureFile = getOutputMediaFile();
	    if (pictureFile == null) {
	        Log.d("",
	                "Error creating media file, check storage permissions: ");// e.getMessage());
	        return;
	    } 
	    try {
	        FileOutputStream fos = new FileOutputStream(pictureFile);
	        image.compress(Bitmap.CompressFormat.PNG, 90, fos);
	        fos.close();
	    } catch (FileNotFoundException e) {
	        Log.d("", "File not found: " + e.getMessage());
	    } catch (IOException e) {
	        Log.d("", "Error accessing file: " + e.getMessage());
	    }  
	}
	
	public void setSelfieBitmap(Bitmap selfieBitmap) {
		this.selfieBitmap = selfieBitmap;
	}

	public Bitmap getSelfieBitmap() {
		return selfieBitmap;
	}

	/** Create a File for saving an image or video */
	public  File getOutputMediaFile(){
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this. 
	    File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
	            + "/Android/data/"
	            + this.context.getApplicationContext().getPackageName()
	            + "/Files"); 

	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            return null;
	        }
	    } 
	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
	    File mediaFile;
	        String mImageName="MI_"+ timeStamp +".jpg";
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);  
	    return mediaFile;
	} 
	
	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
	
}



