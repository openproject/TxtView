package com.example.jay.txtpageviewer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log

enum class LineType {
    CONTENT, TITLE, AD
}

class PagePadding(val left: Float,
                  val top: Float,
                  val right: Float,
                  val bottom: Float) {
}

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

class Page(val width: Int,
           val height: Int,
           val contentPaint: Paint,
           val titlePaint: Paint,
           val adPaint: Paint,
           val lineSpace: Float,
           val header: PageHeader,
           val footer: PageFooter,
           val padding: PagePadding) {

    private var lines = mutableListOf<Line>()

    private var contentFontMetrics: Paint.FontMetrics = contentPaint.fontMetrics
    private var titleFontMetrics: Paint.FontMetrics = titlePaint.fontMetrics

    var drawHeight = header.height.toFloat() + (contentFontMetrics.bottom - contentFontMetrics.top)

    fun addLineText(text: String, type: LineType) {
        val line = Line()
        line.text = text
        line.x = padding.left
        line.y = drawHeight
        line.type = type
        lines.add(line)

        when (line.type) {
            LineType.CONTENT -> {
                drawHeight += contentFontMetrics.bottom - contentFontMetrics.top
                drawHeight += lineSpace
            }
            LineType.TITLE -> {
                val titleTextHeight = titleFontMetrics.bottom - titleFontMetrics.top
                line.y += titleTextHeight
                drawHeight += titleTextHeight * 3

                val titleWidth = titlePaint.measureText(text)
                line.x = (width - padding.left - padding.right - titleWidth) / 2

            }
            LineType.AD -> {

            }
        }

        Log.e("feng", "--- drawHeight: " + drawHeight)
    }

    fun draw(canvas: Canvas) {

        header.draw(canvas, 0f)

        lines.forEach { line ->
          when (line.type) {
              LineType.CONTENT -> {
                  canvas.drawText(line.text, line.x, line.y, contentPaint)

              }
              LineType.TITLE -> {
                  canvas.drawText(line.text, line.x, line.y, titlePaint)
              }
              LineType.AD -> {
                  // draw ad view
              }
          }
        }

        footer.draw(canvas, (height - footer.height).toFloat())
    }

    fun updateFooter(pageIndex: Int, pageTotal: Int) {
        footer.pageInfo = "$pageIndex / $pageTotal"
    }

    fun isFull() : Boolean {

        return drawHeight - (contentFontMetrics.bottom - contentFontMetrics.top)> height - header.height - footer.height
    }

}

class Line {

    var text: String? = null
    var x: Float = 0f
    var y: Float = 0f
    var height: Float = 0f
    var type = LineType.CONTENT // TITLE 标题,  AD 广告

}