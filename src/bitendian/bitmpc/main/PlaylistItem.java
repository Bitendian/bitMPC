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

public class PlaylistItem extends Hashtable<String, String> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PlaylistItem(String _text) {
		String[] parts = _text.split(":", 2);
		put(parts[0].trim(), parts[1].trim());
	}

	public void add(String _text) {
		String[] parts = _text.split(":", 2);
		put(parts[0].trim(), parts[1].trim());
	}

	@Override
	public synchronized String toString() {
		String title = get("Title");
		if (title != null) {
			String artist = get("Artist");
			return (artist != null ? artist + " - " : "") + title;
		}
		String[] parts = get("file").split("/"); 
		return parts[parts.length - 1];
	}

	public synchronized String getTitle() {
		String title = get("Title");
		if (title == null) {
			String[] parts = get("file").split("/"); 
			title = parts[parts.length - 1];			
		}
		return title;
	}
	
	public synchronized String getArtist() {
		String artist = get("Artist");
		String album = get("Album");
		if (artist != null && album != null) artist += " - "  + album;
		return artist;
	}

	public void setDeleting(boolean _deleting) { put("isDeleting", "" + _deleting); }
	
	public boolean isDeleting() { return Boolean.parseBoolean(get("isDeleting")); }

	public void setMoving(boolean _deleting) { put("isMoving", "" + _deleting); }
	
	public boolean isMoving() { return Boolean.parseBoolean(get("isMoving")); }
}
