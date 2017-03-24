package com.kj.anim.maskanimation.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;

import com.kj.anim.maskanimation.R;

import java.io.InputStream;

/**
 * @author kangjian
 * @version 1.0
 * @title ShadeTriangleForRankView
 * @description 三角动画遮罩方案1 (Triangle animation mask scheme 1.)
 * @created 2017/3/23 23:37
 * @changeRecord [修改记录] <br/>
 */

public class ShadeTriangleForRankView extends View {

    RuntimeException mException;
    private int mImageMaskSource = 0;       //底层遮罩
    private int mImageSource_one = 0;       //左边的图形
    private int mImageSource_two = 0;       //右边的图形
    private Bitmap mMask_bitmap;                   //底层遮罩Bitmap
    private Bitmap mSource_one_bitmap;             //左边的图形Bitmap
    private Bitmap mSource_two_bitmap;             //右边的图形Bitmap
    private Paint mPaint_Mask = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Path path;
    private int width;
    private int height;
    private RefreshProgressRunnable mRefreshProgressRunnable;
    private float right_Hz = 0.25f;                 //控制右侧动画速率
    private float left_Hz = 0.2f;                   //控制左侧动画速率
    private float ori_left_y = -500;
    private float des_right_x = -430;
    private float anim_left_y = -500;
    private float anim_right_x = -125;
    private float anim_right_y = -570;

    private boolean flag = false;

    public ShadeTriangleForRankView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public ShadeTriangleForRankView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public ShadeTriangleForRankView(Context context, AttributeSet attrs, int defStyleAttr) {
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

    /**
     * 这个在生命周期中先于onMeasure执行
     *
     * @param visibility
     */
    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (View.GONE == visibility) {
            removeCallbacks(mRefreshProgressRunnable);
        } else {
            removeCallbacks(mRefreshProgressRunnable);
            mRefreshProgressRunnable = new RefreshProgressRunnable();
            post(mRefreshProgressRunnable);
        }
    }

    /**
     * 获取需要绘制的这些图片
     */
    private void init(Context context, AttributeSet attrs, int defStyle) {
        TypedArray type = context.obtainStyledAttributes(attrs, R.styleable.ShadeTriangleView, 0, 0);
        mImageMaskSource = type.getResourceId(R.styleable.ShadeTriangleView_mask_image, R.drawable.mask_triangle);
        mImageSource_one = type.getResourceId(R.styleable.ShadeTriangleView_image_left, R.drawable.mask_source_left);
        mImageSource_two = type.getResourceId(R.styleable.ShadeTriangleView_image_right, R.drawable.mask_source_right);
        type.recycle();

        if (mImageMaskSource == 0 || mImageSource_one == 0 || mImageSource_two == 0) {
            mException = new IllegalArgumentException(type.getPositionDescription() +
                    ": 遮罩动画View-->>The content attribute is required and must refer to a valid image.");
        }

        if (mException != null)
            throw mException;

        mMask_bitmap = readBitMap(context, mImageMaskSource);
        mSource_one_bitmap = readBitMap(context, mImageSource_one);
        mSource_two_bitmap = readBitMap(context, mImageSource_two);
        mPaint_Mask.setColor(Color.WHITE);
        mPaint_Mask.setStyle(Paint.Style.FILL);
        mPaint_Mask.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

        path = new Path();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int saveFlags = Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG | Canvas.FULL_COLOR_LAYER_SAVE_FLAG | Canvas.CLIP_TO_LAYER_SAVE_FLAG;
        canvas.saveLayer(0, 0, width, height, null, saveFlags);  //这里要用个图层,否则最后遮罩后其余部分会黑 (Here want to use a layer, or the last mask after the rest will be black.)
        path.reset();                                            //这行必须加,否则一直重复绘制 (This line must be added, or repeated drawing.)
        path.moveTo(0, 0);
        path.lineTo(width, 0);
        path.lineTo(width / 2, height);
        path.close();
        canvas.drawBitmap(mMask_bitmap, 0, 0, null);
        canvas.drawBitmap(mSource_one_bitmap, -100, anim_left_y, null);
        canvas.drawBitmap(mSource_two_bitmap, anim_right_x, anim_right_y, null);
        canvas.drawPath(path, mPaint_Mask);
        canvas.restore();

    }

    private void calculateLeftUp() {
        anim_left_y += left_Hz;
        anim_right_x -= right_Hz;
        anim_right_y -= right_Hz;
    }

    private void calculateLeftDown() {
        anim_left_y -= left_Hz;
        anim_right_x += right_Hz;
        anim_right_y += right_Hz;
    }

    /**
     * 刷新界面的Runnable
     */
    private class RefreshProgressRunnable implements Runnable {
        public void run() {
            synchronized (ShadeTriangleForRankView.this) {
                long start = System.currentTimeMillis();
                if (anim_left_y <= ori_left_y) {                  //控制增减,此时判断左侧向下,右侧向上
                    flag = true;
                } else if (anim_left_y >= des_right_x) {
                    flag = false;
                }
                if (flag) {
                    calculateLeftUp();
                } else {
                    calculateLeftDown();
                }

                invalidate();

                long gap = 16 - (System.currentTimeMillis() - start);
                postDelayed(this, gap < 0 ? 0 : gap);
            }
        }
    }
}
