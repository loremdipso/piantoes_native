package com.example.piantoes

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.core.content.res.ResourcesCompat


private const val STROKE_WIDTH = 12f // has to be float

class Drawer {
    var black: Int = -1;
    var white: Int = -1;

    constructor(resources: Resources) {
        black = ResourcesCompat.getColor(resources, R.color.black, null)
        white = ResourcesCompat.getColor(resources, R.color.white, null)
    }

    private fun getFill(colorVal: Int): Paint {
        return Paint().apply {
            color = colorVal
            // Smooths out edges of what is drawn without affecting shape.
            isAntiAlias = true
            // Dithering affects how colors with higher-precision than the device are down-sampled.
            isDither = true
//        style = Paint.Style.STROKE // default: FILL
//        strokeJoin = Paint.Join.ROUND // default: MITER
//        strokeCap = Paint.Cap.ROUND // default: BUTT
//        strokeWidth = STROKE_WIDTH // default: Hairline-width (really thin)
        }
    }

    private fun getStroke(inColor: Int): Paint {
        return Paint().apply {
            color = inColor
            // Smooths out edges of what is drawn without affecting shape.
            isAntiAlias = true
            // Dithering affects how colors with higher-precision than the device are down-sampled.
            isDither = true
            style = Paint.Style.STROKE // default: FILL
//        strokeJoin = Paint.Join.ROUND // default: MITER
//        strokeCap = Paint.Cap.ROUND // default: BUTT
            strokeWidth = STROKE_WIDTH // default: Hairline-width (really thin)
        }
    }

    fun drawRect(canvas: Canvas, left: Int, top: Int, width: Int, height: Int, fillColor: Int, strokeColor: Int? = null) {
        canvas.drawRect(Rect(left, top, left + width, top + height), getFill(fillColor))
        if (strokeColor != null) {
            canvas.drawRect(Rect(left, top, left + width, top + height), getStroke(strokeColor))
        }
    }

    fun drawRectOutline(canvas: Canvas, left: Int, top: Int, width: Int, height: Int, color: Int) {
        canvas.drawRect(Rect(left, top, left + width, top + height), getStroke(color))
    }
}

class PianoView(context: Context) : View(context) {
    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap
    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.colorBackground, null)
    private val drawer = Drawer(resources)

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        if (::extraBitmap.isInitialized) extraBitmap.recycle()
        extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
        extraCanvas.drawColor(backgroundColor)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(extraBitmap, 0f, 0f, null)

        drawer.drawRect(canvas, 50, 50, 50, 50, drawer.white)
    }
}
