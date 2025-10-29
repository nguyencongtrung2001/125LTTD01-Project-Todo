package com.example.projecttodo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

public class PieChartView extends View {

    private Paint completedPaint;
    private Paint incompletePaint;
    private Paint borderPaint; // Thêm viền đen để rõ arc
    private RectF rectF;

    private float completedPercentage = 0f;

    private static final String TAG = "PieChartView";

    public PieChartView(Context context) {
        super(context);
        init();
    }

    public PieChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PieChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Completed: Tím đậm hơn (0xFF800080) để nổi bật trên nền đen
        completedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        completedPaint.setColor(0xFF9F9FEF);
        completedPaint.setStyle(Paint.Style.FILL);


        // Incomplete: Giữ xám nhạt, nhưng tăng tương phản
        incompletePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        incompletePaint.setColor(0xFFE0E0E0);
        incompletePaint.setStyle(Paint.Style.FILL);


        // Viền đen cho toàn pie (dễ thấy ranh giới)
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(0xFF000000); // Đen
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3f);

        rectF = new RectF();
    }

    public void setData(int completed, int total) {
        if (total > 0) {
            this.completedPercentage = (completed * 100f) / total;
        } else {
            this.completedPercentage = 0;
        }
        Log.d(TAG, "Set percentage: " + completedPercentage + "% (completed=" + completed + ", total=" + total + ")");
        invalidate(); // Trigger onDraw
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) {
            Log.w(TAG, "Invalid size: width=" + width + ", height=" + height + " -> Skip draw");
            return;
        }

        Log.d(TAG, "onDraw called: size=" + width + "x" + height + ", percentage=" + completedPercentage);

        int radius = Math.min(width, height) / 2 - 20; // Giảm radius để tránh sát viền
        if (radius <= 0) {
            Log.w(TAG, "Radius too small: " + radius + " -> Skip draw");
            return;
        }

        int centerX = width / 2;
        int centerY = height / 2;

        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        // Vẽ viền ngoài trước (toàn bộ circle)
        canvas.drawCircle(centerX, centerY, radius, borderPaint);

        // Vẽ completed arc (từ top, clockwise)
        float completedAngle = (completedPercentage / 100f) * 360f;
        Log.d(TAG, "Drawing completed arc: angle=" + completedAngle + " degrees");
        canvas.drawArc(rectF, -90, completedAngle, true, completedPaint);

        // Vẽ incomplete arc (tiếp nối)
        float incompleteAngle = 360f - completedAngle;
        Log.d(TAG, "Drawing incomplete arc: angle=" + incompleteAngle + " degrees");
        canvas.drawArc(rectF, -90 + completedAngle, incompleteAngle, true, incompletePaint);

        // Text % ở giữa (tăng size, màu trắng đậm)
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFFFFFFFF); // Trắng
        textPaint.setTextSize(50f); // Tăng size
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true); // Bold
        String percentText = String.format("%.0f%%", completedPercentage);
        // Vẽ background trắng mờ cho text (dễ đọc)
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(0x80000000); // Đen mờ 50%
        canvas.drawCircle(centerX, centerY, 30f, bgPaint); // Vòng tròn bg
        canvas.drawText(percentText, centerX, centerY + 15, textPaint); // Text trên bg
    }
}