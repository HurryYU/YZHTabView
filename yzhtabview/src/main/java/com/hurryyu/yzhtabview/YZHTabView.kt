package com.hurryyu.yzhtabview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.getDefaultSize
import kotlin.math.min

/**
 * @author HurryYu
 * https://www.hurryyu.com
 * https://github.com/HurryYu
 * 2020-07-09
 */

typealias OnTabSelectedListener = (position: Int) -> Unit

class YZHTabView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val clipPath = Path()

    private val dividerPath = Path()

    private val viewRect = RectF()

    private var currentTabPosition = -1

    private var tabViewWidth = 0F

    private var arcWidth = DEFAULT_ARC_WIDTH

    /**
     * View高度
     */
    private var viewHeight = DEFAULT_HEIGHT
    /**
     * View宽度 除非手动指定,否则都是按照match_parent处理
     */
    private var viewWidth = 0

    /**
     * 所有Tab的文字集合
     */
    private val tabItemTextList = mutableListOf<String>()

    /**
     * View圆角大小
     */
    private var viewCorner = DEFAULT_VIEW_CORNER

    /**
     * View背景颜色
     */
    private var viewBackgroundColor = DEFAULT_VIEW_BACKGROUND_COLOR

    /**
     * Tab选中的背景颜色
     */
    private var tabSelectedColor = DEFAULT_TAB_SELECTED_BACKGROUND_COLOR

    /**
     * Tab选中文字颜色
     */
    private var tabSelectedTextColor = DEFAULT_TAB_SELECTED_TEXT_COLOR

    /**
     * Tab未选中文字颜色
     */
    private var tabUnSelectedTextColor = DEFAULT_TAB_UN_SELECTED_TEXT_COLOR

    /**
     * Tab选中文字大小
     */
    private var tabSelectedTextSize = DEFAULT_TAB_SELECTED_TEXT_SIZE

    /**
     * Tab未选中文字大小
     */
    private var tabUnSelectedTextSize = DEFAULT_TAB_UN_SELECTED_TEXT_SIZE

    private var dividerControlX = DEFAULT_DIVIDER_CONTROL_X

    private var dividerControlY = DEFAULT_DIVIDER_CONTROL_Y

    /**
     * Tab的数量
     */
    var tabItemCount = 0
        private set
        get() = tabItemTextList.size

    private var tabSelectedListener: OnTabSelectedListener? = null

    init {
        initAttrs(attrs)

        paint.apply {
            style = Paint.Style.FILL
        }
    }

    private fun initAttrs(attrs: AttributeSet?) {
        attrs?.let {
            context.obtainStyledAttributes(it, R.styleable.YZHTabView)
        }?.apply {
            arcWidth =
                getDimensionPixelSize(R.styleable.YZHTabView_arcDividerWidth, DEFAULT_ARC_WIDTH)
            viewCorner = getDimensionPixelSize(R.styleable.YZHTabView_corner, DEFAULT_VIEW_CORNER)
            viewBackgroundColor = getColor(
                R.styleable.YZHTabView_backgroundColor,
                DEFAULT_VIEW_BACKGROUND_COLOR
            )
            tabSelectedColor = getColor(
                R.styleable.YZHTabView_tabSelectedBackgroundColor,
                DEFAULT_TAB_SELECTED_BACKGROUND_COLOR
            )
            tabSelectedTextColor = getColor(
                R.styleable.YZHTabView_tabSelectedTextColor,
                DEFAULT_TAB_SELECTED_TEXT_COLOR
            )
            tabSelectedTextSize = getDimensionPixelSize(
                R.styleable.YZHTabView_tabSelectedTextSize,
                DEFAULT_TAB_SELECTED_TEXT_SIZE
            )
            tabUnSelectedTextColor = getColor(
                R.styleable.YZHTabView_tabUnSelectedTextColor,
                DEFAULT_TAB_UN_SELECTED_TEXT_COLOR
            )
            tabUnSelectedTextSize = getDimensionPixelSize(
                R.styleable.YZHTabView_tabUnSelectedTextSize,
                DEFAULT_TAB_UN_SELECTED_TEXT_SIZE
            )
            dividerControlX = getDimensionPixelSize(
                R.styleable.YZHTabView_dividerControlX,
                DEFAULT_DIVIDER_CONTROL_X
            )
            dividerControlY = getDimensionPixelSize(
                R.styleable.YZHTabView_dividerControlY,
                DEFAULT_DIVIDER_CONTROL_Y
            )
        }?.recycle()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val x = event.x
                for (i in 1..tabItemCount) {
                    val tabEndX = i * tabViewWidth + arcWidth / 2F + (i - 1) * arcWidth
                    if (x <= tabEndX) {
                        currentTabPosition = i - 1
                        tabSelectedListener?.invoke(currentTabPosition)
                        invalidate()
                        return true
                    }
                }
                return false
            }
            MotionEvent.ACTION_UP -> {
                performClick()
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(
            getDefaultSize(suggestedMinimumWidth, widthMeasureSpec),
            measureHeight(heightMeasureSpec)
        )
    }

    private fun measureHeight(heightMeasureSpec: Int): Int {
        val size = MeasureSpec.getSize(heightMeasureSpec)
        return when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.UNSPECIFIED,
            MeasureSpec.AT_MOST -> {
                min(size, viewHeight)
            }
            MeasureSpec.EXACTLY -> {
                size
            }
            else -> {
                viewHeight
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewHeight = h
        viewWidth = w
        viewRect.set(0F, 0F, w.toFloat(), h.toFloat())

        clipPath.addRoundRect(
            viewRect,
            viewCorner.toFloat(),
            viewCorner.toFloat(),
            Path.Direction.CW
        )

        val totalArcWidth = (tabItemCount - 1) * arcWidth
        tabViewWidth = (viewWidth - totalArcWidth).toFloat() / tabItemCount
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.color = viewBackgroundColor
        canvas.clipPath(clipPath)
        canvas.drawRect(viewRect, paint)
        drawArcDividerByTabPosition(canvas, currentTabPosition)
        drawTabText(canvas, currentTabPosition)
    }

    private fun drawTabText(canvas: Canvas, selectedPosition: Int) {
        val halfArcWidth = arcWidth / 2F
        for ((index, itemText) in tabItemTextList.withIndex()) {
            val left = tabViewWidth * index + arcWidth * index - halfArcWidth
            val right = left + tabViewWidth + arcWidth
            val centerX = left + (right - left) / 2
            val centerY = viewHeight / 2F
            if (index == selectedPosition) {
                drawTabSelectedText(canvas, centerX, centerY, itemText)
            } else {
                drawTabUnSelectedText(canvas, centerX, centerY, itemText)
            }
        }
    }

    private fun drawTabSelectedText(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        text: String
    ) {
        val fontMetrics = paint.run {
            color = tabSelectedTextColor
            textSize = tabSelectedTextSize.toFloat()
            textAlign = Paint.Align.CENTER
            fontMetrics
        }
        val offsetY = (fontMetrics.top + fontMetrics.bottom) / 2
        canvas.drawText(text, centerX, centerY - offsetY, paint)
    }

    private fun drawTabUnSelectedText(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        text: String
    ) {
        val fontMetrics = paint.run {
            color = tabUnSelectedTextColor
            textSize = tabUnSelectedTextSize.toFloat()
            textAlign = Paint.Align.CENTER
            fontMetrics
        }
        val offsetY = (fontMetrics.top + fontMetrics.bottom) / 2
        canvas.drawText(text, centerX, centerY - offsetY, paint)
    }

    private fun drawArcDividerByTabPosition(canvas: Canvas, position: Int) {
        if (position < 0) {
            return
        }
        dividerPath.reset()
        when (position) {
            0 -> {
                drawStartArcDivider()
            }
            tabItemCount - 1 -> {
                drawEndArcDivider()
            }
            else -> {
                drawCenterDivider(position)
            }
        }
        paint.color = tabSelectedColor
        canvas.drawPath(dividerPath, paint)
    }

    private fun drawStartArcDivider() {
        dividerPath.apply {
            moveTo(0F, 0F)
            lineTo(tabViewWidth, 0F)
            cubicTo(
                tabViewWidth + dividerControlX, dividerControlY.toFloat(),
                tabViewWidth + arcWidth - dividerControlX, (viewHeight - dividerControlY).toFloat(),
                tabViewWidth + arcWidth, viewHeight.toFloat()
            )
            lineTo(0F, viewHeight.toFloat())
            lineTo(0F, 0F)
        }
    }

    private fun drawEndArcDivider() {
        dividerPath.apply {
            moveTo(viewWidth.toFloat(), 0F)
            lineTo(viewWidth - tabViewWidth, 0F)
            cubicTo(
                viewWidth - tabViewWidth - dividerControlX,
                dividerControlY.toFloat(),
                viewWidth - tabViewWidth - arcWidth + dividerControlX,
                (viewHeight - dividerControlY).toFloat(),
                viewWidth - tabViewWidth - arcWidth,
                viewHeight.toFloat()
            )
            lineTo(viewWidth.toFloat(), viewHeight.toFloat())
        }
    }

    private fun drawCenterDivider(position: Int) {
        val tabViewStartX = position * tabViewWidth + position * arcWidth
        val tabViewEndX = position * tabViewWidth + position * arcWidth + tabViewWidth
        dividerPath.apply {
            moveTo(tabViewStartX, 0F)
            lineTo(tabViewEndX, 0F)
            cubicTo(
                tabViewEndX + dividerControlX, dividerControlY.toFloat(),
                tabViewEndX + arcWidth - dividerControlX, (viewHeight - dividerControlY).toFloat(),
                tabViewEndX + arcWidth, viewHeight.toFloat()
            )
            lineTo(tabViewStartX - arcWidth, viewHeight.toFloat())
            cubicTo(
                tabViewStartX - arcWidth + dividerControlX,
                (viewHeight - dividerControlY).toFloat(),
                tabViewStartX - dividerControlX,
                dividerControlY.toFloat(),
                tabViewStartX,
                0F
            )
        }
    }

    fun setTabItemTextList(tabItemTextList: MutableList<String>) {
        this.tabItemTextList.clear()
        this.tabItemTextList.addAll(tabItemTextList)
        setCurrentTabPosition(currentTabPosition)
    }

    fun setCurrentTabPosition(position: Int) {
        this.currentTabPosition = when {
            position < 0 -> 0
            this.tabItemCount == 0 -> position
            position > tabItemCount - 1 -> tabItemCount - 1
            else -> position
        }
    }

    fun setOnTabSelectedListener(listener: OnTabSelectedListener) {
        this.tabSelectedListener = listener
    }

    companion object {
        /**
         * 默认高度
         */
        private val DEFAULT_HEIGHT: Int = 50.dp

        /**
         * 默认圆角大小
         */
        private const val DEFAULT_VIEW_CORNER = 0

        /**
         * View默认背景颜色
         */
        private val DEFAULT_VIEW_BACKGROUND_COLOR = Color.parseColor("#38A5FF")

        /**
         * 选中Tab的背景颜色
         */
        private val DEFAULT_TAB_SELECTED_BACKGROUND_COLOR = Color.parseColor("#78C2FF")

        /**
         * 选中Tab的文字颜色
         */
        private const val DEFAULT_TAB_SELECTED_TEXT_COLOR = Color.WHITE

        /**
         * 未选中Tab的文字颜色
         */
        private const val DEFAULT_TAB_UN_SELECTED_TEXT_COLOR = Color.WHITE

        /**
         * 选中Tab的文字大小
         */
        private val DEFAULT_TAB_SELECTED_TEXT_SIZE = 14.sp

        /**
         * 未选中Tab的文字大小
         */
        private val DEFAULT_TAB_UN_SELECTED_TEXT_SIZE = 14.sp

        /**
         * Tab弧度分割线宽度
         */
        private val DEFAULT_ARC_WIDTH = 25.dp

        /**
         * 曲线控制点X
         */
        private val DEFAULT_DIVIDER_CONTROL_X = 20.dp

        /**
         * 曲线控制点Y
         */
        private val DEFAULT_DIVIDER_CONTROL_Y = 2.dp
    }
}