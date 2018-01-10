package org.nem.nac.ui;

import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;

import com.annimon.stream.function.BiConsumer;

import java.util.EnumSet;

import timber.log.Timber;

public final class SwipeDetect implements View.OnTouchListener {

	private static final int    MIN_DISTANCE              = 40;
	private static final int    MAX_OTHER_DIRECTION_ERROR = 25;
	private              PointF _touchDown                = new PointF(), _touchUp = new PointF();
	private Direction _swipeDirection = Direction.None;
	private BiConsumer<View, Direction> _swipeDetectedListener;
	private final EnumSet<Direction> _reactTo = EnumSet.allOf(Direction.class);

	public SwipeDetect(boolean reactToHorizontal, boolean reactToVertical) {
		if (!reactToHorizontal) {
			_reactTo.remove(Direction.LR);
			_reactTo.remove(Direction.RL);
		}
		if (!reactToVertical) {
			_reactTo.remove(Direction.BT);
			_reactTo.remove(Direction.TB);
		}
	}

	public Direction getDirection() {
		return _swipeDirection;
	}

	public boolean isSwipeDetected() {
		return _swipeDirection != Direction.None;
	}

	/**
	 * Sets swipe detection listener that will be called on allowed swipes.
	 */
	public SwipeDetect withOnSwipeDetectedListener(final BiConsumer<View, Direction> listener) {
		_swipeDetectedListener = listener;
		return this;
	}

	@Override
	public boolean onTouch(final View v, MotionEvent event) {
		switch (event.getAction()) {
			default:
				return false;
			case MotionEvent.ACTION_DOWN: {
				_touchDown.set(event.getX(), event.getY());
				_swipeDirection = Direction.None;
				return true; // false allow other events like Click to be processed
			}
			case MotionEvent.ACTION_MOVE: {
				_touchUp.set(event.getX(), event.getY());

				float deltaX = _touchDown.x - _touchUp.x;
				float deltaY = _touchDown.y - _touchUp.y;

				// horizontal swipe detection
				if (Math.abs(deltaX) > MIN_DISTANCE && Math.abs(deltaY) < MAX_OTHER_DIRECTION_ERROR) {
					if (deltaX < 0) {
						Timber.d("Swipe right");
						return reactToSwipe(v, Direction.LR);
					}
					if (deltaX > 0) {
						Timber.d("Swipe left");
						return reactToSwipe(v, Direction.RL);
					}
				}
				else if (Math.abs(deltaY) > MIN_DISTANCE && Math.abs(deltaX) < MAX_OTHER_DIRECTION_ERROR) { // vertical swipe detection
					if (deltaY < 0) {
						Timber.d("Swipe down");
						return reactToSwipe(v, Direction.TB);
					}
					if (deltaY > 0) {
						Timber.d("Swipe up");
						return reactToSwipe(v, Direction.BT);
					}
				}
				return false;
			}
		}
	}

	private boolean reactToSwipe(View v, Direction dir) {
		if (_reactTo.contains(dir) && dir != _swipeDirection) {
			if (_swipeDetectedListener != null) {
				_swipeDirection = dir;
				v.post(() -> _swipeDetectedListener.accept(v, dir));
			}
			return true;
		}
		return false;
	}

	public enum Direction {
		/**
		 * Left to Right
		 */
		LR,
		/**
		 * Right to Left
		 */
		RL,
		/**
		 * Top to bottom
		 */
		TB,
		/**
		 * Bottom to Top
		 */
		BT,
		/**
		 * when no action was detected
		 */
		None
	}
}
