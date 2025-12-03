package com.example.projecttodo.ThongKe;

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
    private Paint overduePaint;
    private Paint backgroundPaint;
    private Paint textPaint;

    private RectF rectF;

    private float completedPercent = 0f;
    private float incompletePercent = 0f;
    private float overduePercent = 0f;

    private boolean hasData = false;

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

        TypedValue typedValue = new TypedValue();

        // màu primary
        getContext().getTheme().resolveAttribute(
                androidx.appcompat.R.attr.colorPrimary,
                typedValue,
                true
        );
        int primaryColor = typedValue.data;

        // màu chữ chính
        getContext().getTheme().resolveAttribute(
                com.google.android.material.R.attr.colorOnSurface,
                typedValue,
                true
        );
        int textColor = typedValue.data;

        int colorIncomplete = 0xFFFFC107;  // vàng/cam
        int colorOverdue = 0xFFFF5555;     // đỏ
        int colorBg = 0xFFE0E0E0;          // xám nhạt

        // Completed
        completedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        completedPaint.setColor(primaryColor);
        completedPaint.setStyle(Paint.Style.FILL);

        // Incomplete
        incompletePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        incompletePaint.setColor(colorIncomplete);
        incompletePaint.setStyle(Paint.Style.FILL);

        // Overdue
        overduePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        overduePaint.setColor(colorOverdue);
        overduePaint.setStyle(Paint.Style.FILL);

        // Background nếu total = 0
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(colorBg);
        backgroundPaint.setStyle(Paint.Style.FILL);

        // Text % trên từng miếng
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(sp(12));
        textPaint.setFakeBoldText(true);

        rectF = new RectF();
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }

    private float sp(float v) {
        return v * getResources().getDisplayMetrics().scaledDensity;
    }

    /**
     * completed + incomplete + overdue = total
     *  -> vẽ pie 3 phần, hiển thị % trên từng phần
     */
    public void setData(int completed, int incomplete, int overdue) {

        // Clamp về >= 0 để tránh % âm
        int c = Math.max(0, completed);
        int i = Math.max(0, incomplete);
        int o = Math.max(0, overdue);

        int total = c + i + o;

        if (total > 0) {
            completedPercent  = c * 100f / total;
            incompletePercent = i * 100f / total;
            overduePercent    = o * 100f / total;
            hasData = true;
        } else {
            completedPercent = 0f;
            incompletePercent = 0f;
            overduePercent = 0f;
            hasData = false;
        }

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        int radius = Math.min(width, height) / 2 - (int) dp(8);

        int centerX = width / 2;
        int centerY = height / 2;

        rectF.set(centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius);

        // Không có dữ liệu: vẽ vòng tròn xám + "0%"
        if (!hasData) {
            canvas.drawArc(rectF, 0, 360f, true, backgroundPaint);
            drawCenterText(canvas, centerX, centerY, "0%");
            return;
        }

        float startAngle = -90f;

        float completedAngle  = (completedPercent  / 100f) * 360f;
        float incompleteAngle = (incompletePercent / 100f) * 360f;
        float overdueAngle    = (overduePercent    / 100f) * 360f;

        // Completed
        if (completedAngle > 0) {
            canvas.drawArc(rectF, startAngle, completedAngle, true, completedPaint);
            drawSlicePercent(canvas, centerX, centerY, radius,
                    startAngle, completedAngle, completedPercent);
            startAngle += completedAngle;
        }

        // Incomplete
        if (incompleteAngle > 0) {
            canvas.drawArc(rectF, startAngle, incompleteAngle, true, incompletePaint);
            drawSlicePercent(canvas, centerX, centerY, radius,
                    startAngle, incompleteAngle, incompletePercent);
            startAngle += incompleteAngle;
        }

        // Overdue
        if (overdueAngle > 0) {
            canvas.drawArc(rectF, startAngle, overdueAngle, true, overduePaint);
            drawSlicePercent(canvas, centerX, centerY, radius,
                    startAngle, overdueAngle, overduePercent);
        }
    }

    private void drawSlicePercent(Canvas canvas,
                                  int cx,
                                  int cy,
                                  int radius,
                                  float startAngle,
                                  float sweepAngle,
                                  float percent) {

        if (percent <= 0f) return;

        // Góc ở giữa miếng
        float middleAngle = startAngle + sweepAngle / 2f;
        double rad = Math.toRadians(middleAngle);

        float labelRadius = radius * 0.6f;
        float x = cx + (float) (Math.cos(rad) * labelRadius);
        float y = cy + (float) (Math.sin(rad) * labelRadius);

        String text = String.format("%.0f%%", percent);

        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float textHeight = fm.descent - fm.ascent;
        float textOffset = (textHeight / 2) - fm.descent;

        canvas.drawText(text, x, y + textOffset, textPaint);
    }

    private void drawCenterText(Canvas canvas, int cx, int cy, String text) {
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float textHeight = fm.descent - fm.ascent;
        float textOffset = (textHeight / 2) - fm.descent;
        canvas.drawText(text, cx, cy + textOffset, textPaint);
    }
}
