package com.example.atlantiksvisionfinal

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class GridOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Шаг сетки (размер плитки), теперь его можно менять!
    var gridStep: Float = 150f
    private var currentRotation: Float = 0f

    private val paint = Paint().apply {
        color = Color.parseColor("#00FF41")
        strokeWidth = 2f
        style = Paint.Style.STROKE
        isAntiAlias = true
        alpha = 150
    }

    fun updateRotation(angle: Float) {
        currentRotation = angle
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        canvas.rotate(-currentRotation, width / 2f, height / 2f)

        // Используем наш gridStep вместо статических 150f
        val step = gridStep
        val offset = 1000f // Увеличил запас для сильных наклонов

        var x = -offset
        while (x < width + offset) {
            canvas.drawLine(x, -offset, x, height.toFloat() + offset, paint)
            x += step
        }

        var y = -offset
        while (y < height + offset) {
            canvas.drawLine(-offset, y, width.toFloat() + offset, y, paint)
            y += step
        }

        canvas.restore()

        val centerPaint = Paint(paint).apply {
            strokeWidth = 5f
            alpha = 255
        }
        canvas.drawCircle(width / 2f, height / 2f, 20f, centerPaint)
    }
}