package org.nem.nac.ui.layout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.nem.nac.R;
import org.nem.nac.application.AppHost;

public class ArrowFrameLayout extends FrameLayout {
	private static final int TRIANGLE_SIZE_DP = 10;

	private Path _trianglePath;
	private Paint _trianglePaint;
	private int _height;
	private boolean _showArrow = false;

	public ArrowFrameLayout(Context context) {
		super(context);
		init();
	}

	public ArrowFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ArrowFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public void setActive(boolean active) {
		_showArrow = active;
		int color = active ? getResources().getColor(R.color.white) : getResources().getColor(R.color.white_inactive);
		View view = getChildAt(0);
		if (view != null && view instanceof TextView) {
			((TextView) view).setTextColor(color);
		}
		invalidate();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		_height = h;
		_trianglePath = constructTriangle(new Point(w / 2, h));

		int color = Color.BLACK;
		ViewGroup view = this;
		do {
			Drawable bg = view.getBackground();
			if (bg != null) {
				if (bg instanceof ColorDrawable) {
					color = ((ColorDrawable) bg).getColor();
				}
				break;
			}
			view = (ViewGroup) view.getParent();
		} while (view != null);
		_trianglePaint.setColor(color);

	}

	private Rect _canvasRect = new Rect();

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.getClipBounds(_canvasRect);
		_canvasRect.bottom = _canvasRect.top + _height + ((int)AppHost.Screen.dpToPx(TRIANGLE_SIZE_DP));
		canvas.clipRect(_canvasRect, Region.Op.REPLACE);
		if (_showArrow) {
			canvas.drawPath(_trianglePath, _trianglePaint);
		}
	}

	private void init() {
		setWillNotDraw(false);

		_trianglePaint = new Paint();
		_trianglePaint.setColor(Color.BLACK);
		_trianglePaint.setStyle(Paint.Style.FILL);

		_trianglePath = constructTriangle(new Point(0, 0));
	}

	private Path constructTriangle(final Point location) {
		final int triangleSizePx = ((int)AppHost.Screen.dpToPx(TRIANGLE_SIZE_DP));
		Point p1 = new Point(location.x - triangleSizePx, location.y);
		Point p2 = new Point(location.x + triangleSizePx, location.y);
		Point p3 = new Point(location.x, location.y + triangleSizePx);

		Path path = new Path();
		path.moveTo(p1.x, p1.y);
		path.lineTo(p2.x, p2.y);
		path.lineTo(p3.x, p3.y);
		return path;
	}
}
