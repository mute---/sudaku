package org.klava.sudaku

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.*
import android.preference.PreferenceManager
import android.text.TextPaint
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

class InputView(context: Context) : View(context), SharedPreferences.OnSharedPreferenceChangeListener {
    private val labelTextPaint = TextPaint()
    private val gridPaint = Paint()
    private val fillPaint = Paint()

    private val gridRect = RectF()

    private val density = Resources.getSystem().displayMetrics.density
    private var fontSize = 16
    private var cellPadding = 8
    private var gridAlignment = 2

    private var w = 0f
    private var h = 0f
    private var cellSize = 0f
    private var halfCellSize = 0f
    private var zoneSize = 0f
    private val normalGridLine = 1
    private val fatGridLine = 3
    private var textOffset = 0f
    private val cursorThreshold = 2 * density
    private var useCenterDetection = false

    private var tolerantZones: Array<Int>
    private var centerZones = arrayOf(10, 13, 16, 37, 40, 43, 64, 67, 70)

    private var startZone: Int? = null
    private var currentZone: Int? = null
    private var currentTouchX: Float? = null
    private var isStartFromCenter = false
    private var touchUps = 0

    private lateinit var layout: Layout
    private lateinit var eventListener: IKeyboardEventListener
    private var shiftState = ShiftState.Off
    private var isCursorMode = false

    init {
        labelTextPaint.isAntiAlias = true
        labelTextPaint.color = Color.BLACK
        labelTextPaint.textAlign = Paint.Align.CENTER
        labelTextPaint.isSubpixelText = true

        gridPaint.color = Color.GRAY
        gridPaint.style = Paint.Style.STROKE

        fillPaint.style = Paint.Style.FILL

        val prefsManager = PreferenceManager.getDefaultSharedPreferences(this.context)
        fontSize = prefsManager.getInt("sudaku_font_size", 16)
        gridAlignment = prefsManager.getInt("sudaku_grid_alignment", 1)
        useCenterDetection = prefsManager.getBoolean("sudaku_use_center_detection", false)
        tolerantZones = if (prefsManager.getBoolean("sudaku_extra_tolerance", false))
            expandedTolerantZones
        else
            normalTolerantZones

        centerZones = when (prefsManager.getInt("sudaku_touch_zones_variant", 0)) {
            0 -> rectTouchZones
            1 -> rombTouchZones
            2 -> circleTouchZones
            else -> rectTouchZones
        }

        prefsManager.registerOnSharedPreferenceChangeListener(this)
    }

    fun setLayout(layout: Layout) {
        this.layout = layout
        invalidate()
    }

    fun setShiftState(shiftState: ShiftState) {
        this.shiftState = shiftState
        invalidate()
    }

    fun setEventListener(listener: IKeyboardEventListener) {
        this.eventListener = listener
    }

    fun enableCursorMode() {
        isCursorMode = true
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val specWidth = MeasureSpec.getSize(widthMeasureSpec)

        val scaledFontSize = fontSize * Resources.getSystem().displayMetrics.scaledDensity
        val scaledCellPadding = cellPadding * density

        labelTextPaint.textSize = scaledFontSize
        textOffset = (labelTextPaint.descent() - labelTextPaint.ascent()) / 2 - labelTextPaint.descent()

        cellSize = scaledCellPadding * 2 + scaledFontSize
        halfCellSize = cellSize / 2
        zoneSize = 3 * cellSize

        w = specWidth.toFloat()
        h = cellSize * 9

        gridRect.apply {
            set(0f, 0f, h, h)
            when (gridAlignment) {
                0 -> offsetTo(x, y + fatGridLine)
                1 -> offsetTo(x + (w - h) / 2, y + fatGridLine)
                2 -> offsetTo(x + (w - h), y + fatGridLine)
            }
        }

        setMeasuredDimension(specWidth, h.toInt())
    }

    private fun drawGrid(canvas: Canvas) {
        gridPaint.strokeWidth = fatGridLine * density
        canvas.drawRect(gridRect, gridPaint)

        for (i in arrayOf(3, 6)) {
            canvas.drawLine(gridRect.left, gridRect.top + cellSize * i, gridRect.right, gridRect.top + cellSize * i, gridPaint)
            canvas.drawLine(gridRect.left + cellSize * i, gridRect.top, gridRect.left + cellSize * i, gridRect.bottom, gridPaint)
        }
    }

    private fun drawLabels(canvas: Canvas) {
        gridPaint.strokeWidth = normalGridLine * density
        labelTextPaint.color = if (startZone != null) Color.LTGRAY else Color.BLACK
        labelTextPaint.isFakeBoldText = false

        layout.getKeys(shiftState.value).forEachIndexed { zoneIndex, arrayOfKeys ->
            gridRect.apply {
                canvas.drawLine(
                    left,
                    top + cellSize * zoneIndex,
                    right,
                    top + cellSize * zoneIndex,
                    gridPaint
                )
                canvas.drawLine(
                    left + cellSize * zoneIndex,
                    top,
                    left + cellSize * zoneIndex,
                    bottom,
                    gridPaint
                )
            }

            val zIndex = zoneIndex + 1
            val (zx, zy) = splitCoords3(zIndex)
            arrayOfKeys.forEachIndexed { keyIndex, key ->
                if (key != null) {
                    val i = keyIndex + 1
                    val (x, y) = splitCoords3(i)
                    canvas.drawText(key.label,
                        gridRect.left + zoneSize * zx + cellSize * x + halfCellSize,
                        gridRect.top + zoneSize * zy + cellSize * y + halfCellSize + textOffset,
                        labelTextPaint)
                }
            }
        }
        if (startZone != null) {
            labelTextPaint.color = Color.BLACK
            labelTextPaint.isFakeBoldText = true

            layout.getKeys(startZone!!, shiftState.value).forEachIndexed { index, key ->
                key?.let {
                    val (x, y) = splitCoords3(index + 1)
                    canvas.drawText(key.label,
                        gridRect.left + zoneSize * x + cellSize * 1.5f,
                        gridRect.top + zoneSize * y + cellSize * 1.5f + textOffset,
                        labelTextPaint)
                }
            }
        }
    }

