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
package bitendian.bitmpc.connection;

import bitendian.bitmpc.activity.BitMPC;
import bitendian.bitmpc.activity.SettingsDialog;


public class StatusUpdater extends Thread {

	private static BitMPC context;
	private int time;
	private Connection connection;
	
	public StatusUpdater(BitMPC _context, Connection _connection) {
		connection = _connection;
		context = _context;
		time = context.getPreferences().getInt(SettingsDialog.SETTING_STATUS, 1000);
	}
	
	private boolean running = false;
	
	@Override
	public void run() {
		running = true;
		while (running) {
			connection.doStatus();
			try { sleep(context.getPreferences().getInt(SettingsDialog.SETTING_STATUS, time)); } 
			catch (InterruptedException e) { e.printStackTrace(); }
		}
	}
	
	public void end() { running = false; }
	
}
