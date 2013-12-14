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

import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.os.Message;
import bitendian.bitmpc.activity.BitMPCHandler;

public class RSSHandler extends Thread {

	private URL url;
	private boolean items;
	private BitMPCHandler handler;
	
	public void handleRSSURL(BitMPCHandler _handler, URL _url, boolean _items) {
		handler = _handler;
		url = _url;
		items = _items;
		// para no hacer tantas llamadas al getter
		start();
	}

	@Override
	public void run() {
		try {
			/* Get a SAXParser from the SAXPArserFactory. */
	        SAXParserFactory spf = SAXParserFactory.newInstance();
	        SAXParser sp = spf.newSAXParser();
	
	        /* Get the XMLReader of the SAXParser we created. */
	        XMLReader xr = sp.getXMLReader();
	        /* Create a new ContentHandler and apply it to the XML-Reader*/
	        RSSParser parser = new RSSParser(handler, url, items);
	        xr.setContentHandler(parser);
	        
	        /* Parse the xml-data from our URL. */
	        xr.parse(new InputSource(url.openStream()));
		} catch (SAXException _e) {
			handler.sendMessage(Message.obtain(handler, BitMPCHandler.MESSAGE_RSS_ERROR));
		} catch (ParserConfigurationException _e) {
			handler.sendMessage(Message.obtain(handler, BitMPCHandler.MESSAGE_RSS_ERROR));
		} catch (IOException _e) {
			handler.sendMessage(Message.obtain(handler, BitMPCHandler.MESSAGE_RSS_ERROR));
		}
	}
	
}
