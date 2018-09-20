package com.example.jay.txtpageviewer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log

enum class LineType {
    CONTENT, TITLE, AD
}

class PageHeader(val width: Int,
                 val height: Int,
                 val paint: Paint,
                 val text: String) {

    fun draw(canvas: Canvas, drawHeight: Float) {

//        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        val offset = paint.fontMetrics.bottom - paint.fontMetrics.top
        canvas.drawText(text, 0f, drawHeight + offset, paint)

    }
}

class PageFooter(val width: Int,
                 val height: Int,
                 val paint: Paint,
                 var pageInfo: String) {

    fun draw(canvas: Canvas, drawHeight: Float) {


//        canvas.drawRect(0f, drawHeight, width.toFloat(), drawHeight + height.toFloat(), paint)
        val pageInfoWidth = paint.measureText(pageInfo)
        val offset = paint.fontMetrics.bottom - paint.fontMetrics.top
        canvas.drawText(pageInfo, width - pageInfoWidth, drawHeight + offset, paint)

    }
}

class Page(val width: Int,
           val height: Int,
           val contentPaint: Paint,
           val titlePaint: Paint,
           val adPaint: Paint,
           val lineSpace: Float,
           val header: PageHeader,
           val footer: PageFooter) {

    private var lines = mutableListOf<Line>()

    private var contentFontMetrics: Paint.FontMetrics = contentPaint.fontMetrics
    private var titleFontMetrics: Paint.FontMetrics = titlePaint.fontMetrics

    var drawHeight = header.height.toFloat() + (contentFontMetrics.bottom - contentFontMetrics.top)

    fun addLineText(text: String, type: LineType) {
        val line = Line()
        line.text = text
        line.x = 0f
        line.y = drawHeight
        line.type = type
        lines.add(line)

        when (line.type) {
            LineType.CONTENT -> {
                drawHeight += contentFontMetrics.bottom - contentFontMetrics.top
                drawHeight += lineSpace
            }
            LineType.TITLE -> {
                drawHeight += titleFontMetrics.bottom - titleFontMetrics.top
                drawHeight += lineSpace
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