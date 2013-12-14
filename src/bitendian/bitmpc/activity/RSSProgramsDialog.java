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
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import bitendian.bitmpc.R;
import bitendian.bitmpc.adapter.RSSItemAdapter;
import bitendian.bitmpc.main.RSSItem;

public class RSSProgramsDialog extends Dialog {

	private BitMPCHandler handler;
	RSSItemAdapter programs;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.rssprograms);
		getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

		programs = handler.getRSSProgramAdapter();
		
		((ListView) findViewById(R.id.rssprogram_list)).setAdapter(programs);
		
		addListeners();
	}

	public RSSProgramsDialog(BitMPC _context) {
		super(_context);
		handler = _context.handler;
	}
	
	private void addListeners() { ((ListView) findViewById(R.id.rssprogram_list)).setOnItemClickListener(new OnItemClickListener() { public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long _arg3) { handler.addURL(((RSSItem) programs.getItem((int) _arg3)).getURL().toString()); } }); }

}
