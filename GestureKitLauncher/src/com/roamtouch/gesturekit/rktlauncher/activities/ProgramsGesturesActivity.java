package com.roamtouch.gesturekit.rktlauncher.activities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.roamtouch.gesturekit.GestureKit;
import com.roamtouch.gesturekit.data.GKPreferences;
import com.roamtouch.gesturekit.recording.StoreGesture;
import com.roamtouch.gesturekit.rktlauncher.adapters.BackgroundContainer;
import com.roamtouch.gesturekit.rktlauncher.adapters.GestureListItem;
import com.roamtouch.gesturekit.rktlauncher.adapters.MainAdapter;
import com.roamtouch.gesturekit.rktlauncher.adapters.ProgramsAdapter;
import com.roamtouch.gesturekit.rktlauncherandroid.R;

public class ProgramsGesturesActivity extends ShareActivity
{
	public Context				context;

	private ListView			listView;
	private LinearLayout		layout;
	private LinearLayout		ll_content;
	private Button				btnSettings;
	private Button				btnAbout;
	private Button				btnShare;
	private RelativeLayout		rlButtonContainer;

	private GestureListItem[]	gestureListItems;
	private GestureListItem[]	systemListItems;

	private int					listViewId;
	private int					progressbar_viewId;

	private int					program_list;
	private int					appLabel;
	private int					appImage;
	private int					gestureImage;

	ProgramsAdapter				pAdapter;
	ProgramsAdapter				systemAdapter;

	MainAdapter					mainAdapter;
	ProgramsGesturesActivity	_this;
	Intent						intent;

	static boolean				active	= false;
	
	// Row Deleta Animation
	BackgroundContainer mBackgroundContainer;
	boolean mSwiping = false;
    boolean mItemPressed = false;
    HashMap<Long, Integer> mItemIdTopMap = new HashMap<Long, Integer>();
    private static final int SWIPE_DURATION = 250;
    private static final int MOVE_DURATION = 150;
    
    private String helparray;
          
	public ProgramsGesturesActivity()
	{		
		// Help Array		
		String gid = GKPreferences.getString(this, "gk_application_gid");
		helparray = gid + "_help_array";
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.programs);	

		mBackgroundContainer = (BackgroundContainer) findViewById(R.id.listViewBackground);
		
		listViewId = R.id.listView;
		progressbar_viewId = R.id.progressbar_view;

		this._this = this;

		// Init preferences
		GKPreferences.prefs(this);

		this.context = this.getApplicationContext();
		rlButtonContainer = (RelativeLayout) findViewById(R.id.programs_btn_layout);
		listView = (ListView) findViewById(listViewId);
		layout = (LinearLayout) findViewById(progressbar_viewId);
		ll_content = (LinearLayout) findViewById(R.id.programs_ll_content);
		btnSettings = (Button) findViewById(R.id.programs_btn_settings);
		btnAbout = (Button)findViewById(R.id.programs_btn_about);
		btnShare = (Button)findViewById(R.id.programs_btn_share);
		
