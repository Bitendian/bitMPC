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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import bitendian.bitmpc.R;
import bitendian.bitmpc.activity.GestureListView.DragListener;
import bitendian.bitmpc.activity.GestureListView.DropListener;
import bitendian.bitmpc.activity.GestureListView.RemoveListener;
import bitendian.bitmpc.main.HostItem;

public class BitMPC extends Activity {

	enum StatusIconStatus { PLAY, PAUSE, STOP, CONNECT };
	
	private static final String[] types = { "Artist", "Album", "Title", "Track", "Name", "Genre", "Date", "Composer", "Performer", "Disk", "Filename", "AlbumArtist", "Any", "Query" };		

	private AboutDialog aboutDialog;
	private SettingsDialog settingsDialog;		
	private HostsDialog hostsDialog;
	private HostDialog hostDialog;
	private RSSProgramsDialog programsDialog;
	
	private ProgressDialog progress;
	
	private SharedPreferences preferences;

	private StatusIconStatus currentStatus;
	
	private View control;
	
	private Vibrator vibrator;
	
	private PlaylistGestureListView playlist;
	
	public BitMPCHandler handler;		
	
	private OnTouchListener vibration_listener = new OnTouchListener() {
		
		public boolean onTouch(View _v, MotionEvent _event) {
			if (preferences.getBoolean(SettingsDialog.SETTING_VIBRATION, true) && _event.getAction() == MotionEvent.ACTION_DOWN) vibrator.vibrate(50);
			return false;
		}
	};
	
	public boolean onOptionsItemSelected(MenuItem _item) {
		switch (_item.getItemId()) {
		case R.id.menu_hosts:
			hostsDialog.show();
			return true;
		case R.id.menu_settings:
			settingsDialog.show();
			return true;
		case R.id.menu_about:
			aboutDialog.show();
			return true;
		}
		return false;		
	}
	
	public boolean onContextItemSelected(MenuItem _item) {
		switch (_item.getItemId()) {
		case R.id.playlistmenu_remove:
			handler.playlistRemove(((AdapterContextMenuInfo) _item.getMenuInfo()).position);
			return true;
		case R.id.playlistmenu_clear:
			handler.playlistClear();
			return true;
		case R.id.browsemenu_add:
			handler.browseLongAdd(((AdapterContextMenuInfo) _item.getMenuInfo()).position);
			return true;
		case R.id.rssmenu_remove:
			handler.rssRemove(((AdapterContextMenuInfo) _item.getMenuInfo()).position);
			break;
		}
		return false;
	}
	
	public boolean onCreateOptionsMenu(android.view.Menu _menu) {		
		new MenuInflater(this).inflate(R.menu.main, _menu);
		return true;
	}
	
	void setStatusIcon(StatusIconStatus _value) {
		switch (_value) {
		case CONNECT: 
			((ImageView) findViewById(R.id.status_image)).setImageDrawable(getResources().getDrawable(R.drawable.status_connect));
			break;
		case PAUSE: 
			((ImageView) findViewById(R.id.status_image)).setImageDrawable(getResources().getDrawable(R.drawable.status_pause));
			break;
		case PLAY:
			((ImageView) findViewById(R.id.status_image)).setImageDrawable(getResources().getDrawable(R.drawable.status_play));
			break;
		case STOP:
			((ImageView) findViewById(R.id.status_image)).setImageDrawable(getResources().getDrawable(R.drawable.status_stop));
			break;
		}
		currentStatus = _value;
	}

	// initialize tabs
	private void initTabs() {
	    // setting tabs layouts and indicators
	    TabHost tabhost = (TabHost) findViewById(R.id.tabhost);
	    tabhost.setup();
	    tabhost.addTab(tabhost.newTabSpec("playlist").setIndicator(getString(R.string.tab_playlist), getResources().getDrawable(R.drawable.tab_playlist_selector)).setContent(R.id.tab_playlist));
	    tabhost.addTab(tabhost.newTabSpec("browse").setIndicator(getString(R.string.tab_browse), getResources().getDrawable(R.drawable.tab_browse_selector)).setContent(R.id.tab_browse));
	    tabhost.addTab(tabhost.newTabSpec("search").setIndicator(getString(R.string.tab_search), getResources().getDrawable(R.drawable.tab_search_selector)).setContent(R.id.tab_search));
	    tabhost.addTab(tabhost.newTabSpec("rss").setIndicator(getString(R.string.tab_rss), getResources().getDrawable(R.drawable.tab_rss_selector)).setContent(R.id.tab_rss));
	    
	    // begin at tab 0 "playlist"
	    tabhost.setCurrentTab(0);
	}
	
