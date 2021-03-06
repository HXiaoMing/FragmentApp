package com.fragmentapp.view.water;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.os.Parcelable;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.fragmentapp.R;

/**
 * Created by liuzhen on 2017/12/8.
 */

public class NewWaterBgSurfaceView extends SurfaceView implements
        SurfaceHolder.Callback {

    private Path path = null;

    private int width, height, waveHeight, waterUp = -1;

    private Paint paint;
    /**
     * off = 偏移值，speed = 速度
     */
    private int off1,off2, speed1 = 15,speed2 = 25;

    private Bitmap bitmap;

    private DrawThread drawThread;

    public NewWaterBgSurfaceView(Context context) {
        this(context, null, 0);
    }

    public NewWaterBgSurfaceView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NewWaterBgSurfaceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        getHolder().addCallback(this);
        path = new Path();
        // 初始绘制波纹的画笔
        paint = new Paint();
        // 去除画笔锯齿
        paint.setAntiAlias(true);
        // 设置风格为实线
        paint.setStyle(Paint.Style.FILL);
        // 设置画笔颜色
//        paint.setColor(getResources().getColor(R.color.color_4446b7));

        bitmap = ((BitmapDrawable) (getResources().getDrawable(R.mipmap.icon_moon))).getBitmap();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            width = getWidth();
            height = getHeight();
            waveHeight = height * 3 / 5;//波浪的高度
        }
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        return super.onSaveInstanceState();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawThread = new DrawThread(getHolder());
        drawThread.setRunning(true);
        drawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        bitmap = null;
        boolean retry = true;
        drawThread.setRunning(false);
        while (retry) {
            try {
                drawThread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
        paint = null;
        path = null;
    }

    class DrawThread extends Thread {
        private boolean isRun = false;
        private SurfaceHolder surfaceHolder;

        public DrawThread(SurfaceHolder surfaceHolder) {
            this.surfaceHolder = surfaceHolder;
        }

        public void setRunning(boolean run) {
            isRun = run;
        }

        @Override
        public void run() {
            Canvas canvas = null;
            while (isRun) {
                try {
                    canvas = surfaceHolder.lockCanvas(null);
                    if (canvas != null) {
                        synchronized (surfaceHolder) {
                            //clear
                            canvas.drawColor(getResources().getColor(R.color.transparent), android.graphics.PorterDuff.Mode.CLEAR);
                            paint.setColor(getResources().getColor(R.color.color_ed3159c7));
                            path.reset();
                            //假设屏幕宽度为600，平均分成四分，两个控制点分别定位宽度的四分之三和四分之一，结束点为二分之一
                            path.moveTo(-width + off1, waveHeight);//起点
                            //左边区域，移动的时候连贯,假设max=600
                            path.quadTo(-width * 3 / 4 + off1, waveHeight + 70, -width / 2 + off1, waveHeight);//-450,60
                            path.quadTo(-width / 4 + off1, waveHeight - 70, off1, waveHeight);//-150,-60
                            //根据上面的假设得到两个的控制点距离，x1 = width * 3 / 4，x2 = width / 4 ，和结束点 end = width / 2
                            path.quadTo(width / 4 + off1, waveHeight + 70, width / 2 + off1, waveHeight);//150,-60
                            path.quadTo(width * 3 / 4 + off1, waveHeight - 70, width + off1, waveHeight);//450,60
                            //闭合区域
                            path.lineTo(width, height);
                            path.lineTo(-width, height);//一直到最下方
                            path.close();
                            canvas.drawPath(path, paint);//绘制二级贝塞尔弧形

                            //绘制相交波浪，波形相反
                            paint.setColor(getResources().getColor(R.color.color_ed3159c7));
                            path.reset();
                            path.moveTo(-width + off2, waveHeight);
                            path.quadTo(-width * 3 / 4 + off2, waveHeight - 50, -width / 2 + off2, waveHeight);
                            path.quadTo(-width / 4 + off2, waveHeight + 50, off2, waveHeight);
                            path.quadTo(width / 4 + off2, waveHeight - 50, width / 2 + off2, waveHeight);
                            path.quadTo(width * 3 / 4 + off2, waveHeight + 50, width + off2, waveHeight);
                            path.lineTo(width, height);
                            path.lineTo(-width, height);
                            path.close();
                            canvas.drawPath(path, paint);

//                            //辅助波浪，本身衔接不够平滑，为了追求衔接平滑还是在原来的高度上下降调整
//                            paint.setColor(getResources().getColor(R.color.color_4b12a6d2));
//                            path.reset();
//                            //假设屏幕宽度为600，平均分成四分，两个控制点分别定位宽度的四分之三和四分之一，结束点为二分之一
//                            path.moveTo(-width + off, waveHeight + 100);//起点
//                            //左边区域，移动的时候连贯,假设max=600
//                            path.quadTo(-width / 2 + off, waveHeight - 120, off, waveHeight + 100);//-450,60
//                            path.quadTo(width / 2 + off, waveHeight - 120, width + off, waveHeight + 100);//450,60
//                            //闭合区域
//                            path.lineTo(width, height);
//                            path.lineTo(-width, height);//一直到最下方
//                            path.close();
//                            canvas.drawPath(path, paint);//绘制二级贝塞尔弧形

//                            canvas.drawCircle(-width + off, waveHeight - 400, 5, paint);
//                            canvas.drawCircle(-width / 2 + off, waveHeight - 400, 5, paint);
//                            canvas.drawCircle(off, waveHeight, 5, paint);
//                            canvas.drawCircle(width / 2 + off, waveHeight + 400, 5, paint);
//                            canvas.drawCircle(width + off, waveHeight + 400, 5, paint);
//                            canvas.drawCircle(width + off, waveHeight, 5, paint);
                            //第三条波浪降低高度和深色
//                            int newHeight = waveHeight + 80;
//                            paint.setColor(getResources().getColor(R.color.color_be3c64c7));
//                            path.reset();
//                            path.moveTo(-width + off1, newHeight);
//                            path.quadTo(-width * 3 / 4 + off1, newHeight + 50, -width / 2 + off1, newHeight);
//                            path.quadTo(-width / 4 + off1, newHeight - 50, off1, newHeight);
//                            path.quadTo(width / 4 + off1, newHeight + 50, width / 2 + off1, newHeight);
//                            path.quadTo(width * 3 / 4 + off1, newHeight - 50, width + off1, newHeight);
//                            path.lineTo(width, height);
//                            path.lineTo(-width, height);
//                            path.close();
//                            canvas.drawPath(path, paint);
//                            //第四条波浪
//                            newHeight = waveHeight + 130;
//                            paint.setColor(getResources().getColor(R.color.color_be3c64c7));
//                            path.reset();
//                            path.moveTo(-width + off2, newHeight);
//                            path.quadTo(-width * 3 / 4 + off2, newHeight + 80, -width / 2 + off2, newHeight);
//                            path.quadTo(-width / 4 + off2, newHeight - 80, off2, newHeight);
//                            path.quadTo(width / 4 + off2, newHeight + 80, width / 2 + off2, newHeight);
//                            path.quadTo(width * 3 / 4 + off2, newHeight - 80, width + off2, newHeight);
//                            path.lineTo(width, height);
//                            path.lineTo(-width, height);
//                            path.close();
//                            canvas.drawPath(path, paint);

//                            //贝塞尔坐标，测试红点
//                            canvas.drawCircle(-width + off, waveHeight + 100, 5, paint);
//
//                            canvas.drawCircle(-width * 3 / 4 + off, waveHeight + 100, 5, paint);
//                            canvas.drawCircle(-width / 2 + off, waveHeight, 5, paint);
//
//                            canvas.drawCircle(-width / 4 + off, waveHeight - 100, 5, paint);
//                            canvas.drawCircle(off, waveHeight, 5, paint);
//
//                            canvas.drawCircle(width / 4 + off, waveHeight + 100, 5, paint);
//                            canvas.drawCircle(width / 2 + off, waveHeight, 5, paint);
//
//                            canvas.drawCircle(width * 3 / 4 + off, waveHeight - 100, 5, paint);
//                            canvas.drawCircle(width + off, waveHeight, 5, paint);

                            off1 = off1 + speed1;//移动X控制点
                            if (off1 >= width) {
                                off1 = 0;
                            }
                            off2 = off2 + speed2;//移动X控制点
                            if (off2 >= width) {
                                off2 = 0;
                            }
                            waveHeight = waveHeight + waterUp;
                            //水位上升下降
                            if (waveHeight <= height * 3 / 5 - 50 || waveHeight >= height * 3 / 5 + 50) {
                                waterUp = -waterUp;
                            }


                        }
                    }
                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {
                    if (canvas != null) {
                        canvas.drawBitmap(bitmap, width * 3 / 4, 100, paint);
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }

}
