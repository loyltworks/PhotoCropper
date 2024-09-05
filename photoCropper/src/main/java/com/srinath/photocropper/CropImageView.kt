package com.srinath.photocropper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.MotionEvent

class CropImageView(context: Context, attrs: AttributeSet) : androidx.appcompat.widget.AppCompatImageView(context, attrs) {

    private var paint: Paint = Paint()
    private var borderPaint: Paint = Paint()
    private var overlayPaint: Paint = Paint()

    private var startX: Float = 0f
    private var startY: Float = 0f
    private var endX: Float = 0f
    private var endY: Float = 0f

    private var isDragging: Boolean = false
    private var activeEdge: Edge? = null
    private var lastTouchX: Float = 0f
    private var lastTouchY: Float = 0f
    private var dragMode: Boolean = false

    private val touchRadius = 50f

    init {
        // Initialize the paints
        borderPaint.color = Color.WHITE
        borderPaint.strokeWidth = 5f
        borderPaint.style = Paint.Style.STROKE

        overlayPaint.color = Color.BLACK
        overlayPaint.alpha = 150 // Semi-transparent overlay

        paint.style = Paint.Style.FILL

        // Set default crop rectangle (e.g., center and 50% of the view size)
        post {
            startX = width * 0.25f
            startY = height * 0.25f
            endX = width * 0.75f
            endY = height * 0.75f
            invalidate()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                activeEdge = detectTouchedEdge(event.x, event.y)
                dragMode = activeEdge == null && isInsideCropArea(event.x, event.y)
                if (dragMode || activeEdge != null) {
                    lastTouchX = event.x
                    lastTouchY = event.y
                    isDragging = true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    val dx = event.x - lastTouchX
                    val dy = event.y - lastTouchY

                    if (dragMode) {
                        moveCropArea(dx, dy)
                    } else if (activeEdge != null) {
                        resizeCropArea(dx, dy)
                    }

                    lastTouchX = event.x
                    lastTouchY = event.y
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                isDragging = false
                dragMode = false
                activeEdge = null
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the dark overlay
        canvas.drawRect(0f, 0f, width.toFloat(), startY, overlayPaint)
        canvas.drawRect(0f, endY, width.toFloat(), height.toFloat(), overlayPaint)
        canvas.drawRect(0f, startY, startX, endY, overlayPaint)
        canvas.drawRect(endX, startY, width.toFloat(), endY, overlayPaint)

        // Draw the cropping rectangle border
        canvas.drawRect(startX, startY, endX, endY, borderPaint)
    }

    private fun detectTouchedEdge(x: Float, y: Float): Edge? {
        return when {
            Math.abs(x - startX) < touchRadius -> Edge.LEFT
            Math.abs(x - endX) < touchRadius -> Edge.RIGHT
            Math.abs(y - startY) < touchRadius -> Edge.TOP
            Math.abs(y - endY) < touchRadius -> Edge.BOTTOM
            else -> null
        }
    }

    private fun isInsideCropArea(x: Float, y: Float): Boolean {
        return x > startX && x < endX && y > startY && y < endY
    }

    private fun moveCropArea(dx: Float, dy: Float) {
        // Calculate the new positions
        val newStartX = startX + dx
        val newEndX = endX + dx
        val newStartY = startY + dy
        val newEndY = endY + dy

        // Ensure the crop area stays within the view bounds
        if (newStartX >= 0 && newEndX <= width && newStartY >= 0 && newEndY <= height) {
            startX = newStartX
            endX = newEndX
            startY = newStartY
            endY = newEndY
        }
    }

    private fun resizeCropArea(dx: Float, dy: Float) {
        when (activeEdge) {
            Edge.LEFT -> startX = (startX + dx).coerceAtMost(endX - touchRadius).coerceAtLeast(0f)
            Edge.TOP -> startY = (startY + dy).coerceAtMost(endY - touchRadius).coerceAtLeast(0f)
            Edge.RIGHT -> endX = (endX + dx).coerceAtLeast(startX + touchRadius).coerceAtMost(width.toFloat())
            Edge.BOTTOM -> endY = (endY + dy).coerceAtLeast(startY + touchRadius).coerceAtMost(height.toFloat())
            else -> {}
        }
    }

    fun getCroppedBitmap(): Bitmap? {
        if (drawable == null) return null

        val bitmap = (drawable as BitmapDrawable).bitmap
        val widthRatio = bitmap.width / width.toFloat()
        val heightRatio = bitmap.height / height.toFloat()

        val croppedWidth = (endX - startX) * widthRatio
        val croppedHeight = (endY - startY) * heightRatio

        val x = startX * widthRatio
        val y = startY * heightRatio

        return Bitmap.createBitmap(bitmap, x.toInt(), y.toInt(), croppedWidth.toInt(), croppedHeight.toInt())
    }

    private enum class Edge {
        LEFT, TOP, RIGHT, BOTTOM
    }
}



