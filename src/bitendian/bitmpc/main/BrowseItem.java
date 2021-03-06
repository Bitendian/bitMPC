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

import java.util.Hashtable;

public class BrowseItem extends Hashtable<String, String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum ItemType { DIRECTORY, FILE, PLAYLIST };

	private ItemType type;
	
	public BrowseItem(String _text) {
		String[] parts = _text.split(":", 2);
		parts[0] = parts[0].trim();
		if ("directory".equals(parts[0])) type = ItemType.DIRECTORY;
		if ("file".equals(parts[0])) type = ItemType.FILE;
		if ("playlist".equals(parts[0])) type = ItemType.PLAYLIST;
		put("file", parts[1].trim());
	}
	
	public void add(String _text) {
		String[] parts = _text.split(":", 2);
		put(parts[0].trim(), parts[1].trim());
	}

	@Override
	public String toString() {
		String[] parts = get("file").split("/");
		return parts[parts.length - 1];
	}

	public ItemType getType() { return type; }
	
}