	// initialize buttons panel
	private void initButtonsPanel() {
	    // adding buttons control panel
	    LayoutInflater inflater = getLayoutInflater();
	    control = inflater.inflate(R.layout.control, null);
	    FrameLayout slider = (FrameLayout) control.findViewById(R.id.sliding_slider);
	    DisplayMetrics dm = new DisplayMetrics();
	    getWindowManager().getDefaultDisplay().getMetrics(dm);
	    
	    slider.setPadding(0, dm.heightPixels - 175, 0, 0);
	    
	    // setting max volume to 100 like mpd
	    ((SeekBar) control.findViewById(R.id.playlist_volume)).setMax(100);
	    // adding panel
	    getWindow().addContentView(control, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	}
	
	// initialize search tab elements
	private void initSearchSubsystem() {
		// registering for context menu
	    registerForContextMenu(findViewById(R.id.search_results));		

	    // setting adapters
	    Spinner type = (Spinner) findViewById(R.id.search_type);
	    ArrayAdapter<CharSequence> spinner_adapter = ArrayAdapter.createFromResource(this, R.array.options, android.R.layout.simple_spinner_item);
	    spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    type.setAdapter(spinner_adapter);
	    type.setSelection(12);
		ListView view = (ListView) findViewById(R.id.search_results);
	    view.setAdapter(handler.getSearchAdapter());

		// adding listeners
		// search button listener
		findViewById(R.id.search_do).setOnClickListener(new OnClickListener() { public void onClick(View _v) { search(); } });
		
		findViewById(R.id.search).setOnKeyListener(new OnKeyListener() {
			
			public boolean onKey(View _v, int _keyCode, KeyEvent _event) {
				if (_keyCode == KeyEvent.KEYCODE_ENTER && _event.getAction() == KeyEvent.ACTION_UP) {
					search();
					return true;
				}
				return false;
			}
		});
		
		ListView results = (ListView) findViewById(R.id.search_results); 
		results.setOnItemClickListener(new OnItemClickListener() { public void onItemClick(AdapterView<?> arg0, View _arg1, int _arg2, long _arg3) { handler.searchAdd((int) _arg3); } });

		((Spinner) findViewById(R.id.search_type)).setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> _arg0, View _arg1, int _arg2, long _arg3) { search(); }

			public void onNothingSelected(AdapterView<?> arg0) { }
		});
	}

	private void initRSSSubsystem() {
		ListView view = (ListView) findViewById(R.id.rss_sites);
	    view.setAdapter(handler.getRSSAdapter());
		
	    programsDialog = new RSSProgramsDialog(this);

	    registerForContextMenu(view);

		view.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> _arg0, View _arg1, int _arg2, long _arg3) {
				handler.rssOpen((int) _arg3);
			}
		});

		findViewById(R.id.rss_add).setOnClickListener(new OnClickListener() { public void onClick(View _v) { new AddURLDialog(BitMPC.this).show(); } });
		
	}

	// initialize playlist subsystem
	private void initPlaylistSubsystem() {
	    // setting adapter
		LinearLayout main = (LinearLayout) findViewById(R.id.tab_playlist);
		playlist = new PlaylistGestureListView(this);
		
		playlist.addRemoveListener(new RemoveListener() { public void remove(int _position) { handler.playlistRemove(_position); } });
		playlist.addDragListener(new DragListener() { public void drag(int _position) { handler.playlistStartMove(_position); } });
		playlist.addDropListener(new DropListener() { public void drop(int _position) { handler.playlistEndMove(_position); } });
		playlist.setAdapter(handler.getPlaylistAdapter());
	    registerForContextMenu(playlist);
		main.addView(playlist);
		
	    // add listeners
	    // control buttons listeners
		OnClickListener buttonsListener = new OnClickListener() {		

			public void onClick(View _v) {
				switch (_v.getId()) {
					case R.id.playlist_previous: handler.previous(); break;  
					case R.id.playlist_stop: handler.stop(); break;  
					case R.id.playlist_play: handler.play(); break;  
					case R.id.playlist_pause: handler.pause(); break;  
					case R.id.playlist_next: handler.next(); break;
					case R.id.playlist_url: new PlayURLDialog(BitMPC.this).show(); break;
				}
			}
		};
	    control.findViewById(R.id.playlist_previous).setOnClickListener(buttonsListener);
	    control.findViewById(R.id.playlist_previous).setOnTouchListener(vibration_listener);
		control.findViewById(R.id.playlist_stop).setOnClickListener(buttonsListener);
		control.findViewById(R.id.playlist_stop).setOnTouchListener(vibration_listener);
		control.findViewById(R.id.playlist_play).setOnClickListener(buttonsListener);
		control.findViewById(R.id.playlist_play).setOnTouchListener(vibration_listener);
		control.findViewById(R.id.playlist_pause).setOnClickListener(buttonsListener);
		control.findViewById(R.id.playlist_pause).setOnTouchListener(vibration_listener);
		control.findViewById(R.id.playlist_next).setOnClickListener(buttonsListener);
		control.findViewById(R.id.playlist_next).setOnTouchListener(vibration_listener);
		control.findViewById(R.id.playlist_url).setOnClickListener(buttonsListener);
		control.findViewById(R.id.playlist_url).setOnTouchListener(vibration_listener);
		
		// volume listener
		((SeekBar) control.findViewById(R.id.playlist_volume)).setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			public void onStopTrackingTouch(SeekBar _seekBar) { handler.setVolume(_seekBar.getProgress()); }
			
			public void onStartTrackingTouch(SeekBar _seekBar) { }
			
			public void onProgressChanged(SeekBar _seekBar, int _progress, boolean _fromUser) { }

		});
		
		// playlist click listener
		playlist.setOnItemClickListener(new OnItemClickListener() { public void onItemClick(AdapterView<?> _arg0, View _arg1, int _arg2, long _arg3) { handler.playlistPlay((int) _arg3); } });
		
		//results.setOnTouchListener(vibration_listener);
		// seek bar listener
		((SeekBar) findViewById(R.id.playlist_seek)).setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			public void onStopTrackingTouch(SeekBar _seekBar) { handler.seek( _seekBar.getProgress()); }
			
			public void onStartTrackingTouch(SeekBar _seekBar) { }
			
			public void onProgressChanged(SeekBar _seekBar, int _progress, boolean _fromUser) { }
			
		});
		
		// repeat
		findViewById(R.id.repeat_icon).setOnClickListener(new OnClickListener() { public void onClick(View v) { handler.repeat(); } });
		
		findViewById(R.id.shuffle_icon).setOnClickListener(new OnClickListener() { public void onClick(View v) { handler.random(); } });
		
		findViewById(R.id.playlist_current).setOnClickListener(new OnClickListener() { public void onClick(View v) { playlist.setSelection(handler.getPlaylistAdapter().getSelected()); } });
		
		ImageView image = (ImageView) findViewById(R.id.status_image); 
		
		image.setOnClickListener(new OnClickListener() { public void onClick(View v) { handler.magicButton(currentStatus); } });
		
		image.setOnLongClickListener(new OnLongClickListener() {
			
			public boolean onLongClick(View v) {
				handler.longMagicButton(currentStatus);
				return true;
			}
		});
		
		image.setOnTouchListener(vibration_listener);
		findViewById(R.id.repeat_icon).setOnTouchListener(vibration_listener);
		findViewById(R.id.shuffle_icon).setOnTouchListener(vibration_listener);
		
	}
	
	// initialize hosts subsystem
	private void initHostsSubsystem() {
		hostsDialog = new HostsDialog(this);
	    hostDialog = new HostDialog(this);
	}
	
	void initializeView() {
		// view values
		((TextView) findViewById(R.id.playlist_current)).setText("");
		((TextView) findViewById(R.id.browse_path)).setText("");
		disableProgressBar();
		toggleHostsDialogButton();
		setStatusIcon(StatusIconStatus.CONNECT);
	}
	
	public SharedPreferences getPreferences() { return preferences; }
	
	@Override
	public void onCreate(Bundle _savedInstanceState) {
	    super.onCreate(_savedInstanceState);

	    preferences = getSharedPreferences("bitmpc", Activity.MODE_PRIVATE);
	    
	    handler = new BitMPCHandler(this);
	    
	    // about dialog
	    aboutDialog = new AboutDialog(this);
	    
	    // creating settings dialog
	    settingsDialog = new SettingsDialog(this);
	    
	    // setting main layout
	    setContentView(R.layout.main);
	    
	    vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	    
	    // init tabs
	    initTabs();
	    
	    // init buttons panel
	    initButtonsPanel();
	    
	    // init browse subsystem
	    initBrowseSubsystem();
	    
	    // init search subsystem
	    initSearchSubsystem();
	    
	    // init search subsystem
	    initRSSSubsystem();
	    
	    // init playlist subsystem
	    initPlaylistSubsystem();
	    
	    // init hosts subsystem
	    initHostsSubsystem();
	    
	    // initialize view
	    initializeView();
	}

	private void initBrowseSubsystem() {
		// registering for context menu
		registerForContextMenu(findViewById(R.id.browse_results));
		ListView results = (ListView) findViewById(R.id.browse_results); 
		
		// setting adapter
		results.setAdapter(handler.getBrowseAdapter());
		
		// item click listener
		results.setOnItemClickListener(new OnItemClickListener() { public void onItemClick(AdapterView<?> arg0, View _arg1, int _arg2, long _arg3) { handler.browseAdd((int) _arg3); } });
		
		//results.setOnTouchListener(vibration_listener);
		//back button listener
		findViewById(R.id.browse_back).setOnClickListener(new OnClickListener() { public void onClick(View _v) { handler.browseBack(); } });
	}

	@Override
	public void onCreateContextMenu(ContextMenu _menu, View _v, ContextMenuInfo _info) {
		System.out.println("EL ID DEL QUE VIENE: " + _v.getId());
		if (_v.getId() == R.id.browse_results) new MenuInflater(this).inflate(R.menu.browse, _menu);
		if (_v == playlist) new MenuInflater(this).inflate(R.menu.playlist, _menu);
		if (_v.getId() == R.id.rss_sites) new MenuInflater(this).inflate(R.menu.rss, _menu);
	}
		
	@Override
	protected void onDestroy() {
		super.onDestroy();
		handler.disconnect(true);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		// cerramos todos los dialogos
		handler.disconnect(true);
		dismissProgressDialog();
		aboutDialog.dismiss();
		hostDialog.dismiss();
		hostsDialog.dismiss();
		settingsDialog.dismiss();
		programsDialog.dismiss();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		handler.connect();
	}
	
	void editHost(HostItem _host) { hostDialog.show(_host); }
	
	void showCreateHostDialog() { hostDialog.show(); }

	private void raiseProgressDialog(String _message) {
		progress = new ProgressDialog(this);
		progress.setMessage(_message);
		progress.show();
	}

	void closeProgressDialog() {
		progress.dismiss();
		progress = null;
	}
	
	private void search() {
		Spinner spinner = (Spinner) findViewById(R.id.search_type);
		EditText text = (EditText) findViewById(R.id.search);
		if (spinner.getSelectedItemPosition() != Spinner.INVALID_POSITION && !"".equals(text.getText().toString()))	handler.search(types[spinner.getSelectedItemPosition()], text.getText().toString().replace("\n", ""));
	}

	void toggleHostsDialogButton() { hostsDialog.updateToggle(); }

	void raiseBrowseProgressDialog() { raiseProgressDialog(getString(R.string.progress_browsing)); }

	void raiseSearchProgressDialog() { raiseProgressDialog(getString(R.string.progress_searching)); }

	void raiseConnectProgressDialog(HostItem _host) { raiseProgressDialog(getString(R.string.progress_connecting) + " " + _host.name); }

	void raiseRSSProgressDialog() { raiseProgressDialog(getString(R.string.progress_searching)); }
	
	void dismissProgressDialog() { if (progress.isShowing()) progress.dismiss(); }

	void showPasswordErrorDialog() {
		MessageDialog dialog = new MessageDialog(this);
		dialog.setMessage(getString(R.string.connection_password_error));
		dialog.show();
	}

	public void showRSSErrorDialog() {
		MessageDialog dialog = new MessageDialog(this);
		dialog.setMessage(getString(R.string.connection_rss_error));
		dialog.show();
	}

	void showRSSURLError() {
		MessageDialog dialog = new MessageDialog(this);
		dialog.setMessage(getString(R.string.url_error));
		dialog.show();
	}

	void showProgramsDialog() { programsDialog.show(); }

	void setCurrentText(String _text) { ((TextView) findViewById(R.id.playlist_current)).setText(_text); }
	
	public void setupSeekBar(int _current, int _max) {
		SeekBar bar = (SeekBar) findViewById(R.id.playlist_seek);
		bar.setEnabled(true);
		bar.setMax(_max);
		bar.setProgress(_current);
	}

	void disableProgressBar() {
		SeekBar bar = (SeekBar) findViewById(R.id.playlist_seek); 
		bar.setProgress(0);
		bar.setEnabled(false);
	}

	void selectRandom(boolean _random) {
		ImageView image = (ImageView) findViewById(R.id.shuffle_icon);
		image.setSelected(_random);
	}

	void selectRepeat(boolean _repeat) {
		ImageView image = (ImageView) findViewById(R.id.repeat_icon);
		image.setSelected(_repeat);
	}

	void setBrowsePath(String _path) { ((TextView) findViewById(R.id.browse_path)).setText(_path); }

	void setVolume(int _volume) { ((SeekBar) control.findViewById(R.id.playlist_volume)).setProgress(_volume); }

	void showNoHostError() {
		MessageDialog dialog = new MessageDialog(this);
		dialog.setMessage(getString(R.string.no_host_error));
		dialog.show();
	}

	public StatusIconStatus getStatusIconValue() {
		return currentStatus;
	}

	public void gotoBrowserPosition(int _position) {
		((ListView) findViewById(R.id.browse_results)).setSelection(_position);
	}

}
