package com.example.customfancontroller

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private enum class FanSpeed(val label: Int) {

    OFF(R.string.fan_off),
    LOW(R.string.fan_low),
    MEDIUM(R.string.fan_medium),
    HIGH(R.string.fan_high);


    fun next() = when (this) {
        OFF -> LOW
        LOW -> MEDIUM
        MEDIUM -> HIGH
        HIGH -> OFF
    }

}


private const val RADIUS_OFFSET_LABEL = 30
private const val RADIUS_OFFSET_INDICATOR = -35


class DialView @JvmOverloads constructor(

    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(
    context,
    attrs,
    defStyleAttr,

    ) {


    init {
        isClickable = true
        context.withStyledAttributes(attrs, R.styleable.DialView) {
            fanSpeedLowColor = getColor(R.styleable.DialView_fanColor1, 0)
            fanSpeedMediumColor = getColor(R.styleable.DialView_fanColor2, 0)
            fanSeedMaxColor = getColor(R.styleable.DialView_fanColor3, 0)

        }
    }

    private var fanSpeedLowColor = 0
    private var fanSpeedMediumColor = 0
    private var fanSeedMaxColor = 0
    override fun performClick(): Boolean {

        if (super.performClick()) return true
        fanSpeed = fanSpeed.next()
        contentDescription = resources.getString(fanSpeed.label)

        invalidate()
        return true
    }

    private var radius = 0.0f
    private var fanSpeed = FanSpeed.OFF
    private val pointPosition = PointF(0.0f, 0.0f)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create("", Typeface.BOLD)
    }


    /// to calculate the view size when it appears  at first and anytime size changed
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {

        radius = (min(w, h) / 2.0 * 0.8).toFloat()

    }

    /// to draw a custom view , suing the canvas object styled by a paint  object


    private fun PointF.computeXYForSpeed(
        pos: FanSpeed,
        radius: Float
    ) {

        val startAngle = Math.PI * (9 / 8.0)
        val angle = startAngle + pos.ordinal * (Math.PI / 4)
        x = (radius * cos(angle)).toFloat() + width / 2
        y = (radius * sin(angle)).toFloat() + height / 2

    }

    override fun onDraw(canvas: Canvas) {

        super.onDraw(canvas)


        paint.color = when (fanSpeed) {
            FanSpeed.OFF -> Color.GRAY
            FanSpeed.LOW -> fanSpeedLowColor
            FanSpeed.MEDIUM -> fanSpeedMediumColor
            FanSpeed.HIGH -> fanSeedMaxColor

        }
        canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), radius, paint)
        val markerRadius = radius + RADIUS_OFFSET_INDICATOR
        pointPosition.computeXYForSpeed(fanSpeed, markerRadius)
        paint.color = Color.BLACK
        canvas.drawCircle(
            pointPosition.x,
            pointPosition.y,
            radius / 12,
            paint
        )

        val labelRadius = radius + RADIUS_OFFSET_LABEL
        for (i in FanSpeed.entries) {
            pointPosition.computeXYForSpeed(i, labelRadius)
            val label = resources.getString(i.label)
            canvas.drawText(label, pointPosition.x, pointPosition.y, paint)
        }
    }

    /// NOTE : YOU NEED TO CALL invalidate() method , when responding tp a user
    /// click that changed how the view is drawn , forcing a call to onDraw() to draw again the view
    ///to reflect the new changes and redaw it again with the new updates
    /// onDraw() called each time the screen refresh many times in a second may refrehes
    // do as little work  as possible on draw
    /// dont place   memory allocation inside of it to escape from garbage collection
///In onCreate(), the size of the canvas is returned as 0,0 because its size has not been determined yet.
}