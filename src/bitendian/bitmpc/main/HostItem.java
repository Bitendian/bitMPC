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
package bitendian.bitmpc.main;

import java.io.Serializable;

public class HostItem implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String name;
	
	public String[] ip;
	
	public String password;
	
	public int port;
	
	public boolean auth;
	
	public HostItem(String _name, String[] _ip, int _port, String _password, boolean _auth) {
		name = _name;
		ip = _ip;
		port = _port;
		password = _password;
		auth = _auth;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
