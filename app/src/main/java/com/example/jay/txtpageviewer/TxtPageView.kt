package com.example.jay.txtpageviewer

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.jayfeng.lesscode.core.DisplayLess


class TxtPageView : View {

    var mHeaderPaint = Paint().apply { isAntiAlias = true; }
    val mHeaderHeight: Int by lazy { DisplayLess.`$dp2px`(44f) }
    var mFooterPaint = Paint().apply {
        isAntiAlias = true
        color = Color.GRAY
        textSize = DisplayLess.`$dp2px`(16.0f).toFloat()
    }
    val mFooterHeight: Int by lazy { DisplayLess.`$dp2px`(44f) }

    var mContentPaint = Paint()
    var mTitlePaint = Paint()

    var mContent: String = ""
    var mPage: Int = 1
    var mLines = ArrayList<String>()
    val mLineSpace: Int by lazy { DisplayLess.`$dp2px`(10f)}
    var isPaging = false

    var mBg: Bitmap

    var mTouchX = 0f
    var moveX = 0f

    var mPages = ArrayList<Page>()

    init {
        mContentPaint.color = Color.parseColor("#424242")
        mContentPaint.isAntiAlias = true
        mContentPaint.textSize = DisplayLess.`$dp2px`(18.0f).toFloat()

        mTitlePaint.color = Color.parseColor("#424242")
        mTitlePaint.isAntiAlias = true
        mTitlePaint.textSize = DisplayLess.`$dp2px`(24.0f).toFloat()

        mBg = (resources.getDrawable(R.drawable.theme_leather_bg) as BitmapDrawable).bitmap
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

//        println("--------------------- mpage: $mPage ,view height: " + height + ", line height: $lineHeight")

        if (moveX == 0f) {

            drawPage(canvas, mPage)

        } else if (moveX < 0) {

            drawPage(canvas, mPage + 1)

            canvas.save()
            canvas.translate(moveX, 0f)
            canvas.drawBitmap(mBg, Rect(0, 0, mBg.width, mBg.height), Rect(0, 0, width, height), mContentPaint)

            drawPage(canvas, mPage)
            val paint = Paint()
            paint.color = Color.GREEN
            val linearGradient = LinearGradient(
                    measuredWidth.toFloat(), 0f, measuredWidth.toFloat() + 20.0f, 0f, intArrayOf(Color.parseColor("#AA666666"), Color.TRANSPARENT), null, Shader.TileMode.CLAMP)
            paint.shader = linearGradient
            canvas.drawRect(measuredWidth.toFloat(), 0.0f, measuredWidth.toFloat() + 20.0f, measuredHeight.toFloat(), paint)
            canvas.restore()
        } else if (moveX > 0) {

            drawPage(canvas, mPage)

            canvas.save()
            canvas.translate(moveX - width, 0f)
            canvas.drawBitmap(mBg, Rect(0, 0, mBg.width, mBg.height), Rect(0, 0, width, height), mContentPaint)

            if (mPage > 1) {
                drawPage(canvas, mPage - 1)
                val paint = Paint()
                paint.color = Color.GREEN
                val linearGradient = LinearGradient(
                        measuredWidth.toFloat(), 0f, measuredWidth.toFloat() + 20.0f, 0f, intArrayOf(Color.parseColor("#AA666666"), Color.TRANSPARENT), null, Shader.TileMode.CLAMP)
                paint.shader = linearGradient
                canvas.drawRect(measuredWidth.toFloat(), 0.0f, measuredWidth.toFloat() + 20.0f, measuredHeight.toFloat(), paint)
                canvas.restore()
            }
        }
    }

    fun drawPage(canvas: Canvas, page: Int) {


        Log.d("feng", "------- page: $page")

        if (page > mPages.size) {
            return
        }


        val pageData = mPages[page - 1]
        pageData.draw(canvas)

//        val pageInfo = getPageInfoString(page)
//        val pageInfoWidth = mContentPaint.measureText(pageInfo)
//
//        canvas.drawText(pageInfo, width - pageInfoWidth - 8.0f, y + lineHeight, mContentPaint)
    }

