package jp.co.spookies.android.papersumo;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

public class PaperSumoActivity extends Activity {
	private PaperSumoView view;
	private int pointerIndex;
	private int state;
	public static final int STATE_INIT = 0;
	public static final int STATE_PLAY = 1;
	public static final int STATE_FINISH = 2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
	}

	/**
	 * 初期化
	 */
	public void init() {
		setContentView(R.layout.main);
		view = new PaperSumoView(this);
		state = STATE_INIT;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (state != STATE_PLAY) {
			return super.onTouchEvent(event);
		}
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_POINTER_DOWN:// primary以外(2本目以降)のtouch
			pointerIndex = event.getActionIndex();
			break;
		default:
			return false;
		}

		// playerのtap領域を触れたか
		for (Rikishi player : view.getPlayers()) {
			if (player.getTapRect().contains(event.getX(pointerIndex),
					event.getY(pointerIndex))) {
				view.onTap(player.getId());
			}
		}
		if (view.isFin()) {
			// 終了画面でのボタンタップの判定
			if (view.getRetryRect().contains((int) event.getX(pointerIndex),
					(int) event.getY(pointerIndex))) {
				init();
			} else if (view.getEndRect().contains(
					(int) event.getX(pointerIndex),
					(int) event.getY(pointerIndex))) {
				finish();
			}
		}
		return true;
	}

	/**
	 * スタートボタン
	 * 
	 * @param v
	 */
	public void onStartClicked(View v) {
		setContentView(view);
		state = STATE_PLAY;
	}
}