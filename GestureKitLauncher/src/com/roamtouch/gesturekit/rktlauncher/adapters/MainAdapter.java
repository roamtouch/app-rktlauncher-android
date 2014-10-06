package com.roamtouch.gesturekit.rktlauncher.adapters;

import java.util.LinkedHashMap;
import java.util.Map;

import com.roamtouch.gesturekit.rktlauncherandroid.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;



public class MainAdapter extends BaseAdapter
{
	public final Map<String, Adapter>			sections			= new LinkedHashMap<String, Adapter>();
	public final HeaderListAdapter						headers;
	public final static int						TYPE_SECTION_HEADER	= 0;

	public MainAdapter(Context context)
	{
		headers = new HeaderListAdapter(context, R.layout.list_header);
	}

	public void addSection(String section, Adapter adapter)
	{
		this.headers.add(section);
		this.sections.put(section, adapter);
	}


	public int getSectionCountPerTittle(String label)
	{
		int size = 0;
		for (Object section : this.sections.keySet())
		{
			if (section.toString().equals(label)){
				Adapter adapter = sections.get(section);
				size = adapter.getCount() + 1;
			}			
		}
		return size;
	}

	public void removeSection(String section)
	{
		this.headers.remove(section);
		this.sections.remove(section);
	}

	@Override
	public Object getItem(int position)
	{
		for (Object section : this.sections.keySet())
		{
			Adapter adapter = sections.get(section);
			int size = adapter.getCount() + 1;

			// check if the position is inside the section
			if (position == 0) return section;
			if (position < size) return adapter.getItem(position - 1);
		}

		return null;
	}

	@Override
	public int getCount()
	{
		// total together all sections, plus one for each section header
		int total = 0;
		for (Adapter adapter : this.sections.values())
		{
			total += adapter.getCount() + 1;
		}
		return total;
	}

	public int getViewTypeCount()
	{
		// assume the headers count as one, then total all sections
		int total = 1;
		for (Adapter adapter : this.sections.values())
		{
			total += adapter.getViewTypeCount();
		}

		return total;
	}

	public int getItemViewType(int position)
	{
		int type = 1;
		for (Object section : this.sections.keySet())
		{
			Adapter adapter = sections.get(section);
			int size = adapter.getCount() + 1;

			// check if position inside the section
			if (position == 0) return TYPE_SECTION_HEADER;
			if (position < size) return type + adapter.getItemViewType(position - 1);

			position -= size;
			type += adapter.getViewTypeCount();
		}

		return -1;
	}

	public boolean areAllItemsSelectable()
	{
		return false;
	}

	public boolean isEnabled(int position)
	{
		return (getItemViewType(position) != TYPE_SECTION_HEADER);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		int sectionNum = 0;
		for (Object section : this.sections.keySet())
		{
			Adapter adapter = sections.get(section);
			int size = adapter.getCount() + 1;

			// check if position inside this section
			if (position == 0) return headers.getView(sectionNum, convertView, parent);
			if (position < size) return adapter.getView(position - 1, convertView, parent);

			// otherwise jump into next section
			position -= size;
			sectionNum++;
		}
		return null;
	}

	@Override
	public long getItemId(int position)
	{
		// TODO Auto-generated method stub
		return position;
	}

}