    fun setContent(content: String) {
        this.mContent = content

        post {
            val startTime = System.currentTimeMillis()

            parseContent()

            invalidate()

            println("--------dd-d-d-d-- cost: " + (System.currentTimeMillis() - startTime))
        }
    }

    private fun parseContent() {
        val widthPaintLength = mContentPaint.breakText("测试字符串测试字符串测试字符串测试字符串测试字符串测试字符串字符串测试字符串测试字符符串测试字符串", false, measuredWidth.toFloat() - 14, null)
        mContent.split("\n").forEach { paragraph ->
            var startIndex = 0
            while (startIndex < paragraph.length) {
                var endIndex = startIndex + widthPaintLength
                if (endIndex > paragraph.length) {
                    endIndex = paragraph.length
                }

                var lineText = paragraph.substring(startIndex, endIndex)
                while (mContentPaint.measureText(lineText + "好") < (width - 12.0f) && endIndex < paragraph.length) {
                    endIndex += 1
                    lineText = paragraph.substring(startIndex, endIndex)
                }

                mLines.add(lineText)
                startIndex = endIndex
            }
        }

        var page: Page? = null
        mLines.forEach { lineText ->

            val isFull = page?.isFull() == true
            Log.d("feng", "------------- page: $page,  is full: ${isFull}")
            if (page == null || isFull) {

                val header = PageHeader(width, mHeaderHeight, mHeaderPaint, "标题")
                val footer = PageFooter(width, mFooterHeight, mFooterPaint, "${mPages.size + 1}")
                page = Page(width, height, mContentPaint, mTitlePaint, Paint(), mLineSpace.toFloat(), header, footer)

                mPages.add(page!!)
            }

            page?.addLineText(lineText, LineType.CONTENT)

        }

        mPages.forEachIndexed{ index, page ->
            page.updateFooter(index + 1, mPages.size)
        }

        Log.d("feng", "ddd")
    }

    private fun getPageInfoString(page: Int): String {
        return "$page / ${mPages.size}"
    }

    fun prevPage() {
        mPage--
        if (mPage <= 0) {
            mPage = 1
        }
        invalidate()
    }

    fun nextPage() {
        if (mPage > mPages.size) {
            return
        }
        mPage++
        invalidate()
    }

    fun firstPage() {
        mPage = 1
        invalidate()
    }

    fun lastPage() {
        mPage = mPages.size
        invalidate()
    }


    fun prevPageWithAnim() {

        if (mPage <= 1 || isPaging) {
            return
        }

        val animator = ValueAnimator.ofFloat(moveX, width.toFloat())
        animator.interpolator = DecelerateInterpolator()
        animator.duration = 240
        animator.addUpdateListener { va ->
            moveX = va.animatedValue as Float
            invalidate()
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                prevPage()
                moveX = 0f
                animator.cancel()
                isPaging = false
            }

        })
        animator.start()
        isPaging = true
    }

    fun nextPageWithAnim() {

        Log.d("feng", "--------- mPage: $mPage , size: ${mPages.size}")

        if (mPage > mPages.size - 1 || isPaging) {
            return
        }

        val animator = ValueAnimator.ofFloat(moveX, -width.toFloat())
        animator.interpolator = DecelerateInterpolator()
        animator.duration = 240
        animator.addUpdateListener { va ->
            moveX = va.animatedValue as Float
            invalidate()
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                nextPage()
                moveX = 0f
                animator.cancel()
                isPaging = false
            }

        })
        animator.start()
        isPaging = true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

                mTouchX = event.rawX
                return true
            }
            MotionEvent.ACTION_MOVE -> {

                moveX = event.rawX - mTouchX
                if (mPage == 1 && moveX > 0) {
                    moveX = 0f
                } else if (mPage == mPages.size && moveX < 0) {
                    moveX = 0f
                } else {
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {

                mTouchX = event.rawX
                if (moveX < 0) {
                    nextPageWithAnim()
                } else if (moveX > 0) {
                    prevPageWithAnim()
                }
            }
        }

        return super.onTouchEvent(event)
    }
}