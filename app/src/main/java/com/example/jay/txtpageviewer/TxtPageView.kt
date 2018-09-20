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
import com.jayfeng.txtview.page.*


class TxtPageView : View {

    var mHeaderPaint = Paint().apply {
        isAntiAlias = true
        color = Color.GRAY
        textSize = DisplayLess.`$dp2px`(16.0f).toFloat()
    }
    val mHeaderHeight: Int by lazy { DisplayLess.`$dp2px`(44f) }
    var mFooterPaint = Paint().apply {
        isAntiAlias = true
        color = Color.GRAY
        textSize = DisplayLess.`$dp2px`(16.0f).toFloat()
    }
    val mFooterHeight: Int by lazy { DisplayLess.`$dp2px`(32f) }

    val mPaddingLeft: Float by lazy { DisplayLess.`$dp2px`(24f).toFloat() }
    val mPaddingTop: Float by lazy { DisplayLess.`$dp2px`(8f).toFloat() }
    val mPaddingRight: Float by lazy { DisplayLess.`$dp2px`(24f).toFloat() }
    val mPaddingBottom: Float by lazy { DisplayLess.`$dp2px`(8f).toFloat() }

    var mContentPaint = Paint().apply {
        isAntiAlias = true
        color = Color.parseColor("#424242")
        textSize = DisplayLess.`$dp2px`(18.0f).toFloat()
    }
    var mTitlePaint = Paint().apply {
        isAntiAlias = true
        color = Color.parseColor("#424242")
        textSize = DisplayLess.`$dp2px`(28.0f).toFloat()
    }

    var mContent: String = ""
    var mPage: Int = 1
    var mLines = ArrayList<String>()
    val mLineSpace: Int by lazy { DisplayLess.`$dp2px`(10f)}
    var isPaging = false

    val mBg: Bitmap by lazy { (resources.getDrawable(R.drawable.theme_leather_bg) as BitmapDrawable).bitmap }
    val mBgSrcRect: Rect by lazy { Rect(0, 0, mBg.width, mBg.height) }
    val mBgDestRect: Rect by lazy { Rect(0, 0, width, height) }

    var mTouchX = 0f
    var moveX = 0f

    var mPages = ArrayList<Page>()
    val mShadowPaint = Paint()
    val mShadowGradient: LinearGradient by lazy { LinearGradient(
            measuredWidth.toFloat(), 0f, measuredWidth.toFloat() + 20.0f, 0f, intArrayOf(Color.parseColor("#AA666666"), Color.TRANSPARENT), null, Shader.TileMode.CLAMP) }

    init {

    }

    constructor(context: Context?) : super(context)
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


            canvas.save()
            canvas.clipRect(width + moveX, 0f, width.toFloat(), height.toFloat())
            drawPage(canvas, mPage + 1)
            canvas.restore()

            canvas.save()

            canvas.translate(moveX, 0f)
            canvas.drawBitmap(mBg, mBgSrcRect, mBgDestRect, mContentPaint)

            drawPage(canvas, mPage)
            mShadowPaint.shader = mShadowGradient
            canvas.drawRect(measuredWidth.toFloat(), 0.0f, measuredWidth.toFloat() + 20.0f, measuredHeight.toFloat(), mShadowPaint)
            canvas.restore()
        } else if (moveX > 0) {

            canvas.save()
            canvas.clipRect(moveX, 0f, width.toFloat(), height.toFloat())
            drawPage(canvas, mPage)
            canvas.restore()

            canvas.save()
            canvas.translate(moveX - width, 0f)
            canvas.drawBitmap(mBg, mBgSrcRect, mBgDestRect, mContentPaint)

            if (mPage > 1) {
                drawPage(canvas, mPage - 1)
                mShadowPaint.shader = mShadowGradient
                canvas.drawRect(measuredWidth.toFloat(), 0.0f, measuredWidth.toFloat() + 20.0f, measuredHeight.toFloat(), mShadowPaint)
            }
            canvas.restore()
        }
    }

    fun drawPage(canvas: Canvas, page: Int) {


        Log.d("feng", "------- page: $page")

        if (page > mPages.size) {
            return
        }

        val pageData = mPages[page - 1]
        pageData.draw(canvas)

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
        val contentWidth = measuredWidth.toFloat() - mPaddingLeft - mPaddingRight
        val widthPaintLength = mContentPaint.breakText("测试字符串测试字符串测试字符串测试字符串测试字符串测试字符串字符串测试字符串测试字符符串测试字符串",
                false, contentWidth, null)
        mContent.split("\n").forEach { paragraph ->
            var startIndex = 0
            while (startIndex < paragraph.length) {
                var endIndex = startIndex + widthPaintLength
                if (endIndex > paragraph.length) {
                    endIndex = paragraph.length
                }

                var lineText = paragraph.substring(startIndex, endIndex)
                while (mContentPaint.measureText(lineText + "好") < contentWidth && endIndex < paragraph.length) {
                    endIndex += 1
                    lineText = paragraph.substring(startIndex, endIndex)
                }

                mLines.add(lineText)
                startIndex = endIndex
            }
        }

        val header = PageHeader(width, mHeaderHeight, mHeaderPaint, "王二狗的那些神话", mPaddingLeft, mPaddingRight)
        var footer = PageFooter(width, mFooterHeight, mFooterPaint, "", mPaddingLeft, mPaddingRight)
        val padding = PagePadding(mPaddingLeft, mPaddingTop, mPaddingRight, mPaddingBottom)
        var page = Page(width, height, mContentPaint, mTitlePaint, Paint(), mLineSpace.toFloat(), header, footer, padding)
        mPages.add(page)

        // add title
        page.addLineText("第一章 重新开始", LineType.TITLE)

        // add content
        mLines.forEach { lineText ->

            val isFull = page.isFull()
            if (isFull) {
                footer = PageFooter(width, mFooterHeight, mFooterPaint, "", mPaddingLeft, mPaddingRight)
                page = Page(width, height, mContentPaint, mTitlePaint, Paint(), mLineSpace.toFloat(), header, footer, padding)
                mPages.add(page)
            }

            page.addLineText(lineText, LineType.CONTENT)

        }

        mPages.forEachIndexed{ index, page ->
            page.updateFooter(index + 1, mPages.size)
        }

        Log.d("feng", "ddd")
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