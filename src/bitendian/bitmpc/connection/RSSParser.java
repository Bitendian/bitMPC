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

import java.net.MalformedURLException;
import java.net.URL;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.os.Message;
import bitendian.bitmpc.activity.BitMPCHandler;
import bitendian.bitmpc.main.RSSItem;

public class RSSParser extends DefaultHandler {

	private boolean items, rss, intitle, initem, indescription, initemtitle, initemdescription, inimage, inurlimage;
	
	private StringBuffer image, title, program_title, description, program_description;
		
	private RSSItem item, program;

	private URL url, program_url;
	
	private BitMPCHandler handler;
	
	public RSSParser(BitMPCHandler _handler, URL _url, boolean _items) {
		handler = _handler;
		url = _url;
		items = _items;
	}
	
	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		rss = false;
		intitle = false;
		initemtitle = false;
		initem = false;
		indescription = false;
		initemdescription = false;
		inurlimage = false;
		title = new StringBuffer();
		description = new StringBuffer();
		image = new StringBuffer();
		item = new RSSItem();
	}
	
	@Override
	public void startElement(String _uri, String _localName, String _qName, Attributes _attributes) throws SAXException {
		super.startElement(_uri, _localName, _qName, _attributes);
		if ("rss".equals(_localName)) rss = true;
		if ("title".equals(_localName)) {
			if (initem) initemtitle = true;
			else if ("".equals(title.toString())) intitle = true;
		}
		if ("description".equals(_localName)) {
			if (initem) initemdescription = true;
			else if ("".equals(description.toString())) indescription = true;
		}
		if ("item".equals(_localName)) {
			initem = true;
			program = new RSSItem();
			program_title = new StringBuffer();
			program_description = new StringBuffer();
			program_url = null;
		}
		if ("enclosure".equals(_localName) && initem) {
			try {
				program_url = new URL(_attributes.getValue("url"));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		if ("image".equals(_localName) && !initem && "".equals(image.toString())) {
			if (_attributes.getIndex("href") != -1)	image.append(_attributes.getValue("href"));
			else inimage = true;
		}
		if ("url".equals(_localName) && inimage) inurlimage = true;
	}

	@Override
	public void characters(char[] _ch, int _start, int _length) throws SAXException {
		if (intitle) title.append(_ch, _start, _length);
		if (indescription) description.append(_ch, _start, _length);
		if (items && initemtitle) program_title.append(_ch, _start, _length);
		if (items && initemdescription) program_description.append(_ch, _start, _length);
		if (inurlimage) image.append(_ch, _start, _length);
	}
	
	@Override
	public void endElement(String _uri, String _localName, String _qName) throws SAXException {
		if ("title".equals(_localName)) {
			intitle = false;
			initemtitle = false;
		}
		if ("item".equals(_localName)) {
			//System.out.println("outitem");
			if (items && rss && !"".equals(program_title) && !"".equals(program_description) && program_url != null) {
				program.setURL(program_url);
				program.setTitle(program_title.toString());
				program.setDescription(program_description.toString());
				handler.sendMessage(Message.obtain(handler, BitMPCHandler.MESSAGE_RSS_PROGRAM, program)); 
			}			
			initem = false;
		}
		if ("description".equals(_localName)) {
			indescription = false;
			initemdescription = false;
		}
		if ("image".equals(_localName)) inimage = false;
		if ("url".equals(_localName) && inimage) inurlimage = false;
	}
		
	@Override
	public void endDocument() throws SAXException {
		if (rss && !"".equals(title) && !"".equals(description)) {
			item.setURL(url);
			item.setTitle(title.toString());
			item.setDescription(description.toString());
			if (!"".equals(image.toString())) item.addImage(image.toString());
			handler.sendMessage(Message.obtain(handler, items ? BitMPCHandler.MESSAGE_UPDATE_RSS : BitMPCHandler.MESSAGE_NEW_RSS, item)); 
		} else {
			handler.sendMessage(Message.obtain(handler, BitMPCHandler.MESSAGE_RSS_ERROR));
		}
	}

}
