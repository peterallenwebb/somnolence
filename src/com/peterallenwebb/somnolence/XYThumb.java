package com.peterallenwebb.somnolence;

import android.graphics.Canvas;
import android.graphics.Color;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class XYThumb extends View {

	private int thumbHeight = 40;
	private int thumbWidth = 40;
	
	public int xPos;
	public int yPos;
	
	public XYThumb(Context context) {
		super(context);
		setMinimumHeight(thumbHeight);
		setMinimumWidth(thumbWidth);
	}

	private float mLastTouchX;
	private float mLastTouchY;
	private int mActivePointerId;

	@Override
	public boolean onTouchEvent(MotionEvent ev) {

		RelativeLayout r = null;
		int action = ev.getAction();
		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN: {
			float x = ev.getX();
			float y = ev.getY();
			mLastTouchX = x;
			mLastTouchY = y;
			mActivePointerId = ev.getPointerId(0);
			break;
		}
		case MotionEvent.ACTION_MOVE: {
			// Find the index of the active pointer and fetch its position
			int pointerIndex = ev.findPointerIndex(mActivePointerId);
			float x = ev.getX(pointerIndex);
			float y = ev.getY(pointerIndex);

			float dx = x - mLastTouchX;
			float dy = y - mLastTouchY;

			xPos += dx;
			yPos += dy;
			
			mLastTouchX = x;
			mLastTouchY = y;
			
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(40, 40);
			params.leftMargin = xPos;
			params.topMargin = yPos;
			r.updateViewLayout(this, params);

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
				mLastTouchX = ev.getX(newPointerIndex);
				mLastTouchY = ev.getY(newPointerIndex);
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
		canvas.drawColor(Color.CYAN);
	}

}
