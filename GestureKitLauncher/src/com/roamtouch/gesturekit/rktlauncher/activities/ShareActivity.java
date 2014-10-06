package com.roamtouch.gesturekit.rktlauncher.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;

public class ShareActivity extends Activity
{
	private UiLifecycleHelper	uiHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		uiHelper = new UiLifecycleHelper(this, null);
		uiHelper.onCreate(savedInstanceState);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback()
		{
			@Override
			public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data)
			{
				Log.e("Activity", String.format("Error: %s", error.toString()));
			}

			@Override
			public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data)
			{
				Log.i("Activity", "Success!");
			}
		});
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		uiHelper.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
	}

	@Override
	public void onPause()
	{
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		uiHelper.onDestroy();
	}

	public void shareLink()
	{
		try
		{
			getPackageManager().getApplicationInfo("com.facebook.katana", 0);
			postOnFB();

		}
		catch (PackageManager.NameNotFoundException e)
		{
			Toast.makeText(getActivity(), "To share on Facebook you have to install Facebook app!", Toast.LENGTH_LONG)
					.show();
		}
	}
	
	private void postOnFB()
	{
		if (FacebookDialog.canPresentShareDialog(getApplicationContext(),
				FacebookDialog.ShareDialogFeature.SHARE_DIALOG))
		{
			// Publish the post using the Share Dialog
			FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(this)
					.setLink("http://www.gesturekit.com/rkt-launcher/")
					.setCaption("Life is too short to unlock and launch").build();
			uiHelper.trackPendingDialogCall(shareDialog.present());

		}
		else
		{
			publishFeedDialog();
		}
	}

	private void publishFeedDialog()
	{
		Bundle params = new Bundle();
		params.putString("name", "RKTLauncher");
		params.putString("caption", "Life is too short to unlock and launch");
//		params.putString("description",
//				"The Facebook SDK for Android makes it easier and faster to develop Facebook integrated Android apps.");
		params.putString("link", "http://www.gesturekit.com/rkt-launcher/");


		WebDialog feedDialog = (new WebDialog.FeedDialogBuilder(getActivity(), Session.getActiveSession(), params))
				.setOnCompleteListener(new OnCompleteListener()
				{

					@Override
					public void onComplete(Bundle values, FacebookException error)
					{
						if (error == null)
						{
							// When the story is posted, echo the success
							// and the post Id.
							final String postId = values.getString("post_id");
							if (postId != null)
							{
								Toast.makeText(getActivity(), "Posted story, id: " + postId, Toast.LENGTH_SHORT).show();
							}
							else
							{
								// User clicked the Cancel button
								Toast.makeText(getActivity().getApplicationContext(),
										"Publish cancelled",
										Toast.LENGTH_SHORT).show();
							}
						}
						else if (error instanceof FacebookOperationCanceledException)
						{
							// User clicked the "x" button
							Toast.makeText(getActivity().getApplicationContext(),
									"Publish cancelled",
									Toast.LENGTH_SHORT).show();
						}
						else
						{
							// Generic, ex: network error
							Toast.makeText(getActivity().getApplicationContext(),
									"Error posting story",
									Toast.LENGTH_SHORT).show();
						}
					}

				}).build();
		feedDialog.show();
	}

	private Activity getActivity()
	{
		return this;
	}
}
