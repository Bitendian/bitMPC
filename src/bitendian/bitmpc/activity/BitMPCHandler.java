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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Stack;

import android.os.Handler;
import android.os.Message;
import android.widget.ListAdapter;
import bitendian.bitmpc.activity.BitMPC.StatusIconStatus;
import bitendian.bitmpc.adapter.BrowseItemAdapter;
import bitendian.bitmpc.adapter.HostsAdapter;
import bitendian.bitmpc.adapter.PlaylistItemAdapter;
import bitendian.bitmpc.adapter.RSSItemAdapter;
import bitendian.bitmpc.connection.Connection;
import bitendian.bitmpc.connection.RSSHandler;
import bitendian.bitmpc.connection.StatusUpdater;
import bitendian.bitmpc.main.BrowseItem;
import bitendian.bitmpc.main.HostItem;
import bitendian.bitmpc.main.PlaylistItem;
import bitendian.bitmpc.main.RSSItem;

/**
 * This class is the handler for others threads to communicate with main thread
 * 
 * This class performs all application logic, like a Controller in a 
 * View - Controller pattern. View - Controller connection is performed by 
 * Adapters whose implements the Model.
 * 
 * You must communicate with this class using messages.
 * 
 * @author juanan
 *
 */
public class BitMPCHandler extends Handler {

	public static final int MESSAGE_CONNECTED = 1;
	public static final int MESSAGE_NOT_CONNECTED = 2;
	public static final int MESSAGE_INCORRECT_PASSWORD = 3;
	public static final int MESSAGE_STATUS = 4;
	public static final int MESSAGE_BEGIN_PLAYLIST = 100;
	public static final int MESSAGE_PLAYLIST = 101;
	public static final int MESSAGE_PLAYLIST_UPDATE = 102;
	public static final int MESSAGE_END_PLAYLIST = 103;
	public static final int MESSAGE_BEGIN_BROWSE = 200;
	public static final int MESSAGE_BROWSE = 201;
	public static final int MESSAGE_END_BROWSE = 202;
	public static final int MESSAGE_BEGIN_SEARCH = 300;
	public static final int MESSAGE_SEARCH = 301;
	public static final int MESSAGE_END_SEARCH = 302;
	public static final int MESSAGE_RSS_PROGRAM = 400;
	public static final int MESSAGE_RSS_ERROR = 401;
	public static final int MESSAGE_NEW_RSS = 402;
	public static final int MESSAGE_UPDATE_RSS = 403;
	
	private BitMPC context;

	private Connection connection;
	private StatusUpdater updater;
	
	private boolean checkplaylistsize, connected, updating;

	// browse subsystem
	private BrowseItemAdapter browseAdapter;
	private Stack<String> folders;
	private Stack<Integer> folderPositions;
	
	// search subsystem
	private PlaylistItemAdapter searchAdapter;
	
	// rss subsystem
	private RSSItemAdapter rssAdapter, rssProgramAdapter;
	
	// playlist subsystem
	private PlaylistItemAdapter playlistAdapter;
	private HostsAdapter hostsAdapter;
	private int currentPlaylist, currentSong, currentVolume, currentRSS, movingPosition;
	private boolean currentRepeat, currentRandom, gotolastbrowserposition;

	public BitMPCHandler(BitMPC _context) {
		context = _context;
				
		folders = new Stack<String>();
		folderPositions = new Stack<Integer>();
		
		gotolastbrowserposition = false;
		
		// creating adapters
		// -- volatiles
		playlistAdapter = new PlaylistItemAdapter(_context, true);
		browseAdapter = new BrowseItemAdapter(_context);
		searchAdapter = new PlaylistItemAdapter(_context, false);
		rssProgramAdapter = new RSSItemAdapter(_context);
		
		// -- persistents
		rssAdapter = new RSSItemAdapter(_context);
		rssAdapter.restore();
		hostsAdapter = new HostsAdapter(_context);
		hostsAdapter.restore();
		
		// initialize player values
		initializePlayerValues();
	}

	private void initializePlayerValues() {
		// logic values
		connected = false;
		currentPlaylist = -1;
		currentSong = -1;
		currentVolume = -1;
		currentRepeat = false;
		currentRandom = false;
		
		// volatile adapters
		playlistAdapter.clear();
		browseAdapter.clear();
		searchAdapter.clear();
	}
	
