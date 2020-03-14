package com.allat.mboychenko.silverthread.presentation.views.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat

class LinedEditText
    (context: Context, attrs: AttributeSet) : AppCompatEditText(context, attrs) {
    private val mRect: Rect = Rect()
    private val mPaint: Paint = Paint()

    init {
        mPaint.style = Paint.Style.FILL_AND_STROKE
        mPaint.color = ContextCompat.getColor(context, android.R.color.darker_gray)
    }

    override fun onDraw(canvas: Canvas) {

        var count = height / lineHeight

        if (lineCount > count)
            count = lineCount

        val r = mRect
        val paint = mPaint
        var baseline = getLineBounds(0, r)

        for (i in 0 until count) {

            canvas.drawLine(
                r.left.toFloat(),
                (baseline + 1).toFloat(),
                r.right.toFloat(),
                (baseline + 1).toFloat(),
                paint
            )
            baseline += lineHeight
        }

        super.onDraw(canvas)
    }
}