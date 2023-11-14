package ru.netology.nmedia.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.withStyledAttributes
import ru.netology.nmedia.R
import ru.netology.nmedia.util.AndroidUtils
import kotlin.math.min
import kotlin.random.Random

class StatsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : View(context, attrs, defStyleAttr, defStyleRes) {
    private var radius = 0F
    private var center = PointF(0F, 0F)
    private var oval = RectF(0F, 0F, 0F, 0F)

    private var lineWidth = AndroidUtils.dp(context, 5F).toFloat()
    private var fontSize = AndroidUtils.dp(context, 40F).toFloat()
    private var colors = emptyList<Int>()

    private var progress = 0F

    private var arcFillingAnimator: ValueAnimator? = null
    private var arcRotationAnimator: ValueAnimator? = null

    init {
        context.withStyledAttributes(attrs, R.styleable.StatsView) {
            lineWidth = getDimension(R.styleable.StatsView_lineWidth, lineWidth)
            fontSize = getDimension(R.styleable.StatsView_fontSize, fontSize)
            val resId = getResourceId(R.styleable.StatsView_colors, 0)
            colors = resources.getIntArray(resId).toList()
        }
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = lineWidth
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private var paintForFirstArc = Paint(paint).apply { color = 0xFF000000.toInt() }

    private val paintForCircle: Paint = Paint(paint).apply { color = 0xFFD3D3D3.toInt() }

    //
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = fontSize
    }

    private var arcRotation = 0F


    var data: List<Float> = emptyList()
        set(value) {
            field = value
            update()
        }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = min(w, h) / 2F - lineWidth / 2
        center = PointF(w / 2F, h / 2F)
        oval = RectF(
            center.x - radius, center.y - radius,
            center.x + radius, center.y + radius,
        )
    }

    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty()) {
            return
        }

        canvas.drawCircle(center.x, center.y, radius, paintForCircle)


        val newData: List<Float> = data.subList(0, data.size - 1)


        val dataSum = data.sum()
        var actualPercents = 0F

        val sweepAngleForFirstElement = (360 * (data[0] / dataSum)) / 5
        var startFrom = -90F

        canvas.save()
        canvas.rotate(arcRotation, center.x, center.y)
        for ((index, datum) in newData.withIndex()) {
            val segment = datum / dataSum
            val angle = 360F * segment
            paint.color = colors.getOrNull(index) ?: randomColor()
            canvas.drawArc(oval, startFrom, angle * progress, false, paint)
            startFrom += angle
            actualPercents += segment

            Log.d("actual", "$actualPercents, $segment")

            if (newData.size == data.size) {
                if (index == 0) {
                    paintForFirstArc.color = paint.color

                }
            }
        }

        if (newData.size == data.size) {
            canvas.drawArc(
                oval,
                startFrom,
                sweepAngleForFirstElement * progress,
                false,
                paintForFirstArc
            )
        }

        canvas.restore()

        canvas.drawText(

            "%.2f%%".format(progress * 100 * actualPercents),
            center.x,
            center.y + textPaint.textSize / 4,
            textPaint,
        )

    }

    private fun update() {
        arcFillingAnimator?.let {
            it.removeAllListeners()
            it.cancel()
        }

        arcRotationAnimator?.let {
            it.removeAllListeners()
            it.cancel()
        }

        progress = 0F

        arcFillingAnimator = ValueAnimator.ofFloat(0F, 1F).apply {
            addUpdateListener { anim ->
                progress = anim.animatedValue as Float
                invalidate()
            }
            duration = 2000
            interpolator = LinearInterpolator()
        }.also {
            it.start()
        }

        arcRotationAnimator = ValueAnimator.ofFloat(0F, 360F).apply {
            addUpdateListener { anim ->
                arcRotation = anim.animatedValue as Float
            }
            duration = 2000
            interpolator = LinearInterpolator()
        }.also {
            it.start()
        }

    }


    private fun randomColor() = Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt())
}