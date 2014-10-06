package com.roamtouch.gesturekit.rktlauncher;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;


public class CheckInstalledAppd extends AsyncTask<String, Void, Long[]> { 		
	
		private Context context;
		
		List<ResolveInfo> list;
		JSONArray check_obj;
		SharedPreferences.Editor editor;
		
		public CheckInstalledAppd(Context cont, SharedPreferences.Editor editor) {
			this.context = cont;
			this.editor = editor;
		}	

		@Override
	    protected Long[] doInBackground(String... param) {					
			
			Long[] check_object = null;
			
			final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
			mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			final List<ResolveInfo> pkgAppsList = this.context.getPackageManager().queryIntentActivities( mainIntent, 0);
				    		
			 
		        PackageManager pm = context.getPackageManager();
		        Intent intent = new Intent(Intent.ACTION_MAIN, null);
		        intent.addCategory(Intent.CATEGORY_LAUNCHER);
		        list = pm.queryIntentActivities(intent, PackageManager.PERMISSION_GRANTED);
		        
		        //StringWriter writer = new StringWriter(1024);
				//writer.write("[");
				boolean isNotFirst = false;
				
				JSONArray jsonArray = new JSONArray(); 
				
		        for (ResolveInfo rInfo : list) {
		        	
		        	/*if(isNotFirst)
						writer.write(',');
					else
						isNotFirst = true;*/	
		           
		        	String name = rInfo.activityInfo.applicationInfo.loadLabel(pm).toString();
		        	String packageName = rInfo.activityInfo.applicationInfo.packageName.toString();	            
		        	
		        	Drawable iconDrawable = rInfo.activityInfo.applicationInfo.loadIcon(pm);
		        	Bitmap iconBitmap = drawableToBitmap(iconDrawable);
		        	String base64Icon = encodeTobase64(iconBitmap);
		        	base64Icon = base64Icon.replace(" ", "");
		        	int l = base64Icon.length();
		        	
		        	JSONObject json = new JSONObject();
		        	try {
		        		json.put("icon", base64Icon);
		        		json.put("packageName", packageName);
		        		json.put("name", name);
						
					} catch (JSONException e) {						
						e.printStackTrace();
					}        	
		        	jsonArray.put(json); 
		        	
		        	//writer.write("{\"name\":\""+name+"\",\"packageName\":\""+packageName+"\"}"); //+"\",\"icon\":\""+base64Icon+"\"}");		        	
		        
		        }
		        
		        editor.putString("apps", jsonArray.toString());
		        editor.commit();	
		        
		        return check_object;
			
	    }
		
		public static String encodeTobase64(Bitmap image)
		{
		    Bitmap immagex=image;
		    ByteArrayOutputStream baos = new ByteArrayOutputStream();  
		    immagex.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		    byte[] b = baos.toByteArray();
		    String imageEncoded = Base64.encodeToString(b,Base64.DEFAULT);

		    Log.e("LOOK", imageEncoded);
		    return imageEncoded;
		}
		public static Bitmap decodeBase64(String input) 
		{
		    byte[] decodedByte = Base64.decode(input, 0);
		    return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length); 
		}
		
		public static Bitmap drawableToBitmap (Drawable drawable) {
		    if (drawable instanceof BitmapDrawable) {
		        return ((BitmapDrawable)drawable).getBitmap();
		    }

		    Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
		    Canvas canvas = new Canvas(bitmap); 
		    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		    drawable.draw(canvas);

		    return bitmap;
		}
		

		@Override
	    protected void onPostExecute(final Long[] check_object) {}
	

}





