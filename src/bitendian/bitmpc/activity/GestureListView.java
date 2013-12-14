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

import java.util.ArrayList;

import android.content.Context;
import android.os.AsyncTask;
import android.view.MotionEvent;
import android.widget.ListView;

public abstract class GestureListView extends ListView {

	public interface RemoveListener { public void remove(int _position); }
	public interface DragListener { public void drag(int _position); }
	public interface DropListener { public void drop(int _position); }
	private static class MovingThread extends AsyncTask<GestureListView, Integer, Integer> {
		
		private GestureListView list;
		
		@Override
		protected Integer doInBackground(GestureListView... _params) {
			list = _params[0];
			try {
				Thread.sleep(100);
			} catch (InterruptedException _e) {
			}
			return null;
		}

		@Override
		protected void onPostExecute(Integer _result) {
			cancel(true);
			list.checkScroll(true);
		}

	}

	private static enum Status {NORMAL, DRAG, MOVING};

	private static final float DELETE_GEST_LONG = 80; // delete gesture long in pixel
	private static final float DRAG_GEST_LONG = 80; // drag gesture long in pixel
	private static final float EDGE_GESTURE_ERROR = 5; // percent error in edge detection
	
	private float x;
	private int dragPosition, dragPositionHeight;
	private Status status = Status.NORMAL;
	private ArrayList<RemoveListener> removeListeners;
	private ArrayList<DragListener> dragListeners;
	private ArrayList<DropListener> dropListeners;
	private MovingThread movement;
	
	public GestureListView(Context _context) {
		super(_context);
		removeListeners = new ArrayList<RemoveListener>();
		dragListeners = new ArrayList<DragListener>();
		dropListeners = new ArrayList<DropListener>();
		movement = new MovingThread();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent _ev) {
		switch (_ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			return touchDown(_ev);
		case MotionEvent.ACTION_UP:
			return touchUp(_ev);
		case MotionEvent.ACTION_MOVE:
			// no hacemos la comprobacion en un metodo booleano move para evitar
			// la indireccion. este metodo se ejecuta muchas veces
			if (status != Status.MOVING) return super.onTouchEvent(_ev);
			else touchMoving(_ev);
			return true;
		}
		return super.onTouchEvent(_ev);
	}

	private void touchMoving(MotionEvent _ev) {
		int aux;
		if ((aux = pointToPosition((int)_ev.getX(), (int)_ev.getY())) != dragPosition && aux != INVALID_POSITION) {
			swap(dragPosition, aux);
			dragPosition = aux;
			checkScroll(false);
		}
	}

	private void checkScroll(boolean _iteration) {
		System.out.println(getHeight());
		if ((status == Status.MOVING) && (_iteration || movement.getStatus() != AsyncTask.Status.RUNNING)) {
			if (dragPosition == getFirstVisiblePosition() && dragPosition > 0) {
				swap(dragPosition, dragPosition - 1);
				dragPosition = dragPosition - 1;
				setSelectionFromTop(getFirstVisiblePosition() - 1, 0);
				movement = new MovingThread();
				movement.execute(this);
			} else if (dragPosition == getLastVisiblePosition() && dragPosition < getAdapter().getCount() - 2) {
				swap(dragPosition, dragPosition + 1);
				dragPosition = dragPosition + 1;
				//FIXME que pasa con los paddings?
				setSelectionFromTop(dragPosition, getHeight() - dragPositionHeight);
				movement = new MovingThread();
				movement.execute(this);
			}
		}
	}
	
	protected abstract void swap(int _first, int _second);

	private boolean touchUp(MotionEvent _ev) {
		if ((dragPosition != INVALID_POSITION) && (_ev.getX() - x > DELETE_GEST_LONG) && (_ev.getX() > getWidth() * (100 - EDGE_GESTURE_ERROR) / 100) && (dragPosition == pointToPosition((int)_ev.getX(), (int)_ev.getY()))) for (RemoveListener listener : removeListeners) listener.remove(dragPosition);
		if ((dragPosition != INVALID_POSITION) && (status == Status.NORMAL) && (x - _ev.getX() > DRAG_GEST_LONG) && (_ev.getX() < getWidth() * (EDGE_GESTURE_ERROR / 100)) && (dragPosition == pointToPosition((int)_ev.getX(), (int)_ev.getY()))) drag();
		if (status == Status.MOVING) return drop();
		return super.onTouchEvent(_ev);
	}

	private boolean touchDown(MotionEvent _ev) {
		// pican la pantalla
		switch (status) {
		case DRAG:
			// si estabamos en modo drag comprobamos si pican el que toca o otro
			// si pican el que toca iniciamos modo movimiento
			if ((dragPosition != INVALID_POSITION) && (dragPosition == pointToPosition((int)_ev.getX(), (int)_ev.getY()))) move();
			// si pican otro cancelan, osea dropean en el mismo lugar
			else drop();
			// en cualquier caso nos quedamos con el movimiento
			return true;
		case NORMAL:
			x = _ev.getX();
			dragPosition = pointToPosition((int)_ev.getX(), (int)_ev.getY());
			// iniciamos comprobacion pero liberamos por si no inician gesto
			return super.onTouchEvent(_ev);
		}
		return super.onTouchEvent(_ev);
	}

	private void move() {
		status = Status.MOVING;
		dragPositionHeight = getChildAt(dragPosition - getFirstVisiblePosition()).getHeight();
	}

	private boolean drop() {
		for (DropListener listener : dropListeners) listener.drop(dragPosition);
		status = Status.NORMAL;
		return true;
	}

	private void drag() {
		for (DragListener listener : dragListeners) listener.drag(dragPosition);
		status = Status.DRAG;
	}
	
	public void addRemoveListener(RemoveListener _listener) { removeListeners.add(_listener); }
	public void removeRemoveListener(RemoveListener _listener) { removeListeners.remove(_listener); }
	public void addDragListener(DragListener _listener) { dragListeners.add(_listener); }
	public void removeDragListener(DragListener _listener) { dragListeners.remove(_listener); }
	public void addDropListener(DropListener _listener) { dropListeners.add(_listener); }
	public void removeDropListener(DropListener _listener) { dropListeners.remove(_listener); }
	
}
