/*
 * bitMPC
 *
 * Copyright 2010 BITENDIAN S.L.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 * 
 * Author: Juanan Guerrero (jguerrero@bitendian.com)
 * 
 */
package bitendian.bitmpc.adapter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;

import android.view.View;
import android.widget.CheckedTextView;
import bitendian.bitmpc.activity.BitMPC;
import bitendian.bitmpc.main.HostItem;


/**
 * Hosts
 * 
 * Host's storage.
 * 
 * @author juanan
 *
 */
public class HostsAdapter extends BitMPCAdapter<HostItem> {

	// current host, -1 stands for no current host
	private int current;
	
	public HostsAdapter(BitMPC _context) {
		super(_context);
		current = -1;
	}

	public void addItem(HostItem _item) {
		super.addItem(_item);
		if (current == -1) current = getCount() - 1;
		notifyDataSetChanged();
	}
	
	public HostItem getCurrent() { return current == -1 ? null : getTItem(current); }
	
	public int getCurrentPosition() { return current; }
	
	/**
	 * Set current host identified by position.
	 * @param _current host position.
	 */
	public void setCurrent(int _current) { 
		current = _current;
		notifyDataSetChanged();
	}

	@Override
	protected void saveAttributes(ObjectOutputStream _os) {
		super.saveAttributes(_os);
		try {
			_os.writeInt(current);
		} catch (IOException _e) {
			// TODO Auto-generated catch block
			_e.printStackTrace();
		}
	}
	
	@Override
	protected void restoreAttributes(ObjectInputStream _is) {
		// TODO Auto-generated method stub
		super.restoreAttributes(_is);
		try {
			current = _is.readInt();
		} catch (OptionalDataException _e) {
			// TODO Auto-generated catch block
			_e.printStackTrace();
		} catch (IOException _e) {
			// TODO Auto-generated catch block
			_e.printStackTrace();
		}
	}
	
	public void removeItem(int _host) {
		super.removeItem(_host);
		if (current >= _host) current--;
		notifyDataSetChanged();
	}

	@Override
	protected String getFilename() { return "hosts.data"; }

	@Override
	protected View getView(int _position, HostItem _item, View _view) {
		CheckedTextView v = (CheckedTextView) (_view == null ? getContext().getLayoutInflater().inflate(android.R.layout.simple_list_item_single_choice, null) : _view);
		v.setText(_item.toString());
		v.setChecked(getTItem(current) == _item);
		return v;
	}

}
