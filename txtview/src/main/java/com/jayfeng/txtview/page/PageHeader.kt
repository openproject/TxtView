package com.jayfeng.txtview.page

import android.graphics.Canvas
import android.graphics.Paint

class PageHeader(val width: Int,
                 val height: Int,
                 val paint: Paint,
                 val text: String,
                 val paddingLeft: Float,
                 val paddingRight: Float) {

    fun draw(canvas: Canvas, drawHeight: Float) {
        val textHeight = paint.fontMetrics.descent - paint.fontMetrics.ascent
        val drawY = drawHeight - paint.ascent() + (height - textHeight) / 2
        canvas.drawText(text, paddingLeft, drawY, paint)

    }
}