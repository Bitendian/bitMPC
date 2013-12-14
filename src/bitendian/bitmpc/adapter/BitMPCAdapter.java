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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import bitendian.bitmpc.activity.BitMPC;

/**
 * Application specialized Adapter
 * 
 * @author juanan
 *
 * @param <T> Generic type to be managed by adapter.
 */
public abstract class BitMPCAdapter<T> extends BaseAdapter implements ListAdapter {

	private ArrayList<T> list;
	private BitMPC context;
	
	protected BitMPCAdapter(BitMPC _context) {
		list = new ArrayList<T>();
		context = _context;
	}
	
	protected BitMPC getContext() { return context; }
	
	public int getCount() { return list.size(); }

	public Object getItem(int _position) { return list.get(_position); }
	
	/**
	 * Get the data item associated with the specified position in the data set
	 * casted to generic T type.
	 * 
	 * @param _position Position of the item whose the data we want winthin the
	 * Adapter's data set.
	 * @return The data at the specified position casted to generic T type.
	 */
	public T getTItem(int _position) { return list.get(_position);	}

	public long getItemId(int _position) { return _position; }

	public T getFirst() { return list.size() > 0 ? list.get(0) : null; }
	
	public T getLast() { return list.size() > 0 ? list.get(list.size() - 1) : null; }
	
	public void setItem(int _position, T _item) { list.set(_position, _item); }
	
	/**
	 * Removes item at given position from data set.
	 * @param _position Where to delete item from data set.
	 */
	public void removeItem(int _position) { list.remove(_position); }
	
	/**
	 * Returns filename to read/save Adapter data set.
	 * @return Filename to read/save Adapter data set.
	 */
	protected abstract String getFilename();
	
	/**
	 * Save persistent attribute of Adapter. Childs can override this but must
	 * call parent.saveAttributes(_os).
	 * 
	 * @param _os Where to write persistent attributes.
	 */
	protected void saveAttributes(ObjectOutputStream _os) {
		try {
			_os.writeObject(list);
		} catch (IOException _e) {
			// TODO Auto-generated catch block
			_e.printStackTrace();
		}
	}
	
	/**
	 * Saves Adapter data set to disk.
	 */
	public void save() {
		try {
			ObjectOutputStream os = new ObjectOutputStream(context.openFileOutput(getFilename(), Context.MODE_PRIVATE));
			saveAttributes(os);
			os.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Restores attributes from stream. Childs can override this, but must call
	 * parent.restoreAttributes(_is) in same order than writeAttributes(_os).
	 * @param _is Where to read the attributes.
	 */
	@SuppressWarnings("unchecked")
	protected void restoreAttributes(ObjectInputStream _is) {
		try {
			list = (ArrayList<T>) _is.readObject();
		} catch (OptionalDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Restores Adapter data set from disk.
	 */
	public void restore() {
		try {
			ObjectInputStream is = new ObjectInputStream(context.openFileInput(getFilename()));
			restoreAttributes(is);
			is.close();
		} catch (StreamCorruptedException _e) {
			// TODO Auto-generated catch block
			_e.printStackTrace();
		} catch (FileNotFoundException _e) {
			// nothing to do
		} catch (IOException _e) {
			// TODO Auto-generated catch block
			_e.printStackTrace();
		}
	}
	
	/**
	 * Clears the Adapter.
	 */
	public void clear() {
		list.clear();
		notifyDataSetChanged();
	}
	
	/**
	 * Truncates Adapter to _size elements.
	 * 
	 * @param _size Max number of head elements that will remain in Adapter
	 */
	public void head(int _size) { for (int i = list.size() - 1, size = _size; i >= size; i--) list.remove(i); }

	public View getView(int _position, View _convertView, ViewGroup _parent) {
		return getView(_position, list.get(_position), _convertView);
	}

	/**
	 * Adds _item at the end of the Adapter.
	 * 
	 * @param _item item to be added.
	 */
	public void addItem(T _item) { list.add(_item); }
	
	/**
	 * Returns a view representing the _item.
	 * 
	 * @param _item T instance to be represented.
	 * @return A view representing the _item.
	 */
	protected abstract View getView(int _position, T _item, View _view);
	
}
