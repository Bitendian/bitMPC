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

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import bitendian.bitmpc.R;
import bitendian.bitmpc.activity.BitMPC;
import bitendian.bitmpc.main.BrowseItem;

public class BrowseItemAdapter extends BitMPCAdapter<BrowseItem> {

	public BrowseItemAdapter(BitMPC _context) { super(_context); }

	public void addItem(String _text) { 
		if (_text.startsWith("file") || _text.startsWith("directory") || _text.startsWith("playlist")) addItem(new BrowseItem(_text));
		else getLast().add(_text);
	}

	@Override
	protected View getView(int _position, BrowseItem _item, View _view) {
		if (_view == null) _view = getContext().getLayoutInflater().inflate(R.layout.icon_listitem, null);
		ImageView image = (ImageView) _view.findViewById(R.id.icon_listitem_image);
		switch (_item.getType()) {
		case DIRECTORY:
			image.setImageDrawable(getContext().getResources().getDrawable(R.drawable.browse_folder));
			break;
		case FILE:
			image.setImageDrawable(getContext().getResources().getDrawable(R.drawable.browse_file));
			break;
		case PLAYLIST:
			image.setImageDrawable(getContext().getResources().getDrawable(R.drawable.browse_playlist));
			break;
		default:
			break;
		}
		((TextView) _view.findViewById(R.id.icon_listitem_text)).setText(_item.toString()); 
		return _view;
	}

	@Override
	protected String getFilename() { return null; }

}