	public void handleMessage(Message _msg) {
		synchronized (this) {
			switch (_msg.what) {
			case MESSAGE_CONNECTED:
				context.dismissProgressDialog();
				connected = true;					
				// root browse
				browse();
				// start status beat
				updater = new StatusUpdater(context, connection);
				updater.start();
			    break;
			case MESSAGE_NOT_CONNECTED:
				context.toggleHostsDialogButton();
				context.dismissProgressDialog();
				break;
			case MESSAGE_INCORRECT_PASSWORD:
				context.showPasswordErrorDialog();
				break;
			case MESSAGE_STATUS:
				String[] parts = ((String) _msg.obj).split(":", 2);
				statusMessage(parts[0].trim(), parts[1].trim());
				break;
			case MESSAGE_BEGIN_PLAYLIST:
				playlistAdapter.clear();
				break;
			case MESSAGE_PLAYLIST:
				playlistAdapter.addItem((String) _msg.obj);
				break;
			case MESSAGE_PLAYLIST_UPDATE:
				playlistAdapter.updateItem((String) _msg.obj);
				break;
			case MESSAGE_END_PLAYLIST:
				checkplaylistsize = true;
				break;
			case MESSAGE_BEGIN_BROWSE:
				browseAdapter.clear();
				context.raiseBrowseProgressDialog();
				break;
			case MESSAGE_BROWSE:
				browseAdapter.addItem((String) _msg.obj);
				break;
			case MESSAGE_END_BROWSE:
				context.dismissProgressDialog();
				browseAdapter.notifyDataSetChanged();
				if (gotolastbrowserposition && folderPositions.size() > 0) {
					gotolastbrowserposition = false;
					context.gotoBrowserPosition(folderPositions.pop());
				}
				break;
			case MESSAGE_BEGIN_SEARCH:
				searchAdapter.clear();
				context.raiseSearchProgressDialog();
				break;
			case MESSAGE_SEARCH:
				searchAdapter.addItem((String) _msg.obj);
				break;
			case MESSAGE_END_SEARCH:
				context.dismissProgressDialog();
				searchAdapter.notifyDataSetChanged();
				break;
			case MESSAGE_RSS_PROGRAM:
				rssProgramAdapter.addItem((RSSItem) _msg.obj);
				break;
			case MESSAGE_RSS_ERROR:
				context.dismissProgressDialog();
				context.showRSSErrorDialog();
				break;
			case MESSAGE_NEW_RSS:
				context.dismissProgressDialog();
				rssAdapter.addItem((RSSItem) _msg.obj);
				rssAdapter.notifyDataSetChanged();
				rssAdapter.save();
				break;
			case MESSAGE_UPDATE_RSS:
				context.dismissProgressDialog();
				rssAdapter.setItem(currentRSS, (RSSItem) _msg.obj);
				rssAdapter.notifyDataSetChanged();
				rssAdapter.save();
				context.showProgramsDialog();
				break;
			}
		}
	}

