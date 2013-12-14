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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.CheckBox;
import android.widget.EditText;
import bitendian.bitmpc.R;

/**
 * 
 * Create/Edit Host Dialog
 * 
 * @author juanan
 *
 */
public class SettingsDialog extends Dialog {

	public static final String SETTING_VIBRATION = "vibration";
	public static final String SETTING_PLAYONADD = "playonadd";
	public static final String SETTING_STATUS = "status";
	private SharedPreferences preferences;
	
	private android.view.View.OnClickListener listener = new android.view.View.OnClickListener() {
		
		public void onClick(View _v) {
			switch (_v.getId()) {
			case R.id.settings_button_ok:
				Editor edit = preferences.edit();
				edit.putBoolean(SETTING_VIBRATION, ((CheckBox) findViewById(R.id.settings_vibration)).isChecked());
				edit.putBoolean(SETTING_PLAYONADD, ((CheckBox) findViewById(R.id.settings_playonadd)).isChecked());
				edit.putInt(SETTING_STATUS, Integer.parseInt(((EditText) findViewById(R.id.settings_refresh_time)).getText().toString()));
				System.out.println("el resultado del commit " + edit.commit());
			}
			dismiss();
		}
	};
	
	public SettingsDialog(BitMPC _context) { 
		super(_context); 
		preferences = _context.getPreferences();
	}

	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.settings);

		getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

		((CheckBox) findViewById(R.id.settings_vibration)).setChecked(preferences.getBoolean(SETTING_VIBRATION, true));
		((CheckBox) findViewById(R.id.settings_playonadd)).setChecked(preferences.getBoolean(SETTING_PLAYONADD, false));
		((EditText) findViewById(R.id.settings_refresh_time)).setText("" + preferences.getInt(SETTING_STATUS, 1000));
		
		addListeners();
	}

	private void addListeners() {
		findViewById(R.id.settings_button_ok).setOnClickListener(listener);
		findViewById(R.id.settings_button_cancel).setOnClickListener(listener);
	}
	
}
