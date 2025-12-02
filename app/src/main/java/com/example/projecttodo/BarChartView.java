package com.example.projecttodo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

public class BarChartView extends View {

    private Paint paintTotal;
    private Paint paintCompleted;
    private Paint paintOverdue;

    private Paint paintAxis;
    private Paint paintGrid;
    private Paint paintLabel;
    private Paint paintValue;
    private Paint paintYAxisText;
    private Paint paintYAxisTitle;

    private int total = 0;
    private int completed = 0;
    private int overdue = 0;
    private int maxValue = 0;      // max thực tế
    private int displayMax = 0;    // max để vẽ (cao hơn một chút)

    private int colorPrimary;
    private int colorGray;
    private int colorError;
    private int colorOnSurface;

    public BarChartView(Context context) {
        super(context);
        init();
    }

    public BarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BarChartView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {

        TypedValue typedValue = new TypedValue();

        // Màu chủ đạo: ?attr/colorPrimary (giống PieChartView)
        getContext().getTheme().resolveAttribute(
                androidx.appcompat.R.attr.colorPrimary,
                typedValue,
                true
        );
        colorPrimary = typedValue.data;

        // Màu chữ: ?attr/colorOnSurface (Material)
        getContext().getTheme().resolveAttribute(
                com.google.android.material.R.attr.colorOnSurface,
                typedValue,
                true
        );
        colorOnSurface = typedValue.data;

        // Xám cho "Tất cả"
        colorGray = 0xFFBDBDBD;
        // Đỏ cho "Quá hạn"
        colorError = 0xFFFF5555;

        // ===== PAINT CỘT =====
        paintTotal = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintTotal.setColor(colorGray);
        paintTotal.setStyle(Paint.Style.FILL);

        paintCompleted = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintCompleted.setColor(colorPrimary);
        paintCompleted.setStyle(Paint.Style.FILL);

        paintOverdue = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintOverdue.setColor(colorError);
        paintOverdue.setStyle(Paint.Style.FILL);

        // ===== TRỤC & LƯỚI =====
        paintAxis = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintAxis.setColor(colorOnSurface);
        paintAxis.setStrokeWidth(dp(1));
        paintAxis.setAlpha(180);

        paintGrid = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintGrid.setColor(colorOnSurface);
        paintGrid.setStrokeWidth(dp(1));
        paintGrid.setAlpha(60);

        // ===== TEXT =====
        paintLabel = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintLabel.setColor(colorOnSurface);
        paintLabel.setTextSize(sp(14));
        paintLabel.setTextAlign(Paint.Align.CENTER);

        paintValue = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintValue.setColor(colorOnSurface);
        paintValue.setTextSize(sp(16));
        paintValue.setFakeBoldText(true);
        paintValue.setTextAlign(Paint.Align.CENTER);

        paintYAxisText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintYAxisText.setColor(colorOnSurface);
        paintYAxisText.setTextSize(sp(12));
        paintYAxisText.setTextAlign(Paint.Align.RIGHT);

        paintYAxisTitle = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintYAxisTitle.setColor(colorOnSurface);
        paintYAxisTitle.setTextSize(sp(11));
        paintYAxisTitle.setTextAlign(Paint.Align.LEFT);
    }

    // 🔹 total, completed, overdue
    public void setData(int total, int completed, int overdue) {
        this.total = Math.max(0, total);
        this.completed = Math.max(0, completed);
        this.overdue = Math.max(0, overdue);

        maxValue = Math.max(this.total,
                Math.max(this.completed, this.overdue));

        if (maxValue <= 0) {
            displayMax = 0;
        } else {
            int raw = (int) Math.ceil(maxValue * 1.2f); // dư ra 20%
            int step = 5;
            displayMax = ((raw + step - 1) / step) * step;
        }

        invalidate();
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }

    private float sp(float v) {
        return v * getResources().getDisplayMetrics().scaledDensity;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (displayMax <= 0) return;

        float width = getWidth();
        float height = getHeight();

        float paddingLeft = dp(40);   // chừa chỗ cho số & chữ trục tung
        float paddingRight = dp(20);
        float paddingTop = dp(24);
        float paddingBottom = dp(40); // chừa chỗ cho label X

        float axisX = paddingLeft;              // vị trí trục tung
        float chartBottom = height - paddingBottom;
        float chartTop = paddingTop;
        float chartHeight = chartBottom - chartTop;

        // ===== VẼ LƯỚI + SỐ TRỤC TUNG =====
        int steps = 4;
        float stepValue = displayMax / (float) steps;
        float stepHeight = chartHeight / steps;

        for (int i = 0; i <= steps; i++) {
            float y = chartBottom - (i * stepHeight);

            canvas.drawLine(axisX, y, width - paddingRight, y, paintGrid);

            String yText = String.valueOf((int) (i * stepValue));
            canvas.drawText(yText, axisX - dp(6), y + dp(4), paintYAxisText);
        }

        // chữ nhỏ "SL công việc"
        canvas.drawText("SL công việc", axisX, chartTop - dp(6), paintYAxisTitle);

        // ===== VẼ CỘT =====
        float totalChartWidth = width - paddingRight - axisX;

        float gapFromAxis = dp(10);     // cách trục tung
        float space = dp(26);           // khoảng cách giữa các cột

        float barWidth = (totalChartWidth - gapFromAxis - space * 2) / 3f;
        float firstLeft = axisX + gapFromAxis;

        drawBar(canvas,
                firstLeft,
                chartBottom,
                barWidth,
                total,
                "Tất cả",
                paintTotal,
                chartHeight);

        drawBar(canvas,
                firstLeft + barWidth + space,
                chartBottom,
                barWidth,
                completed,
                "Hoàn thành",
                paintCompleted,
                chartHeight);

        drawBar(canvas,
                firstLeft + (barWidth + space) * 2,
                chartBottom,
                barWidth,
                overdue,
                "Quá hạn",
                paintOverdue,
                chartHeight);

        // ===== TRỤC HOÀNH + TRỤC TUNG =====
        canvas.drawLine(axisX, chartBottom, width - paddingRight, chartBottom, paintAxis);
        canvas.drawLine(axisX, chartBottom, axisX, chartTop, paintAxis);
    }

    private void drawBar(Canvas canvas,
                         float left,
                         float bottom,
                         float width,
                         int value,
                         String label,
                         Paint barPaint,
                         float chartHeight) {

        if (displayMax <= 0) return;

        float barHeight = (value / (float) displayMax) * chartHeight;
        float top = bottom - barHeight;

        RectF rect = new RectF(left, top, left + width, bottom);
        canvas.drawRoundRect(rect, dp(10), dp(10), barPaint);

        float cx = left + width / 2f;

        if (value > 0) {
            canvas.drawText(String.valueOf(value), cx, top - dp(6), paintValue);
        }

        canvas.drawText(label, cx, bottom + dp(22), paintLabel);
    }
}