	private void statusMessage(String _key, String _value) {
		if ("playlist".equals(_key)) {
			int playlist;
			if ((playlist = Integer.parseInt(_value)) != currentPlaylist) {
				if (currentPlaylist == -1) {
					connection.doPlaylist();
				} else {
					updating = true;
					connection.doPlaylistUpdate(currentPlaylist);
				}
				currentPlaylist = playlist;
			}
		}
		if ("song".equals(_key) && !updating) {
			int song;
			if ((song = Integer.parseInt(_value)) != currentSong) {
				if (song < playlistAdapter.getCount()) {
					currentSong = song;
					playlistAdapter.setSelected(song);
					context.setCurrentText(playlistAdapter.getItem(song).toString());
				}
			}
		}
		if ("time".equals(_key)) {
			String[] frac = _value.split(":");
			context.setupSeekBar(Integer.parseInt(frac[0]), Integer.parseInt(frac[1]));
		}
		if ("state".equals(_key)) {
			if ("stop".equals(_value) &&  context.getStatusIconValue() != BitMPC.StatusIconStatus.STOP) {
				context.disableProgressBar();
				context.setStatusIcon(StatusIconStatus.STOP);
			} else if ("play".equals(_value) && context.getStatusIconValue() != BitMPC.StatusIconStatus.PLAY) {
				context.setStatusIcon(StatusIconStatus.PLAY);
			} else if ("pause".equals(_value) && context.getStatusIconValue() != BitMPC.StatusIconStatus.PAUSE) {
				context.setStatusIcon(StatusIconStatus.PAUSE);
			}		
		}
		if ("volume".equals(_key)) {
			int volume;
			if ((volume = Integer.parseInt(_value)) != currentVolume) {
				currentVolume = volume;
				context.setVolume(volume);
			}
		}
		if ("playlistlength".equals(_key) && checkplaylistsize) {
			checkplaylistsize = false;
			int size = Integer.parseInt(_value);
			if (size != playlistAdapter.getCount()) playlistAdapter.head(size);
			playlistAdapter.notifyDataSetChanged();
			updating = false;
			if (context.getStatusIconValue() == StatusIconStatus.STOP || context.getStatusIconValue() == StatusIconStatus.PAUSE) {
				if (playlistAdapter.getSelected() != -1 && playlistAdapter.getSelected() < playlistAdapter.getCount()) {
					PlaylistItem item = (PlaylistItem) playlistAdapter.getItem(playlistAdapter.getSelected());
					if (Integer.parseInt(item.get("Pos")) == currentSong) context.setCurrentText(item.toString());
				} else {
					context.setCurrentText("");
				}
			}
		}
		if ("repeat".equals(_key)) {
			boolean repeat = "1".equals(_value);
			if (repeat != currentRepeat) {
				context.selectRepeat(repeat);
				currentRepeat = repeat;
			}
		}
		if ("random".equals(_key)) {
			boolean random = "1".equals(_value);
			if (random != currentRandom) {
				context.selectRandom(random);
				currentRandom = random;
			}
		}
	}

	/*** CONTROL ***/
	
	// connect system to current host
	void connect() {
		HostItem host;
		if (!connected && ((host = hostsAdapter.getCurrent()) != null)) {
		    context.raiseConnectProgressDialog(host);
			// create connection
		    connection = new Connection(this);
		    // try to connect (asynchronous)
		    connection.connect(host);					
		} else if (hostsAdapter.getCurrent() == null) {
			context.showNoHostError();
		}
	}

	public boolean isConected() { return connected; }

	// disconnect system
	void disconnect(boolean _finish) {
		if (connected) {
			// set system status
			connected = false;
			// kill status updater
			updater.end();
			// close socket
			connection.close();
			
			if (!_finish) {
				initializePlayerValues();
				context.initializeView();
			}
		}
	}

	void previous() { connection.doPrevious(); }
	
	void play() { connection.doPlay(); }
	
	void next() { connection.doNext(); }
	
	void pause() { connection.doPause(); }
	
	void stop() { connection.doStop(); }
	
	void setVolume(int _volume) { connection.doVolume(_volume); }
	
	void seek(int _position) { connection.doSeek(currentSong, _position); }

	void repeat() { connection.doRepeat(!currentRepeat); }
	
	void random() { connection.doRandom(!currentRandom); }

	void magicButton(StatusIconStatus _current) {
		switch (_current) {
		case PLAY:
			pause();
			break;
		case CONNECT:
			connect();					
			break;
		case PAUSE:
		case STOP:
			play();
			break;
		}
	}
	
	void longMagicButton(StatusIconStatus _current) {
		switch (_current) {
		case PAUSE:
		case PLAY:
			stop();
			break;
		case CONNECT:
			connect();
			break;
		case STOP:
			disconnect(false);
		}
	}

	void addURL(String _url) { 
		connection.doAdd(_url);
		if (context.getPreferences().getBoolean(SettingsDialog.SETTING_PLAYONADD, false)) connection.doPlay();
	}
	
	void addRSSURL(String _url) {
		try {
			URL url = new URL(_url);
			context.raiseRSSProgressDialog();
			new RSSHandler().handleRSSURL(this, url, false);
		} catch (MalformedURLException e) {
			context.showRSSURLError();
		}
	}

	/*** PLAYLIST ***/
	
	PlaylistItemAdapter getPlaylistAdapter() { return playlistAdapter; }
	
