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
package bitendian.bitmpc.activity;

import android.content.Context;
import bitendian.bitmpc.adapter.PlaylistItemAdapter;
import bitendian.bitmpc.main.PlaylistItem;

public class PlaylistGestureListView extends GestureListView {

	public PlaylistGestureListView(Context _context) {
		super(_context);
	}

	@Override
	protected void swap(int _first, int _second) {
		PlaylistItemAdapter adapter = (PlaylistItemAdapter) getAdapter();
		PlaylistItem secondclon = (PlaylistItem) adapter.getTItem(_second).clone();
		PlaylistItem first = (PlaylistItem) adapter.getTItem(_first);
		PlaylistItem second = adapter.getTItem(_second);
		second.clear();
		for (String key : first.keySet()) second.put(key, first.get(key));
		first.clear();
		for (String key : secondclon.keySet()) first.put(key, secondclon.get(key));
		adapter.notifyDataSetChanged();
	}

}
