package com.julic20s.fleur.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.julic20s.fleur.R
import kotlin.math.abs

class WaveView(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
    View(context, attrs, defStyleAttr, defStyleRes) {

    private var _phase = 0
    private var _strength = 50f
    private var _length = 180f

    var progress = 0.5f
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }

    private val _paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val _path = Path()

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : this(context, attrs, defStyleAttr, 0)

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.WaveView)
        _paint.color = array.getColor(R.styleable.WaveView_wave_color, Color.BLACK)
        progress = array.getInt(R.styleable.WaveView_progress, 50) / 100f
        array.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        val cy =  height * (1 - progress)

        val startX = -abs(_phase % width).toFloat()
        val endX = width.toFloat()
        _path.reset()
        _path.moveTo(startX, cy)
        var curX = startX
        var ctr = -_strength
        while (curX < endX) {
            _path.rQuadTo(_length / 2, ctr, _length, 0f)
            curX += _length
            ctr = -ctr
        }
        _path.lineTo(endX, height.toFloat())
        _path.lineTo(startX, height.toFloat())
        _path.close()

        canvas.drawPath(_path, _paint)
    }

    var color: Int
        get() = _paint.color
        set(value) {
            _paint.color = value
            invalidate()
        }

    fun reshape(phase: Int, strength: Float, length: Float) {
        _phase = phase
        _strength = strength
        _length = length
        invalidate()
    }
}