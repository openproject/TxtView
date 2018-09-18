package com.example.jay.txtpageviewer

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.jayfeng.lesscode.core.DisplayLess
import java.util.*


class TxtPageView : View {

    var mPaint = Paint()
    var mFontMetrics: Paint.FontMetrics
    var lineHeight: Float = 0.0f

    var mContent: String = ""
    var mPage: Int = 1
    var mPageSize = 8
    var mLines = ArrayList<String>()

    var mBg: Bitmap

    init {
        mPaint.color = Color.parseColor("#424242")
        mPaint.isAntiAlias = true
        mPaint.textSize = DisplayLess.`$dp2px`(18.0f).toFloat()
        mFontMetrics = mPaint.fontMetrics
        lineHeight = mFontMetrics.bottom - mFontMetrics.top

        mBg = (resources.getDrawable(R.drawable.theme_leather_bg) as BitmapDrawable).bitmap
    }

    constructor(context: Context?) : super(context) {

    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        mPageSize = ((measuredHeight - lineHeight - 4 - 24.0f) / lineHeight).toInt()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

//        println("--------------------- mpage: $mPage ,view height: " + height + ", line height: $lineHeight")

        if (moveX < 0) {

            drawPage(canvas, mPage + 1)

            canvas.save()
            canvas.translate(moveX, 0f)
            canvas.drawBitmap(mBg, Rect(0, 0, width, height), Rect(0, 0, width, height), mPaint)

            drawPage(canvas, mPage)
            val paint = Paint()
            paint.color = Color.GREEN
            val linearGradient = LinearGradient(
                    measuredWidth.toFloat(), 0f, measuredWidth.toFloat() + 20.0f, 0f, intArrayOf(Color.parseColor("#666666"), Color.TRANSPARENT), null, Shader.TileMode.CLAMP)
            paint.shader = linearGradient
            canvas.drawRect(measuredWidth.toFloat(), 0.0f, measuredWidth.toFloat() + 20.0f, measuredHeight.toFloat(), paint)
            canvas.restore()
        } else {

            drawPage(canvas, mPage)

            canvas.save()
            canvas.translate(moveX - width, 0f)
            canvas.drawBitmap(mBg, Rect(0, 0, width, height), Rect(0, 0, width, height), mPaint)

            if (mPage > 1) {
                drawPage(canvas, mPage - 1)
                val paint = Paint()
                paint.color = Color.GREEN
                val linearGradient = LinearGradient(
                        measuredWidth.toFloat(), 0f, measuredWidth.toFloat() + 20.0f, 0f, intArrayOf(Color.parseColor("#666666"), Color.TRANSPARENT), null, Shader.TileMode.CLAMP)
                paint.shader = linearGradient
                canvas.drawRect(measuredWidth.toFloat(), 0.0f, measuredWidth.toFloat() + 20.0f, measuredHeight.toFloat(), paint)
                canvas.restore()
            }
        }
    }

    fun drawPage(canvas: Canvas, page: Int) {

        var startLine = (page - 1) * mPageSize
        var endLine = startLine + mPageSize - 1
        if (endLine > mLines.size - 1) {
            endLine = mLines.size - 1
        }

//        Log.d("feng", "----------------- startLine: $startLine, endLine: $endLine, maxLine: ${mLines.size}")

        var x = 8.0f
        var y = 24.0f
        for (lineIndex in startLine..endLine) {
            y += lineHeight
            canvas.drawText(mLines[lineIndex], x, y, mPaint)
            canvas.drawLine(44.0f, y + mFontMetrics.descent, 300.0f, y + mFontMetrics.descent, mPaint)
        }

        val pageInfo = getPageInfoString(page)
        val pageInfoWidth = mPaint.measureText(pageInfo)

        canvas.drawText(pageInfo, width - pageInfoWidth - 8.0f, y + lineHeight, mPaint)
    }

    fun setContent(content: String) {
        this.mContent = content

        val startTime = System.currentTimeMillis()
        val widthPaintLength = mPaint.breakText("测试字符串测试字符串测试字符串测试字符串测试字符串测试字符串字符串测试字符串测试字符符串测试字符串", false, measuredWidth.toFloat() - 14, null)
        mContent.split("\n").forEach { paragraph ->
            var startIndex = 0
            while (startIndex < paragraph.length) {
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

    private fun getPageInfoString(page: Int): String {
        return "$page / ${(mLines.size + mPageSize - 1) / mPageSize}"
    }

    fun prevPage() {
        mPage--
        if (mPage <= 0) {
            mPage = 1
        }
        invalidate()
    }

    fun nextPage() {
        if (mPageSize * mPage >= mLines.size) {
            return
        }
        mPage++
        invalidate()
    }

    var touchX = 0f
    var moveX = 0f

    override fun onTouchEvent(event: MotionEvent): Boolean {

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchX = event.rawX
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                moveX = event.rawX - touchX

                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                touchX = event.rawX
            }
        }

        return super.onTouchEvent(event)
    }
}