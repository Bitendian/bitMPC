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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedList;

import android.os.Message;
import bitendian.bitmpc.activity.BitMPCHandler;
import bitendian.bitmpc.connection.Command.Response;
import bitendian.bitmpc.main.HostItem;

public class Connection extends Thread {
		
	private HostItem host;
	private Socket socket;
	private BitMPCHandler handler;
	private LinkedList<Command> queue = new LinkedList<Command>();
	private BufferedReader in;
	private BufferedWriter out;
		
	public Connection(BitMPCHandler _handler) {
		handler = _handler;
	}
	
	private void sendCommand(String _command) {
		try {
			out.write(_command + "\n");
			out.flush();
		} catch (IOException _e) {
			_e.printStackTrace();
		}
	}

	private void prepareResponse(Response _response) {
		switch (_response) {
		case PLAYLIST: 
			handler.sendMessage(Message.obtain(handler, BitMPCHandler.MESSAGE_BEGIN_PLAYLIST)); 
			break;
		case BROWSE: 
			handler.sendMessage(Message.obtain(handler, BitMPCHandler.MESSAGE_BEGIN_BROWSE));
			break;
		case SEARCH: 
			handler.sendMessage(Message.obtain(handler, BitMPCHandler.MESSAGE_BEGIN_SEARCH));
			break;
		}		
	}

	private void responseReaded(Response _response) {
		switch (_response) {
		case BROWSE:
			handler.sendMessage(Message.obtain(handler, BitMPCHandler.MESSAGE_END_BROWSE));
			break;
		case SEARCH:
			handler.sendMessage(Message.obtain(handler, BitMPCHandler.MESSAGE_END_SEARCH));
			break;
		case PLAYLIST:
		case PLAYLIST_UPDATE:
			handler.sendMessage(Message.obtain(handler, BitMPCHandler.MESSAGE_END_PLAYLIST));
			break;
		}			
	}
	
