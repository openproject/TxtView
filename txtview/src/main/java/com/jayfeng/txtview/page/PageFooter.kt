package com.jayfeng.txtview.page

import android.graphics.Canvas
import android.graphics.Paint

class PageFooter(val width: Int,
                 val height: Int,
                 val paint: Paint,
                 var pageInfo: String,
                 val paddingLeft: Float,
                 val paddingRight: Float) {

    fun draw(canvas: Canvas, drawHeight: Float) {

//        paint.color = Color.RED
//        canvas.drawRect(0f, drawHeight, width.toFloat(), drawHeight + height.toFloat(), paint)
        val pageInfoWidth = paint.measureText(pageInfo)
//        val offset = paint.fontMetrics.ascent
//        paint.color = Color.BLUE
        val textHeight = paint.fontMetrics.descent - paint.fontMetrics.ascent
        val drawY = drawHeight - paint.ascent() + (height - textHeight) / 2
        canvas.drawText(pageInfo, width - pageInfoWidth - paddingRight, drawY, paint)

    }
}