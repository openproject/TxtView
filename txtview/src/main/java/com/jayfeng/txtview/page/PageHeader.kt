package com.jayfeng.txtview.page

import android.graphics.Canvas
import android.graphics.Paint

class PageHeader(val width: Int,
                 val height: Int,
                 val paint: Paint,
                 val text: String,
                 val paddingLeft: Float,
                 val paddingRight: Float) {

    private val textHeight = paint.fontMetrics.descent - paint.fontMetrics.ascent
    private val textOffset = (paint.ascent() - (height - textHeight) / 2)

    fun draw(canvas: Canvas, drawHeight: Float) {
        val drawY = drawHeight - textOffset
        canvas.drawText(text, paddingLeft, drawY, paint)

    }
}