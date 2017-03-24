package com.kj.anim.maskanimation.view;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;

import com.kj.anim.maskanimation.R;

import java.io.InputStream;

/**
 * @author Kang Jian
 * @version 1.0
 * @title ShadeTriangleTotalForRankView
 * @description 三角动画遮罩方案2 (Triangle animation mask scheme 2.)
 * 全图在一起,第一个右边图高度为627,下面左边图高588,最底下三角图
 * (Full figure together, the first on the right side of the figure height is 627, below the left figure is 588, the bottom triangle.)
 * @created 2017/3/23 23:09
 * @changeRecord [修改记录] <br/>
 */

public class ShadeTriangleTotalForRankView extends View {

    public static final String TAG = ShadeTriangleTotalForRankView.class.getSimpleName();

    private long duration;

    private int mImageSource_total = 0;            //三张图在一起的资源id
    private Bitmap mTotal_bitmap;                  //三张图在一张图上的bitmap
    private Paint mPaint_Mask = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mPaint_Pic = new Paint(Paint.ANTI_ALIAS_FLAG);
    private PaintFlagsDrawFilter pfd;
    private Path path;

    private Point currentPoint;                 //控制位置的坐标点
    private Rect src;                           //需要绘图的大小
    private Rect dst;                           //屏幕上绘画的位置
    private int width;                          //当前View的宽度
    private int height;                         //当前View的高度

    public ShadeTriangleTotalForRankView(Context context) {
        super(context);
    }

    public ShadeTriangleTotalForRankView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public ShadeTriangleTotalForRankView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    /**
     * 以最省内存的方式读取本地资源的图片
     * (In the way of reading pictures of local resources in the saving memory.)
     *
     * @param context
     * @param resId
     * @return
     */
    public static Bitmap readBitMap(Context context, int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        //获取资源图片
        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        TypedArray type = context.obtainStyledAttributes(attrs, R.styleable.ShadeTriangleTotalForRankView, 0, 0);
        duration = (long) type.getFloat(R.styleable.ShadeTriangleTotalForRankView_shade_duration, 18000f);
        mImageSource_total = type.getResourceId(R.styleable.ShadeTriangleTotalForRankView_image_total, R.drawable.mask_triangle_total);
        mTotal_bitmap = readBitMap(context, mImageSource_total);
        mPaint_Mask.setStyle(Paint.Style.FILL);
        mPaint_Mask.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        mPaint_Pic.setFilterBitmap(true);
        pfd = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

        src = new Rect();
        dst = new Rect();
        path = new Path();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getMeasuredWidth();
        height = getMeasuredHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.setDrawFilter(pfd);
        if (currentPoint == null) {
            currentPoint = new Point(width, height);
            drawShade(canvas);
            startShadeAnimation();
        } else {
            drawShade(canvas);
        }
    }

    /**
     * 画遮罩效果
     *
     * @param canvas
     */
    private void drawShade(Canvas canvas) {
        int saveFlags = Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG | Canvas.FULL_COLOR_LAYER_SAVE_FLAG | Canvas.CLIP_TO_LAYER_SAVE_FLAG;
        canvas.saveLayer(0, 0, width, height, null, saveFlags);  //这里要用个图层,否则最后遮罩后其余部分会黑
        float offX = currentPoint.getX();
        float offY = currentPoint.getY();
        int offSetX = (int) offX;
        int offSetY = (int) offY;
        int left_offSetY = 350 + (int) (offSetY * 0.3);                                         //左侧图Y方向移动距离
        int right_offSetX = (int) (200 - offSetX * 0.3);                                //右侧图X方向移动距离
        int right_offSetY = (int) (470 - offSetY * 0.3);                                //右侧图Y方向移动距离
        drawImage(canvas, mTotal_bitmap, 0, 0, width, height, 1471, 165);               //先画底图三角
        drawImage(canvas, mTotal_bitmap, 0, 0, width, height, 800, left_offSetY);       //绘制左边的图
        drawImage(canvas, mTotal_bitmap, 0, 0, width, height, right_offSetX, right_offSetY);
        path.reset();
        path.moveTo(0, 0);
        path.lineTo(width, 0);
        path.lineTo(width / 2, height);
        path.close();
        canvas.drawPath(path, mPaint_Mask);
        canvas.restore();
    }

    /**
     * 画图 (draw bitmap)
     *
     * @param canvas
     * @param bitmap
     * @param x      绘制起点 left
     * @param y      绘制起点 top
     * @param w      绘制终点 右上角
     * @param h      绘制终点 右下角
     * @param bx     需要绘图的 left
     * @param by     需要绘图的 top
     */
    private void drawImage(Canvas canvas, Bitmap bitmap, int x, int y, int w, int h, int bx, int by) {
        src.left = bx;
        src.top = by;
        src.right = bx + w;
        src.bottom = by + h;
        dst.left = x;
        dst.top = y;
        dst.right = x + w;
        dst.bottom = y + h;
        canvas.drawBitmap(bitmap, src, dst, mPaint_Pic);
    }

    /**
     * 开启动画 (start animation)
     */
    private void startShadeAnimation() {
        Point startPoint = new Point(width, height);
        Point endPoint = new Point(0, 0);
        ValueAnimator anim = ValueAnimator.ofObject(new PointEvaluator(), startPoint, endPoint, startPoint);            //循环动画过渡
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentPoint = (Point) animation.getAnimatedValue();
                invalidate();
            }
        });
        anim.setInterpolator(new LinearInterpolator());
        anim.setDuration(duration);
        anim.setRepeatMode(ValueAnimator.RESTART);
        anim.setRepeatCount(Animation.INFINITE);
        anim.start();
    }

    /**
     * 导引移动的点 类 (Guide moving point ,Class)
     */
    private class Point {
        private float x;
        private float y;

        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        @Override
        public String toString() {
            return "Point{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }
    }

    /**
     * 计算过渡值的类 (Calculation of transition value class)
     */
    private class PointEvaluator implements TypeEvaluator {
        @Override
        public Object evaluate(float fraction, Object startValue, Object endValue) {
            Point startPoint = (Point) startValue;
            Point endPoint = (Point) endValue;
            float x = startPoint.getX() + fraction * (endPoint.getX() - startPoint.getX());
            float y = startPoint.getY() + fraction * (endPoint.getY() - startPoint.getY());
            Point point = new Point(x, y);
            return point;
        }
    }
}