		btnSettings.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(ProgramsGesturesActivity.this, SettingsActivity.class);
				startActivity(intent);
			}
		});
		
		btnAbout.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(ProgramsGesturesActivity.this, AboutActivity.class);
				startActivity(intent);				
			}
		});
		
		btnShare.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				shareLink();				
			}
		});
		
		program_list = R.layout.program_list;
		appLabel = R.id.appLabel;
		appImage = R.id.appImage;
		gestureImage = R.id.gestureImage;
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		new CheckInstalledApps().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.layout.main, menu);
		return true;
	}

	public Bitmap scaleBitmap(Bitmap bitmapToScale, float scaleX, float scaleY)
	{
		if (bitmapToScale == null) return null;
		// create a matrix for the manipulation
		Matrix matrix = new Matrix();

		// resize the bit map
		matrix.postScale(scaleX, scaleY);

		// recreate the new Bitmap and set it back
		return Bitmap.createBitmap(bitmapToScale,
				0,
				0,
				bitmapToScale.getWidth(),
				bitmapToScale.getHeight(),
				matrix,
				true);
	}

	public class CheckInstalledApps extends AsyncTask<String, Integer, Boolean>
	{

		List<ResolveInfo>	list;
		JSONArray			check_obj;

		public CheckInstalledApps()
		{
		}
		
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			layout.setVisibility(View.VISIBLE);
			ll_content.setVisibility(View.GONE);
			rlButtonContainer.setVisibility(View.GONE);
		}

		@Override
		protected void onPostExecute(Boolean result)
		{
			layout.setVisibility(View.GONE);
			ll_content.setVisibility(View.VISIBLE);
			rlButtonContainer.setVisibility(View.VISIBLE);
			pAdapter.notifyDataSetChanged();
			super.onPostExecute(result);
		}

		Hashtable	storedgestures;

		@Override
		protected Boolean doInBackground(String... param)
		{

			final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
			mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

			PackageManager pm = getApplicationContext().getPackageManager();
			Intent intent = new Intent(Intent.ACTION_MAIN, null);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			list = pm.queryIntentActivities(intent, PackageManager.PERMISSION_GRANTED);

			gestureListItems = new GestureListItem[list.size()];

			JSONArray jsonArray = new JSONArray();

			int i = 0;
			// Matching stored gestures images on the list.
			String add_help_array = GKPreferences.getString(ProgramsGesturesActivity.this, helparray);
			storedgestures = new Hashtable();
			if (add_help_array.length() > 0)
			{
				JSONObject gestures_array = null;

				try
				{
					gestures_array = new JSONObject(add_help_array);
					JSONObject gestureset = gestures_array.getJSONObject("gestureset");
					JSONArray gestures = gestureset.getJSONArray("gestures");

					for (int j = 0; j < gestures.length(); j++)
					{
						JSONObject gesture = (JSONObject) gestures.get(j);
						String packag_name = gesture.getString("packageName");
						String image = gesture.getString("image");
						boolean isStrictModeEnabled = gesture.getBoolean("strictMode"); 

						if (!image.equals(""))
						{
							HelpObj h = new HelpObj();
							h.setImage(image);
							h.setPackageName(packag_name);
							h.setStrictMode(isStrictModeEnabled);
							storedgestures.put(packag_name, h);
						}
					}
				}
				catch (JSONException e1)
				{
					e1.printStackTrace();
				}
			}

			for (ResolveInfo rInfo : list)
			{

				final String name = rInfo.activityInfo.applicationInfo.loadLabel(pm).toString();
				String packageName = rInfo.activityInfo.applicationInfo.packageName.toString();

				Drawable iconDrawable = rInfo.activityInfo.applicationInfo.loadIcon(pm);
				final Bitmap iconBitmap = drawableToBitmap(iconDrawable);				
				
				gestureListItems[i] = new GestureListItem();
				gestureListItems[i].programImage = iconBitmap;
				gestureListItems[i].programName = name;
				gestureListItems[i].packageName = packageName;
				
				// Check if the gesture is local.
				if (storedgestures.containsKey(packageName))
				{
					HelpObj ho = new HelpObj();
					ho = (HelpObj) storedgestures.get(packageName);
					
					String image = ho.getImage();

					byte[] decodedString = Base64.decode(image, Base64.NO_WRAP);
					InputStream inputStream = new ByteArrayInputStream(decodedString);
					Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
					gestureListItems[i].gestureImage = bitmap;
					
					gestureListItems[i].isStrictModeEnabled = ho.getStrictMode();
										
				}
				else
				{
					gestureListItems[i].gestureImage = null;
				}

				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				iconBitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
				byte[] imageStream = stream.toByteArray();

				JSONObject json = new JSONObject();
				try
				{
					json.put("icon", imageStream.toString());
					json.put("packageName", packageName);
					json.put("name", name);

				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}
				jsonArray.put(json);

				i++;
			}

			pAdapter = new ProgramsAdapter(context, _this, sortItemList(gestureListItems), programTouchListener);

			pAdapter.setAssets(program_list, appLabel, appImage, gestureImage);

			// Add system adapter
			systemListItems = new GestureListItem[3];

			GestureListItem helpItem = new GestureListItem();
			
			helpItem.packageName = "com.rktlauncher.helpgesture";
			//helpItem.programImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.question_mark);
			helpItem.programName = " RKT Help";
			helpItem.systemMethod = "Help";
			setThumbnailForSystemGesture(helpItem, R.drawable.help_gesture);

			systemListItems[0] = helpItem;

			GestureListItem blockItem = new GestureListItem();
			blockItem.packageName = "com.rktlauncher.unlockallgesture";
			//blockItem.programImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.unlock);
			blockItem.programName = "RKT Unlock";
			blockItem.systemMethod = "Unlock_all";
			setThumbnailForSystemGesture(blockItem, R.drawable.unlock_gesture);

			systemListItems[1] = blockItem;
			
			GestureListItem selfieItem = new GestureListItem();
			selfieItem.packageName = "com.rktlauncher.selfiegesture";
			//blockItem.programImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.unlock);
			selfieItem.programName = "RKT Selfie";
			selfieItem.systemMethod = "Selfie";
			setThumbnailForSystemGesture(selfieItem, R.drawable.unlock_gesture);

			systemListItems[2] = selfieItem;

			systemAdapter = new ProgramsAdapter(context, _this, systemListItems, systemTouchListener);

			systemAdapter.setAssets(program_list, appLabel, appImage, gestureImage);

			mainAdapter = new MainAdapter(ProgramsGesturesActivity.this);
			mainAdapter.addSection("System", systemAdapter);
			mainAdapter.addSection("Applications", pAdapter);

			runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					listView.setAdapter(mainAdapter);
				}
			});

			return null;
		}
		
		private void setThumbnailForSystemGesture(GestureListItem currentItem, int defaultDrawable)
		{
			// Check if the gesture is local.
			if (storedgestures.containsKey(currentItem.packageName))
			{
				HelpObj ho = new HelpObj();
				ho = (HelpObj) storedgestures.get(currentItem.packageName);

				String image = ho.getImage();

				byte[] decodedString = Base64.decode(image, Base64.NO_WRAP);
				InputStream inputStream = new ByteArrayInputStream(decodedString);
				Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
				currentItem.gestureImage = bitmap;

				currentItem.isStrictModeEnabled = ho.getStrictMode();

			}
			else
			{
				currentItem.gestureImage = BitmapFactory.decodeResource(context.getResources(), defaultDrawable);
			}
		}

		private GestureListItem[] sortItemList(GestureListItem[] itemList)
		{
			ArrayList<GestureListItem> noGesturePrograms = new ArrayList<GestureListItem>();
			ArrayList<GestureListItem> gestureProgramsWithGestures = new ArrayList<GestureListItem>();

			for (int i = 0; i < itemList.length; i++)
			{
				if (itemList[i].gestureImage == null || itemList[i].gestureImage.equals(""))
				{
					noGesturePrograms.add(itemList[i]);
				}
				else
				{
					gestureProgramsWithGestures.add(itemList[i]);
				}
			}

			GestureListItem[] returnList = new GestureListItem[itemList.length];
			int index = 0;
			for (GestureListItem gestureListItem : gestureProgramsWithGestures)
			{
				returnList[index] = gestureListItem;
				index++;
			}

			for (GestureListItem gestureListItem : noGesturePrograms)
			{
				returnList[index] = gestureListItem;
				index++;
			}

			return returnList;
		}

		public class HelpObj
		{
			private String	packageName;
			private String	image;
			private boolean isStrictModeEnabled;

			public String getPackageName()
			{
				return packageName;
			}

			public void setPackageName(String packageName)
			{
				this.packageName = packageName;
			}

			public String getImage()
			{
				return image;
			}

			public void setImage(String image)
			{
				this.image = image;
			}
			
			public boolean getStrictMode()
			{
				return this.isStrictModeEnabled;
			}
			
			public void setStrictMode(boolean strictMode)
			{
				this.isStrictModeEnabled = strictMode;
			}

		}

		public String encodeTobase64(Bitmap image)
		{
			Bitmap immagex = image;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			immagex.compress(Bitmap.CompressFormat.JPEG, 100, baos);
			byte[] b = baos.toByteArray();
			String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

			Log.e("LOOK", imageEncoded);
			return imageEncoded;
		}

		public Bitmap decodeBase64(String input)
		{
			byte[] decodedByte = Base64.decode(input, 0);
			return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
		}

		public Bitmap drawableToBitmap(Drawable drawable)
		{
			if (drawable instanceof BitmapDrawable)
			{
				return ((BitmapDrawable) drawable).getBitmap();
			}

			Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
					drawable.getIntrinsicHeight(),
					Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmap);
			drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
			drawable.draw(canvas);

			return bitmap;
		}

	}

	@Override
	public void onStart()
	{
		super.onStart();
		active = true;
	}

	@Override
	public void onStop()
	{
		super.onStop();
		active = false;
	}	
	
	/*
	 * Independent gesture detector for program listview 
	 * to distinguish touch than fling on top of row.       
	 */
    final GestureDetector programGestureDetector = new GestureDetector(new ProgramGestureListener());
    class ProgramGestureListener extends SimpleOnGestureListener {
    	@Override    	
    	public boolean onSingleTapConfirmed(MotionEvent event) {  	
    		int clicked_position = listView.getPositionForView(programClickedRowView);    		
    		int countSystems = mainAdapter.getSectionCountPerTittle("System")+1; //All System + App Section    		
           	//Force to start recording after we count where the Applications items are.
    		pAdapter.recordGesture(clicked_position-countSystems);           	
    		return super.onSingleTapUp(event);       		
    	}
    }   
    boolean singleTap;
	private View programClickedRowView;
	
    @SuppressLint("NewApi")
	private View.OnTouchListener programTouchListener = new View.OnTouchListener() {
        
        float mDownX;
        private int mSwipeSlop = -1;
        
        @Override
        public boolean onTouch(final View v, MotionEvent event) {
                    
        	programClickedRowView = v;
        	
        	programGestureDetector.onTouchEvent(event);
        	
        	if (mSwipeSlop < 0) {
                mSwipeSlop = ViewConfiguration.get(ProgramsGesturesActivity.this).
                        getScaledTouchSlop();
            }
            switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mItemPressed) {
                    // Multi-item swipes not handled
                    return false;
                }
                mItemPressed = true;
                mDownX = event.getX();
                break;
            case MotionEvent.ACTION_CANCEL:
                v.setAlpha(1);
                v.setTranslationX(0);
                mItemPressed = false;
                break;
            case MotionEvent.ACTION_MOVE:
                {
                    float x = event.getX() + v.getTranslationX();
                    float deltaX = x - mDownX;
                    float deltaXAbs = Math.abs(deltaX);
                    if (!mSwiping) {
                        if (deltaXAbs > mSwipeSlop) {
                            mSwiping = true;
                            listView.requestDisallowInterceptTouchEvent(true);
                            mBackgroundContainer.showBackground(v.getTop(), v.getHeight());
                        }
                    }
                    if (mSwiping) {
                        v.setTranslationX((x - mDownX));
                        v.setAlpha(1 - deltaXAbs / v.getWidth());
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                {
                    // User let go - figure out whether to animate the view out, or back into place
                    if (mSwiping) {
                        float x = event.getX() + v.getTranslationX();
                        float deltaX = x - mDownX;
                        float deltaXAbs = Math.abs(deltaX);
                        float fractionCovered;
                        float endX;
                        float endAlpha;
                        final boolean remove;
                        if (deltaXAbs > v.getWidth() / 4) {
                            // Greater than a quarter of the width - animate it out
                            fractionCovered = deltaXAbs / v.getWidth();
                            endX = deltaX < 0 ? -v.getWidth() : v.getWidth();
                            endAlpha = 0;
                            remove = true;
                        } else {
                            // Not far enough - animate it back
                            fractionCovered = 1 - (deltaXAbs / v.getWidth());
                            endX = 0;
                            endAlpha = 1;
                            remove = false;
                        }
                        // Animate position and alpha of swiped item
                        // NOTE: This is a simplified version of swipe behavior, for the
                        // purposes of this demo about animation. A real version should use
                        // velocity (via the VelocityTracker class) to send the item off or
                        // back at an appropriate speed.
                        long duration = (int) ((1 - fractionCovered) * SWIPE_DURATION);
                        listView.setEnabled(false);
                        v.animate().setDuration(duration).
                                alpha(endAlpha).translationX(endX).
                                withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Restore animated values
                                        v.setAlpha(1);
                                        v.setTranslationX(0);
                                        if (remove) {                                        	
                                        	int p = listView.getPositionForView(v);                                 	
                                        	
                                        	 // Remove gesture by app label
                                        	TextView t = (TextView) v.findViewById(appLabel);    
                                        	String method = t.getText().toString();
                                        	boolean existed = StoreGesture.deleteGestureFromJSON(method);
                                        	
                                        	 //Animate
                                        	animateRemoval(listView, v);
                                        	
                                        	if (existed){
                                        		
                                        		StoreGesture.removeHelpJSON(method);
                                        		
	                                        	// Remove picture and gesture
	                                        	p--;                                  	
	                                        	pAdapter.clearGestureImage(p);    
	                                        	gestureListItems[p].programImage = null;                                
	                                        	
	                                        	//Refresh the row, does not work
	                                            updateItemAtPosition(p);                                     
	                                        	
	                                        	//Refresh this activity to load list again with changes
	                                        	Intent programIntent = new Intent(context, ProgramsGesturesActivity.class);
	                                        	programIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	                                    		finish();
	                                    		context.startActivity(programIntent);
	                                    		
                                        	}
                                                                                        
                                        } else {
                                            mBackgroundContainer.hideBackground();
                                            mSwiping = false;
                                            listView.setEnabled(true);
                                        }
                                    }
                                });
                    }
                }
                mItemPressed = false;
                break;
            default: 
                return false;
            }
            return true;
        }
	
    };
	/**
     * This method animates all other views in the ListView container (not including ignoreView)
     * into their final positions. It is called after ignoreView has been removed from the
     * adapter, but before layout has been run. The approach here is to figure out where
     * everything is now, then allow layout to run, then figure out where everything is after
     * layout, and then to run animations between all of those start/end positions.
     */
    @SuppressLint("NewApi")
	private void animateRemoval(final ListView listview, View viewToRemove) {
        int firstVisiblePosition = listview.getFirstVisiblePosition();
        for (int i = 0; i < listview.getChildCount(); ++i) {
            View child = listview.getChildAt(i);
            if (child != viewToRemove) {
                int position = firstVisiblePosition + i;
                long itemId = pAdapter.getItemId(position);
                mItemIdTopMap.put(itemId, child.getTop());
            }
        }        
        // Delete the item from the adapter
        final int position = listView.getPositionForView(viewToRemove);
        //pAdapter.remove(pAdapter.getItem(position));       
        final ViewTreeObserver observer = listview.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                observer.removeOnPreDrawListener(this);
                boolean firstAnimation = true;
                int firstVisiblePosition = listview.getFirstVisiblePosition();
                for (int i = 0; i < listview.getChildCount(); ++i) {
                    final View child = listview.getChildAt(i);
                    final int position = firstVisiblePosition + i;
                    long itemId = pAdapter.getItemId(position);
                    Integer startTop = mItemIdTopMap.get(itemId);
                    int top = child.getTop();
                    if (startTop != null) {
                        if (startTop != top) {
                            int delta = startTop - top;
                            child.setTranslationY(delta);
                            child.animate().setDuration(MOVE_DURATION).translationY(0);
                            if (firstAnimation) {
                                child.animate().withEndAction(new Runnable() {                                   
                                	public void run() {                                       
                                    	mBackgroundContainer.hideBackground();                                 
                                        mSwiping = false;
                                        listView.setEnabled(true);                                      
                                    }
                                });
                                firstAnimation = false;
                            }
                        }
                    } else {
                        // Animate new views along with the others. The catch is that they did not
                        // exist in the start state, so we must calculate their starting position
                        // based on neighboring views.
                        int childHeight = child.getHeight() + listview.getDividerHeight();
                        startTop = top + (i > 0 ? childHeight : -childHeight);
                        int delta = startTop - top;
                        child.setTranslationY(delta);
                        child.animate().setDuration(MOVE_DURATION).translationY(0);
                        if (firstAnimation) {
                            child.animate().withEndAction(new Runnable() {
                                public void run() {                                 
                                	mBackgroundContainer.hideBackground();
                                    mSwiping = false;
                                    listView.setEnabled(true);                           	
                                }
                            });
                            firstAnimation = false;
                        }
                    }
                }
                mItemIdTopMap.clear();
                return true;
            }
        });
    }    
    private void updateItemAtPosition(int position) {
        int visiblePosition = listView.getFirstVisiblePosition();
        View view = listView.getChildAt(position - visiblePosition);
        listView.getAdapter().getView(position, view, listView);
    }
    
       
    @SuppressLint("NewApi")
   	private View.OnTouchListener systemTouchListener = new View.OnTouchListener() {         
     
    	@Override
           public boolean onTouch(final View v, MotionEvent event) {
			
    			if (event.getAction() == MotionEvent.ACTION_UP){
    				int clicked_position = listView.getPositionForView(v);
    				clicked_position--;
    				systemAdapter.recordGesture(clicked_position);  
    			}
    			
    			return true;  
           	
       
           }
    };
	
}



//Obtain MotionEvent object
/*long downTime = SystemClock.uptimeMillis();
long eventTime = SystemClock.uptimeMillis() + 100;

float x = event.getX();
float y = event.getY();

// List of meta states found here: developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
int metaState = 0;

MotionEvent event_up = MotionEvent.obtain(
    downTime, 
    eventTime, 
    MotionEvent.ACTION_UP, 
    x, 
    y, 
    metaState
);   		

//Hack that allows to bypass the touch event to the listview without returning true on the TouchEvent. 
singleTap=true;    		    		
listView.dispatchTouchEvent(event);
listView.dispatchTouchEvent(event_up);    		
singleTap=false;*/