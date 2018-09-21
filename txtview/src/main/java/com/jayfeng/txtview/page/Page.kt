package com.jayfeng.txtview.page

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import java.text.SimpleDateFormat
import java.util.*

class Page(val width: Int,
           val height: Int,
           val contentPaint: Paint,
           val titlePaint: Paint,
           val adPaint: Paint,
           val lineSpace: Float,
           val header: PageHeader,
           val footer: PageFooter,
           val padding: PagePadding) {

    companion object {
        var pageTime = "00:00"

        fun updateTime() {
            val time = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("HH:mm")
            pageTime = dateFormat.format(Date(time))
        }

    }

    private var lines = mutableListOf<Line>()

    private var contentFontMetrics: Paint.FontMetrics = contentPaint.fontMetrics
    private var titleFontMetrics: Paint.FontMetrics = titlePaint.fontMetrics

    private var drawHeight = header.height.toFloat() + (contentFontMetrics.bottom - contentFontMetrics.top)

    private var adSrcRect: Rect? = null
    private var adDestRect: Rect? = null

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
                line.x = (width + padding.left - padding.right - titleWidth) / 2

            }
            LineType.AD -> {
                // NOT SUPPORT
            }
        }
    }

    fun addLineAd(bitmap: Bitmap) {
        val line = Line()
        line.ad = bitmap
        line.x = 0f
        line.y = drawHeight
        line.type = LineType.AD
        lines.add(line)

        drawHeight += bitmap.height
        drawHeight += lineSpace * 2 + (contentPaint.descent() - contentPaint.ascent())

        adSrcRect = Rect(0, 0, line.ad!!.width, line.ad!!.height)
        adDestRect = Rect(0, line.y.toInt(), width, line.y.toInt() + line.ad!!.height)
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
                    canvas.drawBitmap(line.ad, adSrcRect, adDestRect, adPaint)
                }
            }
        }

        footer.draw(canvas, (height - footer.height).toFloat())
    }

    fun updateFooter(pageIndex: Int, pageTotal: Int) {
        footer.pageInfo = "$pageIndex / $pageTotal"
    }

    fun isFull(): Boolean {

        return drawHeight - (contentFontMetrics.descent - contentFontMetrics.ascent) > height - header.height - footer.height
    }

}