	void playlistRemove(int _position) { 
		playlistAdapter.showDeleting(_position);
		connection.doDelete(Integer.parseInt(playlistAdapter.getTItem(_position).get("Pos"))); 
	}
	
	void playlistClear() { connection.doClear(); }
	
	void playlistPlay(int _position) { connection.doPlaylistPlay(_position); }
	
	/*** BROWSING ***/

	BrowseItemAdapter getBrowseAdapter() { return browseAdapter; }
	
	void browseLongAdd(int _position) {
		BrowseItem item = browseAdapter.getTItem(_position); 
		switch (item.getType()) {
		case DIRECTORY:
		case FILE:
			connection.doAdd(item.get("file"));
			if (context.getPreferences().getBoolean(SettingsDialog.SETTING_PLAYONADD, false)) connection.doPlay();
			break;
		case PLAYLIST:
			connection.doLoad(item.toString());
			if (context.getPreferences().getBoolean(SettingsDialog.SETTING_PLAYONADD, false)) connection.doPlay();
			break;
		}
	}
	
	void browseAdd(int _position) {
		BrowseItem item = browseAdapter.getTItem(_position); 
		switch (item.getType()) {
		case DIRECTORY:
			folders.push(item.get("file"));
			folderPositions.push(_position);
			browse();
			break;
		case FILE:
			connection.doAdd(item.get("file"));
			if (context.getPreferences().getBoolean(SettingsDialog.SETTING_PLAYONADD, false)) connection.doPlay();
			break;
		case PLAYLIST:
			connection.doLoad(item.toString());
			if (context.getPreferences().getBoolean(SettingsDialog.SETTING_PLAYONADD, false)) connection.doPlay();
			break;
		}
	}
	
	private void browse() {
		// calculating new path
		String path = folders.isEmpty() ? "/" : folders.peek();
		context.setBrowsePath(path);
		// connection must send browse command
		connection.doBrowse(path);
	}
	
	void browseBack() {
		if (!folders.isEmpty()) folders.pop();
		gotolastbrowserposition = true;
		browse();
	}

	/*** RSS ***/

	RSSItemAdapter getRSSAdapter() { return rssAdapter; }
	
	RSSItemAdapter getRSSProgramAdapter() { return rssProgramAdapter; }
	
	void rssRemove(int _position) {
		rssAdapter.removeItem(_position);
		rssAdapter.notifyDataSetChanged();
		rssAdapter.save();
	}
	
	void rssOpen(int _position) {
		currentRSS = _position;
		context.raiseRSSProgressDialog();
		rssProgramAdapter.clear();
		new RSSHandler().handleRSSURL(this, rssAdapter.getTItem(_position).getURL(), true);		
	}
	
	/*** SEARCH ***/
	
	PlaylistItemAdapter getSearchAdapter() { return searchAdapter; }
	
	void search(String _type, String _search) { connection.doSearch(_type, _search); }
	
	void searchAdd(int _position) {
		connection.doAdd(searchAdapter.getTItem(_position).get("file"));
		if (context.getPreferences().getBoolean(SettingsDialog.SETTING_PLAYONADD, false)) connection.doPlay();
	}
	
	/*** HOSTS ***/
	
	void setCurrentHost(int _current) {
		if (connected && _current != hostsAdapter.getCurrentPosition()) disconnect(false);
		hostsAdapter.setCurrent(_current);
		hostsAdapter.save();
	}
	
	void addHost(HostItem _host) {
		hostsAdapter.addItem(_host);
		hostsAdapter.save();
	}

	void updateHost(HostItem _host) { hostsAdapter.save(); }

	void editHost(int _position) { context.editHost(hostsAdapter.getTItem(_position)); }

	void deleteHost(int _position) { 
		hostsAdapter.removeItem(_position); 
		hostsAdapter.notifyDataSetChanged();
	}

	ListAdapter getHostsAdapter() { return hostsAdapter; }

	void newHost() { context.showCreateHostDialog(); }

	public void playlistStartMove(int _position) {
		movingPosition = _position;
		playlistAdapter.startMove(_position);
	}

	public void playlistEndMove(int _position) {
		playlistAdapter.endMove(_position);
		if (_position != movingPosition) connection.doMove(movingPosition, _position);
	}

}
