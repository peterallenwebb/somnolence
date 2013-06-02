package com.peterallenwebb.somnolence;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.FillType;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CoolSlider extends View {

	// Location and appearance.
	private int thumbSize = 100;
	private int thumbPos = 0;
	private float _squareness = 0.5f;

	private Paint borderPaint;
	private Paint fillPaint;

	private Path _thumbPath;

	// Pointer tracking.
	private float mLastTouchX;
	private float mLastTouchY;
	private int mActivePointerId;

	public CoolSlider(Context context) {
		super(context);
		init();
	}

	private void init() {
		setMinimumHeight((int) (thumbSize * 1.20));
		setMinimumWidth(400);

		borderPaint = new Paint();
		borderPaint.setDither(true);
		borderPaint.setColor(0xFF000000);
		borderPaint.setStyle(Paint.Style.STROKE);
		borderPaint.setStrokeJoin(Paint.Join.ROUND);
		borderPaint.setStrokeWidth(3);

		fillPaint = new Paint();
		fillPaint.setDither(true);
		fillPaint.setColor(0xFFFF5500);
		fillPaint.setStyle(Paint.Style.FILL);

		_squareness = 0.5f;
		_thumbPath = updatePath(_squareness);
	}

	public CoolSlider(Context context, AttributeSet atts) {
		super(context, atts);
		init();
	}

	public CoolSlider(Context context, AttributeSet atts, int i) {
		super(context, atts, i);
		init();
	}

	public void setSquareness(float squareness) {
		_squareness = Math.min(squareness, 1.0f);
		_squareness = Math.max(_squareness, 0.0f);

		_thumbPath = updatePath(_squareness);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
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

			// xPos += dx;
			// yPos += dy;

			setSquareness(x / this.getWidth());

			mLastTouchX = x;
			mLastTouchY = y;

			invalidate();

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

	private Path updatePath(float squareness) {
		int segments = 24;
		int perSide = segments / 4;

		float[] px = new float[segments];
		float[] py = new float[segments];

		int curPoint = 0;

		// top of square
		for (int j = 0; j < perSide; j++) {
			px[curPoint] = -1.0f + 2.0f / perSide * j;
			py[curPoint] = 1.0f;
			curPoint++;
		}

		// right of square
		for (int j = 0; j < perSide; j++) {
			px[curPoint] = 1.0f;
			py[curPoint] = 1.0f - 2.0f / perSide * j;
			curPoint++;
		}

		// bottom of square
		for (int j = 0; j < perSide; j++) {
			px[curPoint] = 1.0f - 2.0f / perSide * j;
			py[curPoint] = -1.0f;
			curPoint++;
		}

		// left of square
		for (int j = 0; j < perSide; j++) {
			px[curPoint] = -1.0f;
			py[curPoint] = -1.0f + 2.0f / perSide * j;
			curPoint++;
		}

		for (int j = 0; j < segments; j++) {
			float norm = (float) Math.hypot(px[j], py[j]);
			float factor = (1.0f - squareness) + squareness * norm;
			px[j] = px[j] / norm * factor * thumbSize / 2.0f;
			py[j] = py[j] / norm * factor * thumbSize / 2.0f;
		}

		Path path = new Path();
		path.moveTo(px[0], py[0]);

		for (int j = 1; j < segments; j++) {
			path.lineTo(px[j], py[j]);
		}

		path.close();

		return path;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.save();
		float thumbX = thumbSize / 2.0f;
		float thumbY = this.getHeight() / 2.0f + 3.0f;
		canvas.translate(thumbX, thumbY);
		canvas.drawPath(_thumbPath, fillPaint);
		canvas.drawPath(_thumbPath, borderPaint);
		canvas.restore();
	}
}
