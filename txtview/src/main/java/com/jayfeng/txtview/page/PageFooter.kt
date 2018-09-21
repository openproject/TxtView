package com.jayfeng.txtview.page

import android.graphics.Canvas
import android.graphics.Paint
import kotlin.properties.Delegates

class PageFooter(val width: Int,
                 val height: Int,
                 val paint: Paint,
                 val paddingLeft: Float,
                 val paddingRight: Float) {

    var pageInfo: String by Delegates.observable("") { _, _, _ ->
        pageInfoWidth = paint.measureText(pageInfo)
    }
    private var pageInfoWidth: Float = 0f
    private val textHeight = paint.fontMetrics.descent - paint.fontMetrics.ascent
    private val textOffset = paint.ascent() - (height - textHeight) / 2

    fun draw(canvas: Canvas, drawHeight: Float) {
        val drawY = drawHeight - textOffset
        canvas.drawText(pageInfo, width - pageInfoWidth - paddingRight, drawY, paint)
        canvas.drawText(Page.pageTime, paddingLeft, drawY, paint)
    }
}