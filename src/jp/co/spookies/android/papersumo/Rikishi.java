package jp.co.spookies.android.papersumo;

import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;

/**
 * 力士オブジェクト
 * 
 */
public class Rikishi {
	private RectF tapRect;
	private Bitmap bitmap;
	private int width, height;
	private PointF velocity;
	private int playerId;
	private RectF position;
	private Paint paint;
	private float bottom;
	private boolean oppositeDirection;
	private Random rand = new Random();
	private long jumpTime;
	private final PointF JUMP = new PointF(5.0f, 5.0f);
	private boolean dropFlag;

	/**
	 * 
	 * @param playerId
	 *            プレイヤー番号
	 * @param bitmap
	 *            描画するbitmap
	 * @param position
	 *            初期位置（x座標はcenter,y座標はbottom）
	 * @param tapRect
	 *            tapイベントを受け取るviewの領域
	 * @param oppositeDirection
	 *            trueならx軸逆向きに進む
	 */
	public Rikishi(int playerId, Bitmap bitmap, PointF position, RectF tapRect,
			boolean oppositeDirection) {
		this.playerId = playerId;
		this.bitmap = bitmap;
		width = bitmap.getWidth();
		height = bitmap.getHeight();
		bottom = position.y;
		this.position = new RectF(position.x - width / 2, position.y - height,
				position.x + width / 2, position.y);
		this.tapRect = tapRect;
		velocity = new PointF(0.0f, 0.0f);
		paint = new Paint();
		paint.setAntiAlias(true);
		this.oppositeDirection = oppositeDirection;
		dropFlag = false;
	}

	public int getId() {
		return playerId;
	}

	public RectF getTapRect() {
		return tapRect;
	}

	public RectF getPositionRect() {
		return position;
	}

	public PointF getVelocity() {
		return velocity;
	}

	public void draw(Canvas canvas) {
		canvas.drawBitmap(bitmap, position.left, position.top, paint);
	}

	/**
	 * 可能なら（ジャンプ中でなければ）ジャンプする
	 */
	public void jump() {
		if (!isJumping()) {
			// 横方向はランダムに
			velocity.x = rand.nextFloat()
					* (oppositeDirection ? -JUMP.x : JUMP.x);
			velocity.y = -JUMP.y;

			// 着地してすぐのジャンプなら大きくとぶ
			if (System.currentTimeMillis() - jumpTime < 160) {
				velocity.x *= 2.5f;
				velocity.y *= 2.0f;
			}
		}
	}

	/**
	 * 落下させる
	 */
	public void drop() {
		dropFlag = true;
	}

	/**
	 * 毎フレームの更新処理
	 */
	public void update() {
		position.offset(velocity.x, velocity.y);

		if (isJumping()) {
			// ジャンプ中の軌道
			velocity.y += 1.0f;
			jumpTime = System.currentTimeMillis();
		} else {
			// 位置補正
			position.offsetTo(position.left, position.bottom - height);
			velocity.set(0.0f, 0.0f);
		}
	}

	/**
	 * 衝突処理
	 * 
	 * @param force
	 *            衝突後の速さ
	 * @param collisionX
	 *            衝突した座標
	 */
	public void onCollision(float force, float collisionX) {
		// 位置補正
		position.offsetTo(
				((collisionX < position.centerX()) ? collisionX + 2.0f
						: (collisionX - width - 2.0f)), position.top);
		// 速度交換
		velocity.x = force;
	}

	/**
	 * ジャンプ中かの判定
	 * 
	 * @return ジャンプ中ならtrue
	 */
	private boolean isJumping() {
		return position.bottom < bottom || dropFlag;
	}

	/**
	 * 落下しているか
	 * 
	 * @return dropメソッドが呼ばれていればtrue
	 */
	public boolean isLose() {
		return dropFlag;
	}
}
