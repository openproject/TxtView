package com.jayfeng.txtview

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import com.jayfeng.lesscode.core.DisplayLess
import com.jayfeng.txtview.page.*
import com.jayfeng.txtview.touch.PageTouchLinstener
import com.jayfeng.txtview.touch.TouchType

class TxtView : View {

    private var mHeaderPaint = Paint()
    private val mHeaderHeight: Int by lazy { DisplayLess.`$dp2px`(48f) }
    private var mFooterPaint = Paint()
    private val mFooterHeight: Int by lazy { DisplayLess.`$dp2px`(48f) }

    private val mPaddingLeft: Float by lazy { DisplayLess.`$dp2px`(28f).toFloat() }
    private val mPaddingTop: Float by lazy { DisplayLess.`$dp2px`(8f).toFloat() }
    private val mPaddingRight: Float by lazy { DisplayLess.`$dp2px`(28f).toFloat() }
    private val mPaddingBottom: Float by lazy { DisplayLess.`$dp2px`(8f).toFloat() }

    private var mContentPaint = Paint()
    private var mTitlePaint = Paint()

    private var mContent: String = ""
    private var mPage: Int = 1
    private var mLines = ArrayList<String>()
    private val mLineSpace: Int by lazy { DisplayLess.`$dp2px`(8f)}
    private var mIsPaging = false

    private var mTouchX = 0f
    private var mTouchY = 0f
    private var moveX = 0f

    var mAdBitmap: Bitmap? = null

    private var mPages = ArrayList<Page>()
    private val mShadowPaint = Paint()
    private val mShadowGradient: LinearGradient by lazy { LinearGradient(
            measuredWidth.toFloat(), 0f, measuredWidth.toFloat() + 16f, 0f, intArrayOf(Color.parseColor("#AA666666"), Color.TRANSPARENT), null, Shader.TileMode.CLAMP)}
    private val mShadowRect: Rect by lazy {
        Rect(measuredWidth, 0, measuredWidth + 16, measuredHeight).apply {
            mShadowPaint.shader = mShadowGradient
        }
    }

    private val mPunctuationSet: Set<Char> = setOf('，', '。', '？', '！', '：', '“', '”',
            ',', '.', '?', '!', ':', '"')

    var mPageTouchLinstener: PageTouchLinstener? = null

    init {
        mHeaderPaint.apply {
            isAntiAlias = true
            color = Color.GRAY
            textSize = DisplayLess.`$dp2px`(14.0f).toFloat()
        }
        mFooterPaint.apply {
            isAntiAlias = true
            color = Color.GRAY
            textSize = DisplayLess.`$dp2px`(14.0f).toFloat()
        }
        mContentPaint.apply {
            isAntiAlias = true
            color = Color.parseColor("#424242")
            textSize = DisplayLess.`$dp2px`(17.0f).toFloat()
        }
        mTitlePaint.apply {
            isAntiAlias = true
            color = Color.parseColor("#424242")
            textSize = DisplayLess.`$dp2px`(24.0f).toFloat()
        }
    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onDraw(canvas: Canvas) {

        when {
            Math.abs(moveX) < ViewConfiguration.getTouchSlop() -> drawPage(canvas, mPage)
            moveX < - ViewConfiguration.getTouchSlop() -> {

                canvas.save()
                canvas.clipRect(width + moveX, 0f, width.toFloat(), height.toFloat())
                drawPage(canvas, mPage + 1)
                canvas.restore()

                canvas.save()

                canvas.translate(moveX, 0f)
                background.draw(canvas)

                drawPage(canvas, mPage)
                canvas.drawRect(mShadowRect, mShadowPaint)
                canvas.restore()
            }
            moveX > ViewConfiguration.getTouchSlop() -> {

                canvas.save()
                canvas.clipRect(moveX, 0f, width.toFloat(), height.toFloat())
                drawPage(canvas, mPage)
                canvas.restore()

                canvas.save()
                canvas.translate(moveX - width, 0f)
                background.draw(canvas)

                if (mPage > 1) {
                    drawPage(canvas, mPage - 1)
                    canvas.drawRect(mShadowRect, mShadowPaint)
                }
                canvas.restore()
            }
        }
    }

    private fun drawPage(canvas: Canvas, page: Int) {

        if (page > mPages.size) {
            return
        }

        val pageData = mPages[page - 1]
        pageData.draw(canvas)

    }

    fun setTxtFile(path: String) {
        // NEXT PLAN
    }

    fun setContent(content: String) {
        this.mContent = content

        post {
            Thread {
                val startTime = System.currentTimeMillis()

                parseContent()

                postInvalidate()

                Log.d("feng", "-------- parse cost: " + (System.currentTimeMillis() - startTime))
            }.start()
        }
    }

    private fun parseContent() {

        val contentWidth = measuredWidth.toFloat() - mPaddingLeft - mPaddingRight
        val widthPaintLength = mContentPaint.breakText("测试字符串测试字符串测试字符串测试字符串测试字符串测试字符串字符串测试字符串测试字符符串测试字符串",
                false, contentWidth, null)
        var preloadPage = true
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

                if (endIndex < paragraph.length && mPunctuationSet.contains(paragraph[endIndex])) {
                    endIndex += 1
                    lineText = paragraph.substring(startIndex, endIndex)
                }

                mLines.add(lineText)
                startIndex = endIndex
            }

            if (preloadPage && mLines.size > 24) {
                parseLineToPage(true)
                postInvalidate()
                preloadPage = false
            }
        }

