package com.roamtouch.gesturekit.rktlauncher;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.SurfaceHolder;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.roamotuch.gesturekit.plugin.GKActionInterface;
import com.roamtouch.gesturekit.GestureKit;
import com.roamtouch.gesturekit.rktlauncherandroid.R;

public class GestureActionTakeSelfie extends Activity implements GKActionInterface  {

	private String dir;
	private String fname;
	private Activity activity;
	
	 //Camera variables
    //a surface holder
    private SurfaceHolder sHolder; 
    //a variable to control the camera
    private Camera mCamera;
    //the camera parameters
    private Parameters parameters;
    
    public static final String PREFS_NAME = "ImageNumber";
    private int lastSavedNumber;
    private int currentCameraId;
    
    private Bitmap selfieBitmap;  
    
    private SelfieBitmapStorage sStorage;
     
        
    private GestureKit gK;    
    
    private Context context;
    
    private TextureView mTextureView;
	
	public GestureActionTakeSelfie(GestureKit gK, Activity activity){
		this.activity = activity;
		checkPicsDirectory();    	
		orientationListener = new CameraOrientationListener(activity);
		this.gK = gK;
		this.context = activity;		
	}
		
	@Override
	public String getActionID() {		
		return "Selfie";
	}

	@Override
	public void onGestureRecognized(Object... params) {
		
		//swap the id of the camera to be used
       if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK)
         currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
       else 
         currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;  
            
       Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
       int cameraCount = Camera.getNumberOfCameras();
       for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
           Camera.getCameraInfo(camIdx, cameraInfo);
           if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
               try {
            	   mCamera = Camera.open(camIdx);
               } catch (RuntimeException e) {
                   Log.v("Camera failed to open: ",  e.getLocalizedMessage());
               }
           }
       }

       if (mCamera != null) {
           
           SurfaceTexture dummy = new SurfaceTexture(0);

           try {
        	   mCamera.setPreviewTexture(dummy);
           } catch (IOException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
           }           

           mCamera.startPreview();         

           mCamera.takePicture(null, null, new Camera.PictureCallback() {

               @Override
               public void onPictureTaken(byte[] data, Camera camera) {                

            	   mCamera.stopPreview();           	   
            	   mCamera.release();

            	   //decode the data obtained by the camera into a Bitmap     
            	   FileOutputStream outStream = null;
            	   
            	   try {
            		   
            		   outStream = new FileOutputStream(dir);
            		   outStream.write(data);
            		   outStream.close();
            		   
            	   } catch (FileNotFoundException e){
            		   Log.d("CAMERA", e.getMessage());
            	   } catch (IOException e){
            		   Log.d("CAMERA", e.getMessage());
            	   }
            	   
            	   selfieBitmap = null;  
            	   File file = new File(dir);
            	   Uri imgUri = Uri.fromFile(file);  
            	   
            	   try {
            		   
            		   selfieBitmap = MediaStore.Images.Media.getBitmap(
        				           activity.getContentResolver(), imgUri);
            		   
            		   //Rotate
            	        int rotation = (
            	            displayOrientation
            	            + orientationListener.getRememberedOrientation()
            	            + layoutOrientation
            	        ) % 360;

            	        if (rotation != 0) {
            	            Bitmap oldBitmap = selfieBitmap;

            	            Matrix matrix = new Matrix();
            	            matrix.postRotate(-rotation);

            	            selfieBitmap = Bitmap.createBitmap(
            	            		selfieBitmap,
            	                0,
            	                0,
            	                selfieBitmap.getWidth(),
            	                selfieBitmap.getHeight(),
            	                matrix,
            	                false
            	            );

            	            oldBitmap.recycle();
            	        }
            		   
            		   selfieBitmap = getCroppedBitmap(selfieBitmap, 150);
            		   
            		   sStorage = SelfieBitmapStorage.getInstance();
            		   sStorage.storeImage(selfieBitmap);
            		   
            		   
            	   	} catch (FileNotFoundException e1) {				
        				e1.printStackTrace();
        			} catch (IOException e1) {
        				e1.printStackTrace();
        			}
            	   		 
            	   // Play picture sound          
            	   Resources res = activity.getResources();
            	   AssetFileDescriptor afd = res.openRawResourceFd(R.raw.camera_shutter_click);

            	   MediaPlayer mp = MediaPlayer.create(activity, R.raw.camera_shutter_click);
            	   mp.reset();
            	   mp.setAudioStreamType(AudioManager.STREAM_ALARM);                 

            	   try {

            		   mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            		   mp.prepare();

            		   mp.setOnCompletionListener(new OnCompletionListener() {           
            			   public void onCompletion(MediaPlayer mp) {
            				   mp.stop();
            				   mp.reset();                	        
            			   }
            		   });

        	    	   mp.start();
        	    	   					  
    	    	   } catch (IllegalArgumentException e) {					
    	    	   		e.printStackTrace();
    	    	   } catch (IllegalStateException e) {					
    	    	   		e.printStackTrace();
    	    	   } catch (IOException e) {					
    	    	   		e.printStackTrace();
    	    	   }   	      
            	   			 
    	    	   //To Store Last Saved Number
    	    	   SharedPreferences saveNumber = activity.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
    	    	   SharedPreferences.Editor editorset = saveNumber.edit();
    	    	   editorset.putInt("lastsavednumber",lastSavedNumber);
    	    	   editorset.putString(dir, "image");   
    	    	   editorset.commit();                        
                              
               	}     
       
           }); 
           
       }
        
	}
	
	private int displayOrientation;
	private int layoutOrientation;
	private CameraOrientationListener orientationListener;	  
    

	private void checkPicsDirectory(){		
		
		dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/RKTLauncherPics/";
		File f = new File(dir);
		
		if(!f.isDirectory()) {     
			File newdir = new File(dir); 
			newdir.mkdirs();
		}
		
		// Get Last Saved Number
	    SharedPreferences savedNumber = activity.getSharedPreferences(PREFS_NAME, 0);
	    lastSavedNumber = savedNumber.getInt("lastsavednumber",0); 
	    lastSavedNumber++;
	    
	    fname = "Image-"+lastSavedNumber+".png";     
	    
	    dir = dir + fname;
	    
	    sStorage = SelfieBitmapStorage.getInstance();
	    sStorage.setImagePath(dir);
	    
	}
	
	 public Camera determineDisplayOrientation(Camera camera, int cameraId) {
		 	
		 CameraInfo cameraInfo = new CameraInfo();
	        Camera.getCameraInfo(cameraId, cameraInfo);

	        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
	        int degrees  = 0;

	        switch (rotation) {
	            case Surface.ROTATION_0:
	                degrees = 0;
	                break;

	            case Surface.ROTATION_90:
	                degrees = 90;
	                break;

	            case Surface.ROTATION_180:
	                degrees = 180;
	                break;

	            case Surface.ROTATION_270:
	                degrees = 270;
	                break;
	        }

	        int displayOrientation;

	        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
	            displayOrientation = (cameraInfo.orientation + degrees) % 360;
	            displayOrientation = (360 - displayOrientation) % 360;
	        } else {
	            displayOrientation = (cameraInfo.orientation - degrees + 360) % 360;
	        }

	        this.displayOrientation = displayOrientation;
	        this.layoutOrientation  = degrees;

	        camera.setDisplayOrientation(displayOrientation);
	        
	        return camera;
	    }
	
	@Override
	public String getPackageName() {		
		return null;
	}
			
	public static Bitmap getCroppedBitmap(Bitmap bmp, int radius) {
	    Bitmap sbmp;
	    if(bmp.getWidth() != radius || bmp.getHeight() != radius)
	        sbmp = Bitmap.createScaledBitmap(bmp, radius, radius, false);
	    else
	        sbmp = bmp;
	    Bitmap output = Bitmap.createBitmap(sbmp.getWidth(),
	            sbmp.getHeight(), Config.ARGB_8888);
	    Canvas canvas = new Canvas(output);

	    final int color = 0xffa19774;
	    final Paint paint = new Paint();
	    final Rect rect = new Rect(0, 0, sbmp.getWidth(), sbmp.getHeight());

	    paint.setAntiAlias(true);
	    paint.setFilterBitmap(true);
	    paint.setDither(true);
	    canvas.drawARGB(0, 0, 0, 0);
	    paint.setColor(Color.parseColor("#BAB399"));
	    canvas.drawCircle(sbmp.getWidth() / 2+0.7f, sbmp.getHeight() / 2+0.7f,
	            sbmp.getWidth() / 2+0.1f, paint);
	    paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
	    canvas.drawBitmap(sbmp, rect, rect, paint);
	    
	    return output;
	}
	
	public class CameraOrientationListener extends OrientationEventListener {
	    private int currentNormalizedOrientation;
	    private int rememberedNormalizedOrientation;

	    public CameraOrientationListener(Context context) {
	        super(context, SensorManager.SENSOR_DELAY_NORMAL);
	    }

	    @Override
	    public void onOrientationChanged(int orientation) {
	        if (orientation != ORIENTATION_UNKNOWN) {
	            currentNormalizedOrientation = normalize(orientation);
	        }
	    }

	    private int normalize(int degrees) {
	        if (degrees > 315 || degrees <= 45) {
	            return 0;
	        }

	        if (degrees > 45 && degrees <= 135) {
	            return 90;
	        }

	        if (degrees > 135 && degrees <= 225) {
	            return 180;
	        }

	        if (degrees > 225 && degrees <= 315) {
	            return 270;
	        }

	        throw new RuntimeException("The physics as we know them are no more. Watch out for anomalies.");
	    }

	    public void rememberOrientation() {
	        rememberedNormalizedOrientation = currentNormalizedOrientation;
	    }

	    public int getRememberedOrientation() {
	        return rememberedNormalizedOrientation;
	    }
	}
	
}
