package com.jayfeng.txtview.page

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.Log
import com.jayfeng.txtview.TxtView
import java.text.SimpleDateFormat
import java.util.*

class Page(val width: Int,
           val height: Int,
           val adPaint: Paint,
           val lineSpace: Float,
           val header: PageHeader,
           val footer: PageFooter,
           val padding: PagePadding) {

    var start = 0
    var length = 0

    private var lines = mutableListOf<Line>()

    private var contentFontMetrics: Paint.FontMetrics = TxtView.contentPaint.fontMetrics
    private var titleFontMetrics: Paint.FontMetrics = TxtView.titlePaint.fontMetrics

    private var drawHeight = header.height.toFloat() + (contentFontMetrics.bottom - contentFontMetrics.top)

    private var hasAd = false
    private var adYStart = 1000f
    private var adYEnd = 2000f

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

                val titleWidth = TxtView.titlePaint.measureText(text)
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

        val scaleHeight = width * bitmap.height / bitmap.width

        drawHeight += scaleHeight
        drawHeight += lineSpace * 2 + (TxtView.contentPaint.descent() - TxtView.contentPaint.ascent())


        hasAd = true
        adYStart = line.y
        adYEnd = drawHeight
        adSrcRect = Rect(0, 0, bitmap.width, bitmap.height)
        adDestRect = Rect(0, line.y.toInt(), width, line.y.toInt() + scaleHeight)
    }

    fun draw(canvas: Canvas) {

        header.draw(canvas, 0f)

        lines.forEach { line ->
            when (line.type) {
                LineType.CONTENT -> {
                    canvas.drawText(line.text, line.x, line.y, TxtView.contentPaint)
                }
                LineType.TITLE -> {
                    canvas.drawText(line.text, line.x, line.y, TxtView.titlePaint)
                }
                LineType.AD -> {
                    if (line.ad != null && !line.ad!!.isRecycled) {
                        canvas.drawBitmap(line.ad, adSrcRect, adDestRect, adPaint)
                    }
                }
            }
        }

        footer.draw(canvas, (height - footer.height).toFloat())
    }

    fun updateFooter(pageIndex: Int, pageTotal: Int) {
        footer.pageInfo = "$pageIndex / $pageTotal"
    }

    fun isInAd(yPos : Float) : Boolean {

        Log.d("feng", "-------- hasAd: $hasAd, yPos: $yPos, adYStart: $adYStart, adYEnd: $adYEnd")

        return hasAd && yPos > adYStart && yPos < adYEnd
    }

    fun isFull(): Boolean {

        return drawHeight - (contentFontMetrics.descent - contentFontMetrics.ascent) > height - header.height - footer.height
    }

}