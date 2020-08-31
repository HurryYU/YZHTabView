package com.hurryyu.yzhtableview.simple

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

/**
 * @author HurryYu
 * https://www.hurryyu.com
 * https://github.com/HurryYu
 * 2020-07-10
 */
class TestView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path: Path = Path()

    init {
        paint.apply {
            color = Color.BLACK
            strokeWidth = 2F
            style = Paint.Style.STROKE
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        path.moveTo(0F, 0F)
        path.quadTo(300F, 300F, 0F, 600F)
        canvas?.drawPath(path, paint)
        paint.style = Paint.Style.FILL
        canvas?.drawCircle(300F,300F,5F,paint)
    }
}