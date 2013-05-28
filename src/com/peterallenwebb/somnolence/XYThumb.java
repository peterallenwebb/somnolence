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
	private int thumbHeight = 80;
	private int thumbWidth = 80;
	
	// Events.
	private XYChangeListener listener;
	
	// Pointer tracking.
	private float mLastTouchX;
	private float mLastTouchY;
	private int mActivePointerId;
	
	
	public XYThumb(Context context) {
		super(context);
		setMinimumHeight(thumbHeight);
		setMinimumWidth(thumbWidth);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {

		RelativeLayout r = (RelativeLayout)this.getParent();
		int action = ev.getAction();
		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN: {
			float x = ev.getX();
			float y = ev.getY();
			mLastTouchX = x + xPos;
			mLastTouchY = y + yPos;
			mActivePointerId = ev.getPointerId(0);
			break;
		}
		case MotionEvent.ACTION_MOVE: {
			// Find the index of the active pointer and fetch its position
			int pointerIndex = ev.findPointerIndex(mActivePointerId);
			float x = xPos + ev.getX(pointerIndex);
			float y = yPos + ev.getY(pointerIndex);
			
			float dx = x - mLastTouchX;
			float dy = y - mLastTouchY;

			xPos += dx;
			yPos += dy;
			
			mLastTouchX = x;
			mLastTouchY = y;
			
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(thumbWidth, thumbHeight);
			params.leftMargin = (int)x;
			params.topMargin = (int)y;
			r.updateViewLayout(this, params);
			invalidate();
			
			if (listener != null) {
				listener.onXYChange(this, xPos / r.getWidth(), yPos / r.getHeight());
			}
			
			break;
		}
		case MotionEvent.ACTION_UP:
			mActivePointerId = -1;
			break;
		case MotionEvent.ACTION_CANCEL:
			mActivePointerId = -1;
			break;
		case MotionEvent.ACTION_POINTER_UP: {
			// Extract the index of the pointer that left the touch sensor
			final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			final int pointerId = ev.getPointerId(pointerIndex);
			if (pointerId == mActivePointerId) {
				// This was our active pointer going up. Choose a new
				// active pointer and adjust accordingly.
				final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
				mLastTouchX = xPos + ev.getX(newPointerIndex);
				mLastTouchY = yPos + ev.getY(newPointerIndex);
				mActivePointerId = ev.getPointerId(newPointerIndex);
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
		listener = l;
	}
}
