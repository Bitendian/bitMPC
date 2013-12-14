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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import bitendian.bitmpc.R;
import bitendian.bitmpc.activity.BitMPC;

public class AddURLDialog extends AlertDialog {

	private final EditText input;
	
	public AddURLDialog(final BitMPC _context) {
		super(_context);
		setTitle(_context.getString(R.string.app_name));  
		setMessage(_context.getString(R.string.input_url));
		input = new EditText(getContext());

		input.setOnKeyListener(new android.view.View.OnKeyListener() {
			
			public boolean onKey(View _v, int _keyCode, KeyEvent _event) {
				if (_keyCode == KeyEvent.KEYCODE_ENTER && _event.getAction() == KeyEvent.ACTION_UP) {
					_context.handler.addRSSURL(input.getText().toString().replace("\n", ""));
					dismiss();
					return true;
				}
				return false;
			}
		});

		setView(input);
		setButton(_context.getString(android.R.string.ok), new OnClickListener() { public void onClick(DialogInterface dialog, int which) { _context.handler.addRSSURL(input.getText().toString()); } });
		setButton2(_context.getString(android.R.string.cancel), new OnClickListener() {	public void onClick(DialogInterface dialog, int which) { dismiss(); } });
	}

}
