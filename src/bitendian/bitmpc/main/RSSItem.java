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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;


public class RSSItem implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
 	
	private BitmapDrawable image = null;
	
	private URL url;
	
	private String title, description;
	
	public Drawable getImage() {
		return image;
	}
	
	public void addImage(String _url) {
		try {
			URL urlobject = new URL(_url);
			InputStream is = (InputStream) urlobject.getContent();
			image = (BitmapDrawable) Drawable.createFromStream(is, "src");
			is.close();
		} catch (IOException e) {
		}
	}

	public void setTitle(String _title) {
		title = _title;
	}
	
	public String getTitle() {
		return title;
	}
	
	@Override
	public String toString() {
		return title;
	}

	public void setDescription(String _description) {
		description = _description;
	}
	
	public String getDescription() {
		return description;
	}
	
	private void writeObject(java.io.ObjectOutputStream _out) throws IOException {
		_out.writeObject(url);
		_out.writeInt(title.getBytes().length);
		_out.write(title.getBytes());
		_out.flush();
		_out.writeInt(description.getBytes().length);
		_out.write(description.getBytes());
		_out.flush();
		if (image != null) {
			ByteArrayOutputStream test = new ByteArrayOutputStream();
			if (image.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, test)) {
				_out.writeBoolean(true);
				image.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, _out);
			} else {
				_out.writeBoolean(false);
			}
		} else {
			_out.writeBoolean(false);			
		}
	}

	private void readObject(java.io.ObjectInputStream _in) throws IOException, ClassNotFoundException {
		url = (URL) _in.readObject();
		int size = _in.readInt();
		byte[] bytes = new byte[512];

		title = "";
		int readed = 0;
		while (readed < size) {
			int buffersize = _in.read(bytes, 0, Math.min(size - readed, bytes.length));
			title += new String(bytes, 0, buffersize);
			readed += buffersize;
		}
		
		size = _in.readInt();
		description = "";
		readed = 0;
		while (readed < size) {
			int buffersize = _in.read(bytes, 0, Math.min(size - readed, bytes.length));
			description += new String(bytes, 0, buffersize);
			readed += buffersize;
		}

		if (_in.readBoolean()) { image = new BitmapDrawable(Bitmap.createBitmap(BitmapFactory.decodeStream(_in))); }
	}

	public void setURL(URL _url) {
		url = _url;
	}
	
	public URL getURL() {
		return url;
	}

}