    private fun drawBg(canvas: Canvas) {
        fillPaint.color = Color.GRAY
        canvas.drawPaint(fillPaint)

        fillPaint.color = Color.WHITE
        canvas.drawRect(gridRect, fillPaint)
    }

    private fun drawZones(canvas: Canvas) {
        if (useCenterDetection) {
            fillPaint.color = Color.LTGRAY
            centerZones.forEach {
                val (x, y) = splitCoords9(it)
                canvas.drawRect(
                    gridRect.left + x * cellSize,
                    gridRect.top + y * cellSize,
                    gridRect.left + (x+1) * cellSize,
                    gridRect.top + (y+1) * cellSize,
                    fillPaint
                )
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            drawBg(it)
            drawZones(it)
            drawGrid(it)
            if (!isCursorMode) drawLabels(it)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean = when(event!!.action) {
        MotionEvent.ACTION_DOWN -> touchStart(event)
        MotionEvent.ACTION_MOVE -> touchMove(event)
        MotionEvent.ACTION_UP -> touchEnd(event)
        else -> true
    }

    private fun touchStart(event: MotionEvent): Boolean {
        gridRect.getZone(event.x, event.y)?.let { zone ->
            isStartFromCenter = zone == 4
            if (!isStartFromCenter && startZone == null) {
                startZone = zone
                invalidate()
            }

            currentTouchX = event.x
        }

        return true
    }

    private fun touchMove(event: MotionEvent): Boolean {
        if (isCursorMode)  {
            currentTouchX?.let {
                val dx = it - event.x
                if (abs(dx) > cursorThreshold) {
                    eventListener.onInput(Action.Cursor((dx / abs(dx)).toInt()))
                }
            }

        } else {

            gridRect.getZone(event.x, event.y, true)?.let { zone ->
                if (startZone != null && currentZone != null && currentZone != 4 && zone == 4) {
                    layout.getKey(startZone!!, currentZone!!, shiftState.value)?.let {
                        eventListener.onInput(it.action)
                    }
                    startZone = null
                    isStartFromCenter = true
                    invalidate()

                } else if (startZone == null && zone != 4) {
                    startZone = zone
                    invalidate()
                }

                currentZone = zone
            }
        }
        currentTouchX = event.x

        return true
    }

    private fun touchEnd(event: MotionEvent): Boolean {
        if (isCursorMode) {
            isCursorMode = false
            cleanUp()

        } else {

            gridRect.getZone(event.x, event.y)?.let { zone ->
                touchUps++

                if ((startZone != null && (startZone != zone || isStartFromCenter) && touchUps == 1) || zone == 4) {
                    cleanUp()
                } else if (startZone != null && touchUps == 2) {
                    layout.getKey(startZone!!, zone, shiftState.value)?.let {
                        eventListener.onInput(it.action)
                    }
                    cleanUp()
                }

                currentZone = null
                currentTouchX = null
            }
        }

        return true
    }

    private fun cleanUp() {
        startZone = null
        currentZone = null
        currentTouchX = null
        touchUps = 0
        isStartFromCenter = false
        invalidate()
    }

    private fun splitCoords3(index: Int): Point {
        val mod = index % 3
        val x = if (mod == 0) 3 else mod
        val y = (index - 1) / 3
        return Point(x-1, y)
    }

    private fun splitCoords9(index: Int): Point {
        if (index == 36) return Point(0, 4)
        val x = index % 9
        val y = (index - 1) / 9
        return Point(x, y)
    }

    private fun RectF.getZone(x: Float, y: Float, isMoving: Boolean = false): Int? {
        if (x !in left..right || y !in top..bottom) return null

        val localX = x - this.left
        val localY = y - this.top

        val z3 = (localX / zoneSize).toInt() + (3 * (localY / zoneSize).toInt())
        val z9 = (localX / cellSize).toInt() + (9 * (localY / cellSize).toInt())

        return if (!isMoving) z3
               else if (useCenterDetection && z9 in centerZones) z3
               else if (useCenterDetection || (z9 in tolerantZones)) currentZone ?: startZone
               else z3
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        sharedPreferences?.let {
            fontSize = it.getInt("sudaku_font_size", 16)
            gridAlignment = it.getInt("sudaku_grid_alignment", 1)
            useCenterDetection = it.getBoolean("sudaku_use_center_detection", false)
            tolerantZones = if (it.getBoolean("sudaku_extra_tolerance", false))
                expandedTolerantZones
            else
                normalTolerantZones

            centerZones = when (it.getInt("sudaku_touch_zones_variant", 0)) {
                0 -> rectTouchZones
                1 -> rombTouchZones
                2 -> circleTouchZones
                else -> rectTouchZones
            }

            requestLayout()
            invalidate()
        }
    }
}

private data class Point(val x: Int, val y: Int)

private val normalTolerantZones = arrayOf(21, 23, 29, 33, 47, 51, 57, 59)
private val expandedTolerantZones = arrayOf(12, 14, 28, 34, 46, 52, 66, 68, 21, 23, 29, 33, 47, 51, 57, 59)
private val rectTouchZones = arrayOf(10, 13, 16, 37, 40, 43, 64, 67, 70)
private val rombTouchZones = arrayOf(20, 4, 24, 36, 40, 44, 56, 76, 60)
private val circleTouchZones = arrayOf(20, 13, 24, 37, 40, 43, 56, 67, 60)