        mPages.clear()
        parseLineToPage(false)
    }

    private fun parseLineToPage(preload : Boolean) {

        val header = PageHeader(width, mHeaderHeight, mHeaderPaint, "王二狗的那些神话", mPaddingLeft, mPaddingRight)
        var footer = PageFooter(width, mFooterHeight, mFooterPaint, mPaddingLeft, mPaddingRight)
        val padding = PagePadding(mPaddingLeft, mPaddingTop, mPaddingRight, mPaddingBottom)
        var page = Page(width, height, mContentPaint, mTitlePaint, Paint(), mLineSpace.toFloat(), header, footer, padding)
        mPages.add(page)

        // add title
        page.addLineText("第一章 重新开始", LineType.TITLE)

        // add content
        var pageLineIndex = 0
        mLines.forEach { lineText ->

            val isFull = page.isFull()
            if (isFull) {
                pageLineIndex = 0
                footer = PageFooter(width, mFooterHeight, mFooterPaint, mPaddingLeft, mPaddingRight)
                page = Page(width, height, mContentPaint, mTitlePaint, Paint(), mLineSpace.toFloat(), header, footer, padding)
                mPages.add(page)
            }

            page.addLineText(lineText, LineType.CONTENT)
            pageLineIndex++

            if ((mPages.size == 3 && pageLineIndex == 5) || (mPages.size % 5 == 0 && pageLineIndex == 8)) {
                page.addLineAd(mAdBitmap!!)
            }

        }

        if (!preload) {
            mPages.forEachIndexed { index, page ->
                page.updateFooter(index + 1, mPages.size)
            }
        }

        Page.updateTime()
    }

    fun prevPage() {
        mPage--
        if (mPage <= 0) {
            mPage = 1
        }
        Page.updateTime()
        invalidate()
    }

    fun nextPage() {
        if (mPage > mPages.size) {
            return
        }
        mPage++
        Page.updateTime()
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

        if (mPage <= 1 || mIsPaging) {
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
                mIsPaging = false
            }

        })
        animator.start()
        mIsPaging = true
    }

    fun nextPageWithAnim() {

        if (mPage > mPages.size - 1 || mIsPaging) {
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
                mIsPaging = false
            }

        })
        animator.start()
        mIsPaging = true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mTouchX = event.rawX
                mTouchY = event.rawY
                return true
            }
            MotionEvent.ACTION_MOVE -> {

                moveX = event.rawX - mTouchX
                if (mPage == 1 && moveX > ViewConfiguration.getTouchSlop()) {
//                    moveX = 0f
                } else if (mPage == mPages.size && moveX < - ViewConfiguration.getTouchSlop()) {
//                    moveX = 0f
                } else if (Math.abs(moveX) > ViewConfiguration.getTouchSlop()){
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {

                mTouchX = event.rawX
                when {
                    moveX < - ViewConfiguration.getTouchSlop() -> nextPageWithAnim()
                    moveX > ViewConfiguration.getTouchSlop() -> prevPageWithAnim()
                    else -> {
                        val touchType = when {
                            mPages[mPage - 1].isInAd(mTouchY) -> TouchType.AD
                            mTouchX < width * 0.3 -> TouchType.LEFT
                            mTouchX > width * 0.7 -> TouchType.RIGHT
                            else -> TouchType.CENTER
                        }
                        mPageTouchLinstener?.onClick(touchType, mPages[mPage - 1])
                    }
                }
            }
        }

        return super.onTouchEvent(event)
    }
}