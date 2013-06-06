package com.peterallenwebb.somnolence;

import android.graphics.Canvas;
import android.graphics.Color;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class XYThumb extends View {
	
	// Location and appearance.
	public float xPos;
	public float yPos;
	public int color = Color.BLUE;
	
	private int _thumbHeight = 80;
	private int _thumbWidth = 80;
	
	// Events.
	private XYChangeListener _listener;
	
	// Pointer tracking.
	private float _lastTouchX;
	private float _lastTouchY;
	private int _activePointerId;
	
	
	public XYThumb(Context context) {
		super(context);
		setMinimumHeight(_thumbHeight);
		setMinimumWidth(_thumbWidth);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {

		RelativeLayout r = (RelativeLayout)this.getParent();
		int action = ev.getAction();
		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN: {
			float x = ev.getX();
			float y = ev.getY();
			_lastTouchX = x + xPos;
			_lastTouchY = y + yPos;
			_activePointerId = ev.getPointerId(0);
			break;
		}
		case MotionEvent.ACTION_MOVE: {
			// Find the index of the active pointer and fetch its position
			int pointerIndex = ev.findPointerIndex(_activePointerId);
			float x = xPos + ev.getX(pointerIndex);
			float y = yPos + ev.getY(pointerIndex);
			
			float dx = x - _lastTouchX;
			float dy = y - _lastTouchY;

			xPos += dx;
			yPos += dy;
			
			_lastTouchX = x;
			_lastTouchY = y;
			
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(_thumbWidth, _thumbHeight);
			params.leftMargin = (int)x;
			params.topMargin = (int)y;
			r.updateViewLayout(this, params);
			invalidate();
			
			if (_listener != null) {
				_listener.onXYChange(this, xPos / r.getWidth(), yPos / r.getHeight());
			}
			
			break;
		}
		case MotionEvent.ACTION_UP:
			_activePointerId = -1;
			break;
		case MotionEvent.ACTION_CANCEL:
			_activePointerId = -1;
			break;
		case MotionEvent.ACTION_POINTER_UP: {
			// Extract the index of the pointer that left the touch sensor
			final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			final int pointerId = ev.getPointerId(pointerIndex);
			if (pointerId == _activePointerId) {
				// This was our active pointer going up. Choose a new
				// active pointer and adjust accordingly.
				final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
				_lastTouchX = xPos + ev.getX(newPointerIndex);
				_lastTouchY = yPos + ev.getY(newPointerIndex);
				_activePointerId = ev.getPointerId(newPointerIndex);
			}
			break;
		}
		default:
			break;
		}

		return true;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(getSuggestedMinimumWidth(),
				getSuggestedMinimumHeight());
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawColor(color);
	}
	
	public interface XYChangeListener {
		void onXYChange(XYThumb xyThumb, float x, float y);
	}
	
	public void setOnXYChangeListener(XYChangeListener l) {
		_listener = l;
	}
}
