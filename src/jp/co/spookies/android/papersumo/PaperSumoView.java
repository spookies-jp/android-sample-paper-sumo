package jp.co.spookies.android.papersumo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class PaperSumoView extends SurfaceView implements
		SurfaceHolder.Callback, Runnable {
	private int width, height;
	private int finCount;
	private Thread thread;
	private Paint paint;
	private Canvas canvas;
	private Rikishi[] players;
	private final float dohyoRange = 0.50f;
	private RectF canvasRect;
	private Bitmap bgImage = BitmapFactory.decodeResource(getResources(),
			R.drawable.bg);
	private Bitmap dohyoImage = BitmapFactory.decodeResource(getResources(),
			R.drawable.dohyo);
	private RectF dohyoRect;
	private Bitmap[] leftImages = {
			BitmapFactory.decodeResource(getResources(), R.drawable.ltap),
			BitmapFactory.decodeResource(getResources(), R.drawable.lwin),
			BitmapFactory.decodeResource(getResources(), R.drawable.llose), };
	private Bitmap[] rightImages = {
			BitmapFactory.decodeResource(getResources(), R.drawable.rtap),
			BitmapFactory.decodeResource(getResources(), R.drawable.rwin),
			BitmapFactory.decodeResource(getResources(), R.drawable.rlose), };
	private RectF rightRect, leftRect;
	private Bitmap retryImage = BitmapFactory.decodeResource(getResources(),
			R.drawable.retry);
	private Rect retryRect;
	private Bitmap endImage = BitmapFactory.decodeResource(getResources(),
			R.drawable.end);
	private Rect endRect;

	public PaperSumoView(Context context) {
		super(context);
		getHolder().addCallback(this);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		width = getWidth();
		height = getHeight();

		// paint初期化
		paint = new Paint();
		paint.setStrokeWidth(2.0f);
		paint.setAntiAlias(true);

		// 画面
		canvasRect = new RectF(0, 0, width, height);
		// 土俵領域
		dohyoRect = new RectF(0, height - dohyoImage.getHeight(), width, height);
		// Player1のタップ領域
		leftRect = new RectF(0, 0, leftImages[0].getWidth(), height);
		// Player2のタップ領域
		rightRect = new RectF(width - rightImages[0].getWidth(), 0, width,
				height);

		// 終了画面のボタン領域
		retryRect = new Rect((width - retryImage.getWidth()) / 2, height / 2
				- retryImage.getHeight() - 5,
				(width + retryImage.getWidth()) / 2, height / 2 - 5);
		endRect = new Rect((width - endImage.getWidth()) / 2, height / 2 + 5,
				(width + endImage.getWidth()) / 2, height / 2
						+ endImage.getHeight() + 5);

		// カエル初期化
		players = new Rikishi[] {
				new Rikishi(0, BitmapFactory.decodeResource(getResources(),
						R.drawable.kaeru1), new PointF(width / 2 - 50.0f,
						dohyoRect.top), new RectF(0, 0, width / 2, height),
						false),
				new Rikishi(1, BitmapFactory.decodeResource(getResources(),
						R.drawable.kaeru2), new PointF(width / 2 + 50.0f,
						dohyoRect.top), new RectF(width / 2 + 1, 0, width,
						height), true) };

		if (thread == null) {
			finCount = 0;
			thread = new Thread(this);
			thread.start();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		thread = null;
	}

	@Override
	public void run() {
		while (thread != null) {
			update();
			doDraw();
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void doDraw() {
		canvas = getHolder().lockCanvas();
		canvas.drawBitmap(bgImage, null, canvasRect, paint);
		canvas.drawBitmap(dohyoImage, null, dohyoRect, paint);

		// player描画
		for (Rikishi player : players) {
			player.draw(canvas);
		}

		if (finCount > 100) {
			// 終了画面
			canvas.drawColor(Color.argb(127, 0, 0, 0));
			canvas.drawBitmap(retryImage, retryRect.left, retryRect.top, paint);
			canvas.drawBitmap(endImage, endRect.left, endRect.top, paint);
		} else if (finCount > 40) {
			// タップ画像を勝ち負けの画像に変更
			if (players[0].isLose()) {
				canvas.drawBitmap(leftImages[2], null, leftRect, paint);
				canvas.drawBitmap(rightImages[1], null, rightRect, paint);
			} else if (players[1].isLose()) {
				canvas.drawBitmap(leftImages[1], null, leftRect, paint);
				canvas.drawBitmap(rightImages[2], null, rightRect, paint);
			}
		} else {
			// タップ画像
			canvas.drawBitmap(leftImages[0], null, leftRect, paint);
			canvas.drawBitmap(rightImages[0], null, rightRect, paint);
		}
		getHolder().unlockCanvasAndPost(canvas);
	}

	/**
	 * 更新処理
	 */
	private void update() {
		for (Rikishi player : players) {
			player.update();
			// 終了判定
			if (player.getPositionRect().right < width * (1 - dohyoRange) / 2
					|| width * (1 + dohyoRange) / 2 < player.getPositionRect().left) {
				player.drop();
				finCount++;
			}
		}

		// 衝突判定
		for (int i = 0; i < players.length - 1; i++) {
			for (int j = i + 1; j < players.length; j++) {
				if (RectF.intersects(players[i].getPositionRect(),
						players[j].getPositionRect())) {
					float v = players[i].getVelocity().x;

					// 衝突地点
					float collisionX;
					if (players[i].getPositionRect().centerX() < players[j]
							.getPositionRect().centerX()) {
						collisionX = (players[i].getPositionRect().right + players[j]
								.getPositionRect().left) / 2.0f;
					} else {
						collisionX = (players[i].getPositionRect().left + players[j]
								.getPositionRect().right) / 2.0f;
					}

					// 衝突を通知
					players[i].onCollision(players[j].getVelocity().x,
							collisionX);
					players[j].onCollision(v, collisionX);
				}
			}
		}
	}

	/**
	 * タップされたplayerはジャンプする
	 * 
	 * @param playerId
	 *            タップしたplayerのid
	 */
	public void onTap(int playerId) {
		players[playerId].jump();
	}

	public Rikishi[] getPlayers() {
		return players;
	}

	public Rect getRetryRect() {
		return retryRect;
	}

	public Rect getEndRect() {
		return endRect;
	}

	public boolean isFin() {
		return finCount > 100;
	}

}
