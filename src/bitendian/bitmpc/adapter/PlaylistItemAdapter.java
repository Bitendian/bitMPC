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

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import bitendian.bitmpc.R;
import bitendian.bitmpc.activity.BitMPC;
import bitendian.bitmpc.main.PlaylistItem;

public class PlaylistItemAdapter extends BitMPCAdapter<PlaylistItem> {

	private int selected = -1;	
	private PlaylistItem aux;
	private boolean notifyOnAdd;
	
	public PlaylistItemAdapter(BitMPC _context, boolean _notifyOnAdd) {
		super(_context);
		selected = -1;
		notifyOnAdd = _notifyOnAdd;
	}
	
	public void addItem(String _text) { 
		if (_text.startsWith("file")) {
			addItem(new PlaylistItem(_text));
			if (notifyOnAdd) notifyDataSetChanged();
		} else {
			getLast().add(_text);
		}
	}
	
	public void updateItem(String _text) {
		if (_text.startsWith("file")) {
			aux = new PlaylistItem(_text);
		} else if (_text.startsWith("Pos")) { 
			aux.add(_text);
			int pos = Integer.parseInt(aux.get("Pos"));
			if (pos < getCount()) {
				PlaylistItem item = getTItem(pos);
				item.clear();
				for (String key : aux.keySet()) item.put(key, aux.get(key));
			} else {
				addItem(aux);
			}
		} else {
			aux.add(_text);
		}		
	}
	
	public void setSelected(int _selected) {
		selected = _selected;
		notifyDataSetChanged();
	}
	
	public int getSelected() { return selected; }
	
	@Override
	protected View getView(int _position, PlaylistItem _item, View _view) {
		if (_view == null) _view = getContext().getLayoutInflater().inflate(R.layout.listitem, null);
		TextView main = (TextView) _view.findViewById(R.id.listitem_main);
		TextView sub = (TextView) _view.findViewById(R.id.listitem_sub);
		if (_item.isDeleting() || _item.isMoving()) {
			main.setGravity(Gravity.CENTER_HORIZONTAL);
			main.setText(getContext().getString(_item.isDeleting() ? R.string.playlist_deleting : R.string.playlist_moving));
			sub.setGravity(Gravity.CENTER);
			sub.setText(_item.getTitle());
		} else {
			main.setGravity(Gravity.LEFT);
			main.setText(_item.getTitle());
			sub.setGravity(Gravity.LEFT);
			String artistname = _item.getArtist(); 
			sub.setText(artistname == null ? getContext().getString(R.string.unknown_artist) : artistname);
		}
		if (_position == selected) {
			main.setBackgroundColor(Color.parseColor("#80F9F9F9"));
			sub.setBackgroundColor(Color.parseColor("#80F9F9F9"));
		} else {
			main.setBackgroundColor(Color.parseColor("#00000000"));
			sub.setBackgroundColor(Color.parseColor("#00000000"));
		}
		return _view;
	}

	@Override
	protected String getFilename() {
		return null;
	}

	public void showDeleting(int _position) {
		getTItem(_position).setDeleting(true);
		notifyDataSetChanged();
	}

	public void startMove(int _position) {
		getTItem(_position).setMoving(true);
		notifyDataSetChanged();
	}

	public void endMove(int _position) {
		getTItem(_position).setMoving(false);
		notifyDataSetChanged();
	}
		
}