	private void readResponse(Response _response) {
		try {
			String line = in.readLine();
			while (!line.equals("OK")) {
				switch (_response) {
				case STATUS: 
					handler.sendMessage(Message.obtain(handler, BitMPCHandler.MESSAGE_STATUS, line));
					break;
				case PLAYLIST: 
					handler.sendMessage(Message.obtain(handler, BitMPCHandler.MESSAGE_PLAYLIST, line));
					break;
				case PLAYLIST_UPDATE: 
					handler.sendMessage(Message.obtain(handler, BitMPCHandler.MESSAGE_PLAYLIST_UPDATE, line));
					break;
				case BROWSE: 
					handler.sendMessage(Message.obtain(handler, BitMPCHandler.MESSAGE_BROWSE, line));
					break;
				case SEARCH: 
					handler.sendMessage(Message.obtain(handler, BitMPCHandler.MESSAGE_SEARCH, line));
					break;
				}
				line = in.readLine();
			}
		} catch (IOException _e) {
			_e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		try {
			String ip = host.ip[0] + "." + host.ip[1] + "." + host.ip[2] + "." + host.ip[3]; 
			socket = new Socket();
			socket.connect(new InetSocketAddress(ip, host.port), 5000);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			if (in.readLine().startsWith("OK")) {
				if (host.auth) {
					out.write("password " + host.password + "\n");
					out.flush();
					if (!in.readLine().startsWith("OK")) handler.sendMessage(Message.obtain(handler, BitMPCHandler.MESSAGE_INCORRECT_PASSWORD));
				}
				handler.sendMessage(Message.obtain(handler, BitMPCHandler.MESSAGE_CONNECTED));
				while (true) {
					// esperamos un comando minimizando consumo
					while (queue.size() == 0) {
						try { sleep(100); } 
						catch (InterruptedException _e) { }
					}
					// comando actual
					Command command = queue.remove();
					// preparacion para el resultado
					prepareResponse(command.response);
					// envio del comando
					sendCommand(command.command);
					// lectura del resultado
					readResponse(command.response);
					// final de la lectura
					responseReaded(command.response);
				}
			}
		} catch (IOException _e) {
			handler.sendMessage(Message.obtain(handler, BitMPCHandler.MESSAGE_NOT_CONNECTED));
		}
	}
	
	public void connect(HostItem _host) {
		host = _host;
		start();
	}

	public void close() { 
		try {
			queue.clear(); 
			socket.close();
		} catch (IOException _e) {
			// TODO
			_e.printStackTrace();
		} 
	}

	public void doPlay() { 
		queue.add(new Command("play", Response.ACK)); 
		queue.add(new Command("status ", Response.STATUS)); 		
	}
	
	public void doPause() { 
		queue.add(new Command("pause", Response.ACK)); 
		queue.add(new Command("status ", Response.STATUS)); 		
	}
	
	public void doStop() {  
		queue.add(new Command("stop", Response.ACK)); 
		queue.add(new Command("status ", Response.STATUS)); 		
	}
	
	public void doNext() {  
		queue.add(new Command("next", Response.ACK)); 
		queue.add(new Command("status ", Response.STATUS)); 		
	}
	
	public void doPrevious() { 
		queue.add(new Command("previous", Response.ACK)); 
		queue.add(new Command("status ", Response.STATUS)); 		
	}

	public void doClear() { 
		queue.add(new Command("clear", Response.ACK)); 
		queue.add(new Command("status ", Response.STATUS)); 		
	}
	
	public void doDelete(int _position) { 
		queue.add(new Command("delete " + _position, Response.ACK)); 
		queue.add(new Command("status ", Response.STATUS)); 		
	}

	public void doPlaylist() { 
		queue.add(new Command("playlistinfo", Response.PLAYLIST)); 
		queue.add(new Command("status ", Response.STATUS)); 		
	}

	public void doPlaylistPlay(int _item) { 
		queue.add(new Command("play " + _item, Response.ACK)); 
		queue.add(new Command("status ", Response.STATUS)); 		
	}

	public void doStatus() { queue.add(new Command("status", Response.STATUS)); }

	public void doBrowse(String _path) { queue.add(new Command("lsinfo \"" + _path + "\"", Response.BROWSE)); }

	public void doSearch(String _type, String _search) { queue.add(new Command("search " + _type + " \"" + _search + "\"", Response.SEARCH)); }

	public void doAdd(String _file) { 
		queue.add(new Command("add \"" + _file + "\"", Response.ACK));
		queue.add(new Command("status ", Response.STATUS)); 		
	}

	public void doPlay(String _file) { 
		queue.add(new Command("play \"" + _file + "\"", Response.ACK)); 
		queue.add(new Command("status ", Response.STATUS)); 		
	}

	public void doSeek(int _song, int _progress) { 
		queue.add(new Command("seek " +  _song + " " + _progress, Response.ACK)); 
		queue.add(new Command("status ", Response.STATUS)); 		
	}

	public void doVolume(int _volume) { 
		queue.add(new Command("setvol " +  _volume, Response.ACK)); 
		queue.add(new Command("status ", Response.STATUS)); 		
	}

	public void doLoad(String _path) { 
		queue.add(new Command("load \"" +  _path + "\"", Response.ACK)); 
		queue.add(new Command("status ", Response.STATUS)); 		
	}

	public void doPlaylistUpdate(int _current) { 
		queue.add(new Command("plchanges " +  _current, Response.PLAYLIST_UPDATE)); 
		queue.add(new Command("status ", Response.STATUS)); 		
	}

	public void doRandom(boolean _value) { 
		queue.add(new Command("random " +  (_value ? "1" : "0"), Response.ACK)); 
		queue.add(new Command("status ", Response.STATUS)); 		
	}
	
	public void doRepeat(boolean _value) { 
		queue.add(new Command("repeat " +  (_value ? "1" : "0"), Response.ACK)); 
		queue.add(new Command("status ", Response.STATUS)); 		
	}

	public void doMove(int _from, int _to) {
		queue.add(new Command("move " +  _from + " " + _to, Response.ACK)); 
		queue.add(new Command("status ", Response.STATUS)); 				
	}
	
}
