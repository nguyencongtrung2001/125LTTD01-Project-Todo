package com.example.projecttodo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

public class PieChartView extends View {

    private Paint completedPaint;
    private Paint incompletePaint;
    private Paint textPaint;

    private RectF rectF;

    private float completedPercentage = 0f;

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

        // === Lấy màu từ ?attr/colorPrimary của app ===
        TypedValue typedValue = new TypedValue();
        getContext().getTheme().resolveAttribute(
                androidx.appcompat.R.attr.colorPrimary,
                typedValue,
                true
        );
        int primaryColor = typedValue.data;

        // Completed color (màu chủ đạo của app)
        completedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        completedPaint.setColor(primaryColor);
        completedPaint.setStyle(Paint.Style.FILL);

        // Incomplete color (xám nhạt)
        incompletePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        incompletePaint.setColor(0xFFE0E0E0);
        incompletePaint.setStyle(Paint.Style.FILL);

        // Text % giữa biểu đồ
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFF000000);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(48f);
        textPaint.setFakeBoldText(true);

        rectF = new RectF();
    }


    public void setData(int completed, int total) {
        if (total > 0) {
            this.completedPercentage = (completed * 100f) / total;
        } else {
            this.completedPercentage = 0;
        }
        invalidate(); // refresh UI
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        int radius = Math.min(width, height) / 2 - 20;

        int centerX = width / 2;
        int centerY = height / 2;

        rectF.set(centerX - radius, centerY - radius,
                centerX + radius, centerY + radius);

        float completedAngle = (completedPercentage / 100f) * 360f;

        // Draw completed arc
        canvas.drawArc(rectF, -90, completedAngle, true, completedPaint);

        // Draw incomplete arc
        canvas.drawArc(rectF, -90 + completedAngle, 360f - completedAngle,
                true, incompletePaint);

        // Draw % text
        String percentText = String.format("%.0f%%", completedPercentage);

        // căn text chính giữa
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float textHeight = fm.descent - fm.ascent;
        float textOffset = (textHeight / 2) - fm.descent;

        canvas.drawText(percentText, centerX, centerY + textOffset, textPaint);
    }
}
