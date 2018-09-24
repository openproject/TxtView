package com.jayfeng.txtview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Handler
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import com.jayfeng.lesscode.core.DisplayLess
import com.jayfeng.lesscode.core.FileLess
import com.jayfeng.lesscode.core.ResourceLess
import com.jayfeng.txtview.page.*
import com.jayfeng.txtview.theme.NightTheme
import com.jayfeng.txtview.touch.PageTouchLinstener
import com.jayfeng.txtview.touch.TouchType
import java.text.SimpleDateFormat
import java.util.*

class TxtView : ViewGroup {

    companion object {

        var pageTime = "00:00"
        var contentPaint = Paint()
        var titlePaint = Paint()

        fun updateTime() {
            val time = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("HH:mm")
            pageTime = dateFormat.format(Date(time))
        }
    }

    private var mHeaderPaint = Paint()
    private val mHeaderHeight: Int by lazy { DisplayLess.`$dp2px`(48f) }
    private var mFooterPaint = Paint()
    private val mFooterHeight: Int by lazy { DisplayLess.`$dp2px`(48f) }

    private val mPaddingLeft: Float by lazy { DisplayLess.`$dp2px`(28f).toFloat() }
    private val mPaddingTop: Float by lazy { DisplayLess.`$dp2px`(8f).toFloat() }
    private val mPaddingRight: Float by lazy { DisplayLess.`$dp2px`(28f).toFloat() }
    private val mPaddingBottom: Float by lazy { DisplayLess.`$dp2px`(8f).toFloat() }

    private var mContent: String = ""
    private var mPage: Int = 1
    private var mLines = ArrayList<String>()
    private val mLineSpace: Int by lazy { DisplayLess.`$dp2px`(8f) }
    private var mIsPaging = false     // 正在翻页动画状态
    private var mIsParsing = false    // 正在转化文本：第一次初始化、设置字体大小时触发

    private var mTouchX = 0f
    private var mTouchY = 0f
    private var moveX = 0f

    var mAdBitmap: Bitmap? = null

    private var mPages = ArrayList<Page>()
    private val mShadowPaint = Paint()
    private val mShadowGradient: LinearGradient by lazy {
        LinearGradient(
                measuredWidth.toFloat(), 0f, measuredWidth.toFloat() + 16f, 0f, intArrayOf(Color.parseColor("#88666666"), Color.TRANSPARENT), null, Shader.TileMode.CLAMP)
    }
    private val mShadowRect: Rect by lazy {
        Rect(measuredWidth, 0, measuredWidth + 16, measuredHeight).apply {
            mShadowPaint.shader = mShadowGradient
        }
    }

    private val mPunctuationSet: Set<Char> = setOf('，', '。', '？', '！', '：', '“', '”',
            ',', '.', '?', '!', ':', '"')

    var mPageTouchLinstener: PageTouchLinstener? = null

    private var mPageBitmapMap: HashMap<Int, Bitmap> = HashMap()


    private var title = ""
    private var renderMode: RenderMode = RenderMode.DOUBLE_BUFFER
    private var nightMode: Boolean = false

    // Long Pressed
    private var isLongPressed = false
    private var isLongPressedCleared = false
    private val longPressedHandler = Handler()

    private var isLoading = true
    private var loadingView: View? = null

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
    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        for (i in 0..(childCount - 1)) {
            val childView = getChildAt(i)
            if (childView.id == ResourceLess.`$id`(context, "txtViewLoadingView", ResourceLess.TYPE.ID)) {
                // Loading View
                loadingView = childView
                measureChild(childView, widthMeasureSpec, heightMeasureSpec)
            }
        }

