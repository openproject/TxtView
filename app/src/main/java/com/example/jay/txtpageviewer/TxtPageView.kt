package com.example.jay.txtpageviewer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import java.util.*

class TxtPageView : View {

    var mPaint = Paint()
    var mFontMetrics: Paint.FontMetrics
    var lineHeight: Float = 0.0f


    var mContent : String = ""
    var mPage : Int = 0
    var mPageSize = 8
    var mLines = ArrayList<String>()

    init {
        mPaint.color = Color.WHITE
        mPaint.textSize = 18.0f
        mFontMetrics = mPaint.fontMetrics
        lineHeight = mFontMetrics.bottom - mFontMetrics.top
    }

    constructor(context: Context?) : super(context) {

    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        println("--------------------- mpage: $mPage ,view height: " + height + ", line height: $lineHeight")

        mPageSize = ((height - lineHeight - 4 - 24.0f) / lineHeight).toInt()

        var startLine = mPage * mPageSize
        var endLine = startLine + mPageSize
        if (endLine > mLines.size - 1) {
            endLine = mLines.size - 1
            mPage = -1
        }
        var x = 8.0f
        var y = 24.0f
        for (lineIndex in startLine .. (endLine - 1)) {
            y += lineHeight
            canvas.drawText(mLines[lineIndex], x,  y, mPaint)
            canvas.drawLine(44.0f, y + mFontMetrics.descent, 300.0f, y + mFontMetrics.descent, mPaint)
        }

        val pageInfo = "$mPage / ${mLines.size / mPageSize}"
        val pageInfoWidth = mPaint.measureText(pageInfo)

        canvas.drawText(pageInfo, width - pageInfoWidth - 8.0f, y + lineHeight, mPaint)
    }

//    private fun drawPageInfo(canvas: Canvas) {}

    fun setContent(content: String) {
        this.mContent = content

        val startTime = System.currentTimeMillis()
        val widthPaintLength = mPaint.breakText("测试字符串测试字符串测试字符串测试字符串测试字符串测试字符串字符串测试字符串测试字符符串测试字符串", false, measuredWidth.toFloat() - 14, null)
        mContent.split("\n").forEach { paragraph ->
            var startIndex = 0
            while(startIndex < paragraph.length) {
                var endIndex = startIndex + widthPaintLength
                if (endIndex > paragraph.length) {
                    endIndex = paragraph.length
                }

//                Log.e("feng","------ 1startIndex: $startIndex, endIndex: $endIndex")

                var line = paragraph.substring(startIndex, endIndex)
                while (mPaint.measureText(line + "好") < (width - 12.0f) && endIndex < paragraph.length) {
                    endIndex += 1
                    line = paragraph.substring(startIndex, endIndex)
                }
                mLines.add(line)
                startIndex = endIndex
            }

//            Log.e("feng", "------------------paragraph: " + paragraph)
        }

        println("--------dd-d-d-d-- cost: " + (System.currentTimeMillis() - startTime))
    }

    fun prevPage() {
        mPage--
        if (mPage < 0) {
            mPage = 0
        }
        invalidate()
    }
    fun nextPage() {
        mPage++
        invalidate()
    }
}