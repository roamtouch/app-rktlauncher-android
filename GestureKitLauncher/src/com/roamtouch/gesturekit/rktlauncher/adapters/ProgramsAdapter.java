package com.roamtouch.gesturekit.rktlauncher.adapters;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.roamtouch.gesturekit.GestureKit;
import com.roamtouch.gesturekit.data.GKPreferences;
import com.roamtouch.gesturekit.rktlauncher.activities.ProgramsGesturesActivity;
import com.roamtouch.gesturekit.rktlauncher.activities.RKTGesturesRecordingActivity;
import com.roamtouch.gesturekit.rktlauncher.activities.SettingsActivity;
import com.roamtouch.gesturekit.rktlauncher.activities.StrictModeActivity;
import com.roamtouch.gesturekit.rktlauncherandroid.R;

public class ProgramsAdapter extends BaseAdapter
{

	GestureListItem[]				gestureListItems;

	private static LayoutInflater	inflater	= null;

	ProgramsGesturesActivity				mainActivity;
	Context							context;

	String							gesture_actions;
	
	boolean							isSecurityHigh;
		
    View.OnTouchListener mTouchListener;

	public ProgramsAdapter(
			Context context, 
			ProgramsGesturesActivity mainActivity, 
			GestureListItem[] gestureListItems,
			View.OnTouchListener listener)
	{

		this.context = mainActivity;
		
		if (listener!=null)
			this.mTouchListener = listener;		
		
		this.gestureListItems = gestureListItems;

		this.mainActivity = mainActivity;

		this.context = context;

		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		isSecurityHigh = GKPreferences.getBoolean(context, SettingsActivity.kPrefsSecurityHigh, false);

		// Load GestureActions linked to applications
		gesture_actions = GKPreferences.getString(context, "gesture_actions");

		if (gesture_actions.equals(""))
		{

			try
			{
				// NEW ACTIONS ARRAY
				JSONObject gesturesetActions = new JSONObject();
				gesturesetActions.put("amount", 0);
				gesturesetActions.put("actions", new JSONArray());

				// COMMIT TO ACTION
				GKPreferences.put(context, "gesture_actions", gesturesetActions.toString());

			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public int getCount()
	{
		return gestureListItems.length;
	}

	@Override
	public Object getItem(int position)
	{
		return position;
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	public class Holder
	{
		public TextView		tv;
		public TextView		tvPosition;
		public ImageView	img;
		public ImageView	imgG;
	}
	
	public void clearGestureImage(int position){
		gestureListItems[position].gestureImage=null;	
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{

		Holder holder = new Holder();

		View rowView;

		final String systemMethod;
		final String packagename;
		final String name;
		final Bitmap imageG;
		final boolean positionEnabled;

		rowView = inflater.inflate(program_list, null);

		rowView.setOnTouchListener(mTouchListener);

		packagename = gestureListItems[position].packageName;
		name = gestureListItems[position].programName;
		imageG = gestureListItems[position].gestureImage;
		systemMethod = gestureListItems[position].systemMethod;
		positionEnabled = gestureListItems[position].isStrictModeEnabled;

		holder.tv = (TextView) rowView.findViewById(appLabel);
		holder.tv.setText(name);

		holder.img = (ImageView) rowView.findViewById(appImage);
		holder.img.setImageBitmap(gestureListItems[position].programImage);

		holder.imgG = (ImageView) rowView.findViewById(gestureImage);
		if (imageG != null)
		{
			holder.imgG.setImageBitmap(imageG);
		}
		else
		{
			holder.imgG.setVisibility(View.INVISIBLE);
		}
		
		holder.tvPosition = (TextView)rowView.findViewById(R.id.appPosition);
		if (positionEnabled && isSecurityHigh)
		{
			holder.tvPosition.setVisibility(View.VISIBLE);
		}
		else
		{
			holder.tvPosition.setVisibility(View.INVISIBLE);
		}

		if (systemMethod != null && !systemMethod.isEmpty())
		{
			holder.img.setVisibility(View.GONE);
		}
		else
		{
			holder.img.setVisibility(View.VISIBLE);
		}
		
		rowView.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				/*GKPreferences.put(context, "count_gesture_records", 0);

				boolean strict = GKPreferences.getBoolean(context, "RKTisSecurityHigh", false);
				
				if (strict){
					
					Intent prgIntentStrict = new Intent(context, StrictModeActivity.class);
					prgIntentStrict.putExtra("name", name);
					prgIntentStrict.putExtra("systemMethod", systemMethod);
					prgIntentStrict.putExtra("packageName", packagename);
					prgIntentStrict.putExtra("UIID", GestureKit.getActiveUIID());
					prgIntentStrict.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(prgIntentStrict);
					
				} else {
					
					Intent prgIntent = new Intent(context, RKTGesturesRecordingActivity.class);
					prgIntent.putExtra("name", name);
					prgIntent.putExtra("systemMethod", systemMethod);
					prgIntent.putExtra("packageName", packagename);
					prgIntent.putExtra("UIID", GestureKit.getActiveUIID());
					prgIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);								
					context.startActivity(prgIntent);		
				}*/		
			}
		});

		return rowView;
	}
	
	public void recordGesture(int position){
		
		String systemMethod;
		String packagename;
		String name;
		Bitmap imageG;
		boolean positionEnabled;

		packagename = gestureListItems[position].packageName;
		name = gestureListItems[position].programName;
		imageG = gestureListItems[position].gestureImage;
		systemMethod = gestureListItems[position].systemMethod;
		positionEnabled = gestureListItems[position].isStrictModeEnabled;
		
		GKPreferences.put(context, "count_gesture_records", 0);

		boolean strict = GKPreferences.getBoolean(context, "RKTisSecurityHigh", false);
		
		//if (strict){
			
			Intent prgIntentStrict = new Intent(context, StrictModeActivity.class);
			prgIntentStrict.putExtra("name", name);
			prgIntentStrict.putExtra("systemMethod", systemMethod);
			prgIntentStrict.putExtra("packageName", packagename);
			prgIntentStrict.putExtra("UIID", GestureKit.getActiveGID());
			prgIntentStrict.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(prgIntentStrict);
			
		//} else {
			
			/*Intent prgIntent = new Intent(context, RKTGesturesRecordingActivity.class);
			prgIntent.putExtra("name", name);
			prgIntent.putExtra("systemMethod", systemMethod);
			prgIntent.putExtra("packageName", packagename);
			prgIntent.putExtra("UIID", GestureKit.getActiveGID());
			prgIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);								
			context.startActivity(prgIntent);*/		
		//}		
		
	}
	

	private int	program_list;
	private int	appLabel;
	private int	appImage;
	private int	gestureImage;

	public void setAssets(int list, int label, int image, int gesture)
	{
		this.program_list = list;
		this.appLabel = label;
		this.appImage = image;
		this.gestureImage = gesture;
	}


}