        setMeasuredDimension(widthSize, heightSize)
    }

    override fun onLayout(p0: Boolean, p1: Int, p2: Int, p3: Int, p4: Int) {

        loadingView?.let {
            val cWidth = it.measuredWidth
            val cHeight = it.measuredHeight

            it.layout((width - cWidth) / 2,
                    (height - cHeight) / 2,
                    (width + cWidth) / 2,
                    (height + cHeight) / 2)
        }
    }

    override fun onDraw(canvas: Canvas) {
        when {
            Math.abs(moveX) < ViewConfiguration.getTouchSlop() -> {
                drawPage(canvas, mPage)
            }
            moveX < -ViewConfiguration.getTouchSlop() -> {

                canvas.save()
                canvas.clipRect(width + moveX, 0f, width.toFloat(), height.toFloat())
                drawPage(canvas, mPage + 1)
                canvas.restore()

                canvas.save()
                canvas.translate(moveX, 0f)
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
        when (renderMode) {
            RenderMode.NORMAL -> pageData.draw(canvas)
            RenderMode.DOUBLE_BUFFER -> {
                if (mPageBitmapMap[page - 1] == null) {
                    pageData.draw(canvas)
                } else {
                    canvas.drawBitmap(mPageBitmapMap[page - 1], 0f, 0f, null)
                }
            }
        }
    }

    private fun preloadPageBitmap(page: Int, forward: Boolean) {
        val pageIndex = page - 1
        if (pageIndex < mPages.size && pageIndex > 0 && mPageBitmapMap[pageIndex] == null) {
            Thread {
                val pageBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                val pageCanvas = Canvas(pageBitmap)
                if (background != null) {
                    background.draw(pageCanvas)
                }
                mPages[pageIndex].draw(pageCanvas)

                mPageBitmapMap[pageIndex] = pageBitmap
                if (forward && pageIndex - 2 >= 0) {
                    mPageBitmapMap[pageIndex - 2]?.recycle()
                    mPageBitmapMap.remove(pageIndex - 2)
                } else if ((!forward) && pageIndex + 2 < mPages.size) {
                    mPageBitmapMap.get(pageIndex + 2)?.recycle()
                    mPageBitmapMap.remove(pageIndex + 2)
                }
            }.start()
        }
    }

    fun setTxtFile(path: String) {
        // NEXT PLAN

        setContent(FileLess.`$read`(context.resources.assets.open("demo.txt")))
    }

    fun setContent(content: String) {
        this.mContent = content

        this.isLoading = false
        this.loadingView?.visibility = View.GONE

        post {
            Thread {
                val startTime = System.currentTimeMillis()

                parseContent(true)

                postInvalidate()

                Log.d("feng", "-------- parse cost: " + (System.currentTimeMillis() - startTime))
            }.start()
        }
    }

    private fun parseContent(preload: Boolean) {
        mIsParsing = true
        mLines.clear()

        val contentWidth = measuredWidth.toFloat() - mPaddingLeft - mPaddingRight
        val widthPaintLength = contentPaint.breakText("测试字符串测试字符串测试字符串测试字符串测试字符串测试字符串字符串测试字符串测试字符符串测试字符串",
                false, contentWidth, null)
        var preloadPage = preload
        mContent.split("\n").forEach { paragraph ->
            var startIndex = 0
            while (startIndex < paragraph.length) {
                var endIndex = startIndex + widthPaintLength
                if (endIndex > paragraph.length) {
                    endIndex = paragraph.length
                }

                var lineText = paragraph.substring(startIndex, endIndex)
                while (contentPaint.measureText(lineText + "好") < contentWidth && endIndex < paragraph.length) {
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

        mIsParsing = false
    }

    private fun parseLineToPage(preload: Boolean) {

        val header = PageHeader(width, mHeaderHeight, mHeaderPaint, "王二狗的那些神话", mPaddingLeft, mPaddingRight)
        var footer = PageFooter(width, mFooterHeight, mFooterPaint, mPaddingLeft, mPaddingRight)
        val padding = PagePadding(mPaddingLeft, mPaddingTop, mPaddingRight, mPaddingBottom)
        var page = Page(width, height, Paint(), mLineSpace.toFloat(), header, footer, padding)
        mPages.add(page)

        // add title
        if (!TextUtils.isEmpty(title)) {
            page.addLineText(title, LineType.TITLE)
        }

        // add content
        var pageLineIndex = 0
        var pageLength = 0
        mLines.forEach { lineText ->

            val isFull = page.isFull()
            if (isFull) {

                if (mPages.size > 0) {
                    mPages.last().length = pageLength
                }

                pageLineIndex = 0
                pageLength = 0
                footer = PageFooter(width, mFooterHeight, mFooterPaint, mPaddingLeft, mPaddingRight)
                page = Page(width, height, Paint(), mLineSpace.toFloat(), header, footer, padding)
                if (mPages.size > 0) {
                    page.start = mPages.last().start + mPages.last().length
                }
                mPages.add(page)
            }

            page.addLineText(lineText, LineType.CONTENT)
            pageLineIndex++
            pageLength += lineText.length

            if ((mPages.size == 3 && pageLineIndex == 5) || (mPages.size % 5 == 0 && pageLineIndex == 8)) {
                page.addLineAd(mAdBitmap!!)
            }

        }

        if (!preload) {
            mPages.forEachIndexed { index, page ->
                // update footer
                page.updateFooter(index + 1, mPages.size)
                // update start, length
                page.start
            }
        }

        updateTime()
    }

    fun prevPage() {
        mPage--
        if (mPage <= 0) {
            mPage = 1
        }
        updateTime()

        invalidate()
        if (renderMode == RenderMode.DOUBLE_BUFFER) {
            preloadPageBitmap(mPage - 1, false)
        }
    }

    fun nextPage() {
        if (mPage > mPages.size) {
            return
        }
        mPage++
        updateTime()

        invalidate()
        if (renderMode == RenderMode.DOUBLE_BUFFER) {
            preloadPageBitmap(mPage + 1, true)
        }
    }

    fun gotoPage(pageIndex: Int) {
        var pageIndex = pageIndex
        if (pageIndex < 1) {
            pageIndex = 1
        } else if (pageIndex > mPages.size) {
            pageIndex = mPages.size
        }

        mPage = pageIndex
        updateTime()

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
        animator.duration = 160
        animator.addUpdateListener { va ->
            moveX = va.animatedValue as Float
            invalidate()
        }
        animator.addListener(object : AnimatorListenerAdapter() {
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
        animator.duration = 160
        animator.addUpdateListener { va ->
            moveX = va.animatedValue as Float
            invalidate()
        }
        animator.addListener(object : AnimatorListenerAdapter() {
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

    fun release() {
        if (mAdBitmap?.isRecycled == false) {
            mAdBitmap?.recycle()
        }
        mPageBitmapMap.forEach { _, bitmap ->
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        if (isLoading) {
            return false
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mTouchX = event.rawX
                mTouchY = event.rawY

                isLongPressed = false
                isLongPressedCleared = false
                longPressedHandler.postDelayed({

                    isLongPressed = true

                    mTouchX = event.rawX
                    if (Math.abs(moveX) < ViewConfiguration.getTouchSlop()) {
                        moveX = 0f
                        val touchType = when {
                            mPages[mPage - 1].isInAd(mTouchY) -> TouchType.AD
                            mTouchX < width * 0.3 -> TouchType.LEFT
                            mTouchX > width * 0.7 -> TouchType.RIGHT
                            else -> TouchType.CENTER
                        }
                        mPageTouchLinstener?.onLongPressed(touchType, mPages[mPage - 1])
                    }
                }, ViewConfiguration.getLongPressTimeout().toLong())


                return true
            }
            MotionEvent.ACTION_MOVE -> {

                if (isLongPressed) {
                    return false
                }

                moveX = event.rawX - mTouchX
                if (mPage == 1 && moveX > ViewConfiguration.getTouchSlop()) {
//                    moveX = 0f
                } else if (mPage == mPages.size && moveX < -ViewConfiguration.getTouchSlop()) {
//                    moveX = 0f
                } else if (Math.abs(moveX) > ViewConfiguration.getTouchSlop()) {
                    if (!isLongPressedCleared) {
                        longPressedHandler.removeCallbacksAndMessages(null)
                        isLongPressedCleared = true
                    }
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                longPressedHandler.removeCallbacksAndMessages(null)
                if (!isLongPressed) {
                    mTouchX = event.rawX
                    when {
                        moveX < -ViewConfiguration.getTouchSlop() -> nextPageWithAnim()
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
        }

        return super.onTouchEvent(event)
    }

    fun isNightMode(): Boolean {
        return nightMode
    }

    fun scaleFont(cPaint: Paint) {

        if (mIsParsing) {
            return
        }

        if (nightMode) {
            val theme = NightTheme()
            TxtView.contentPaint = theme.toNight(cPaint)
        } else {
            TxtView.contentPaint = cPaint
        }

        // clear outdated bitmap cache
        mPageBitmapMap.forEach { _, bitmap ->
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
        mPageBitmapMap.clear()

        val currentPage = mPages[mPage - 1]
        val currentStart = currentPage.start + currentPage.length / 2


        Thread {

            parseContent(false)

            var newPageIndex = 0
            run breaking@ {
                mPages.forEachIndexed { index, page ->
                    if (page.start > currentStart) {
                        newPageIndex = index
                        return@breaking
                    }
                }
            }

            mPage = newPageIndex
            postInvalidate()
        }.start()
    }

    class Builder(private val txtView: TxtView) {

        private var title = ""
        private var renderMode: RenderMode = RenderMode.NORMAL
        private var nightMode: Boolean = false
        private var backgroud: Drawable? = null

        private var contentPaint: Paint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#424242")
            textSize = DisplayLess.`$dp2px`(17.0f).toFloat()
        }

        private var titlePaint: Paint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#424242")
            textSize = DisplayLess.`$dp2px`(24.0f).toFloat()
        }


        fun setNightMode(nightMode: Boolean): Builder {
            this.nightMode = nightMode
            return this@Builder
        }

        fun setTitle(title: String): Builder {
            this.title = title
            return this@Builder
        }

        fun setRenderMode(renderMode: RenderMode): Builder {
            this.renderMode = renderMode
            return this@Builder
        }

        fun setBackgroudDrawable(drawableId: Int): Builder {
            this.backgroud = txtView.resources.getDrawable(drawableId)
            return this@Builder
        }

        fun setContentPainter(paint: Paint): Builder {
            this.contentPaint = paint
            return this@Builder
        }

        fun setTitlePainter(paint: Paint): Builder {
            this.titlePaint = paint
            return this@Builder
        }

        fun build() {

            txtView.title = this.title
            txtView.nightMode = this.nightMode
            txtView.renderMode = this.renderMode
            if (backgroud != null) {
                txtView.setBackgroundDrawable(backgroud)
            }

            if (nightMode) {
                val theme = NightTheme()
                TxtView.contentPaint = theme.toNight(contentPaint)
                TxtView.titlePaint = theme.toNight(titlePaint)
                txtView.setBackgroundColor(Color.BLACK)
            } else {
                TxtView.contentPaint = this.contentPaint
                TxtView.titlePaint = this.titlePaint
            }

            txtView.mPageBitmapMap.forEach { _, bitmap ->
                if (!bitmap.isRecycled) {
                    bitmap.recycle()
                }
            }
            txtView.mPageBitmapMap.clear()
            txtView.postInvalidate()
        }

    }
}