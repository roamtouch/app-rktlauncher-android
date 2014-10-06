package com.roamtouch.gesturekit.rktlauncher.adapters;

import java.util.ArrayList;
import java.util.List;

import com.roamtouch.gesturekit.rktlauncherandroid.R;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Array adapter for list of application of device's home screen.
 * @author piyush
 * @version 1.0
 */

public class ApplicationListAdapter extends BaseAdapter
{
	private List<ResolveInfo> applications;
	private LayoutInflater inflater;
	private PackageManager packageManager;

	public ApplicationListAdapter(final Context theContext)
	{
		inflater = LayoutInflater.from(theContext);
		this.applications = new ArrayList<ResolveInfo>();
		this.packageManager = theContext.getPackageManager();
	}

	public void setItems(List<ResolveInfo> theApplications)
	{
		this.applications.clear();
		this.applications.addAll(theApplications);
	}

	@Override
	public int getCount()
	{
		return applications.size();
	}

	@Override
	public ResolveInfo getItem(int position)
	{
		return applications.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return applications.get(position).specificIndex;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		final ResolveInfo application = this.getItem(position);
		final Drawable icon = application.activityInfo.loadIcon(this.packageManager);
		CharSequence appName = application.activityInfo.applicationInfo.name;
		appName = application.activityInfo.loadLabel(packageManager);
		if (appName == null)
		{
			appName = application.activityInfo.name;
		}
		if (convertView == null)
		{
			convertView = this.inflater.inflate(R.layout.griditem_applications_hometracker, null);
		}
		((ImageView) convertView.findViewById(R.id.application_icon)).setImageDrawable(icon);
		((TextView) convertView.findViewById(R.id.application_name)).setText(appName);

		return convertView;
	}

}
