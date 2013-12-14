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

import android.app.Dialog;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ToggleButton;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import bitendian.bitmpc.R;

public class HostsDialog extends Dialog {

	private BitMPCHandler handler;
	
	public HostsDialog(BitMPC _context) {
		super(_context);
		handler = _context.handler;
	}
	
	public boolean onMenuItemSelected(int _featureId, MenuItem _item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) _item.getMenuInfo();
		switch (_item.getItemId()) {
		case R.id.hostscontextedit:
			handler.editHost(info.position);
			return true;
		case R.id.hostscontextdelete:
			handler.deleteHost(info.position);
			return true;
		default:
			return super.onMenuItemSelected(_featureId, _item);
		}
	};
	
	@Override
	public void onCreateContextMenu(ContextMenu _menu, View _v, ContextMenuInfo _info) {
		new MenuInflater(getContext()).inflate(R.menu.hosts, _menu);
	}
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.connections);
		getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

		ListView list = (ListView) findViewById(R.id.connections_connections);
		list.setAdapter(handler.getHostsAdapter());
		registerForContextMenu(list);
		
		addListeners();
	}

	private void addListeners() {
		findViewById(R.id.connections_add).setOnClickListener(new android.view.View.OnClickListener() { public void onClick(View _v) { handler.newHost(); } });
		findViewById(R.id.connections_connect).setOnClickListener(new android.view.View.OnClickListener() {
			
			public void onClick(View _v) {
				if (handler.isConected()) handler.disconnect(false);
				else handler.connect();
			}
		});
		((ListView) findViewById(R.id.connections_connections)).setOnItemClickListener(new OnItemClickListener() { public void onItemClick(AdapterView<?> _arg0, View _arg1, int _arg2, long _arg3) { handler.setCurrentHost((int) _arg3); } });	
	}

	@Override
	public void show() {
		super.show();
		updateToggle();		
	}
	
	public void updateToggle() {
		ToggleButton button = (ToggleButton) findViewById(R.id.connections_connect);
		if (button != null) button.setChecked(handler.isConected());
	}
	
}
