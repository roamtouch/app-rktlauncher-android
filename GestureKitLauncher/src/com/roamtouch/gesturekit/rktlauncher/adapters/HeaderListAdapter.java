package com.roamtouch.gesturekit.rktlauncher.adapters;

import java.util.ArrayList;

import com.roamtouch.gesturekit.rktlauncherandroid.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class HeaderListAdapter extends ArrayAdapter<String>
{
	Context				context;
	int					layoutResourceId;
	ArrayList<String>	headerTitles	= null;

	public HeaderListAdapter(Context context, int layoutResourceId)
	{
		super(context, layoutResourceId);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.headerTitles = new ArrayList<String>();
	}

	@Override
	public void add(String object)
	{
		headerTitles.add(object);
		notifyDataSetChanged();
	}

	@Override
	public void remove(String object)
	{
		headerTitles.remove(object);
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View row = convertView;
		HeaderHolder holder = null;

		if (row == null)
		{
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new HeaderHolder();
			holder.txtTitle = (TextView) row.findViewById(R.id.list_header_title);

			row.setTag(holder);
		}
		else
		{
			holder = (HeaderHolder) row.getTag();
		}

		holder.txtTitle.setText(headerTitles.get(position));

		return row;
	}

	private class HeaderHolder
	{
		TextView	txtTitle;
	}
}
