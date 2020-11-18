package com.example.piantoes

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.view.View
import androidx.core.content.res.ResourcesCompat


private const val STROKE_WIDTH = 12f
private const val TEXT_SIZE = 150F

class Drawer {
    var black: Int = -1;
    var white: Int = -1;
    var red: Int = -1;
    var blue: Int = -1;
    var yellow: Int = -1;
    var background: Int = -1;

    constructor(resources: Resources) {
        black = ResourcesCompat.getColor(resources, R.color.black, null)
        white = ResourcesCompat.getColor(resources, R.color.white, null)
        red = ResourcesCompat.getColor(resources, R.color.red, null)
        blue = ResourcesCompat.getColor(resources, R.color.blue, null)
        yellow = ResourcesCompat.getColor(resources, R.color.yellow, null)
        background = ResourcesCompat.getColor(resources, R.color.background, null)
    }

    fun getFill(colorVal: Int): Paint {
        return Paint().apply {
            color = colorVal
// Smooths out edges of what is drawn without affecting shape.
            isAntiAlias = true
// Dithering affects how colors with higher-precision than the device are down-sampled.
            isDither = true
			textAlign = Paint.Align.CENTER
            textSize = TEXT_SIZE
//        style = Paint.Style.STROKE // default: FILL
//        strokeJoin = Paint.Join.ROUND // default: MITER
//        strokeCap = Paint.Cap.ROUND // default: BUTT
//        strokeWidth = STROKE_WIDTH // default: Hairline-width (really thin)
        }
    }

    fun getStroke(inColor: Int): Paint {
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
            textAlign = Paint.Align.CENTER
            textSize = TEXT_SIZE
        }
    }

    fun drawRect(canvas: Canvas, rect: Rect, fillColor: Int, strokeColor: Int? = null) {
        canvas.drawRect(rect, getFill(fillColor))
        if (strokeColor != null) {
            canvas.drawRect(rect, getStroke(strokeColor))
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

var keys = arrayOf("A", "A Sharp", "B", "C", "C Sharp", "D", "D Sharp", "E", "F", "F Sharp", "G", "G Sharp")
val numKeys = keys.size
fun getName(note: Int): String {
    return keys[note % (numKeys + 1)]
}

fun isSharp(note: Int): Boolean {
    when (note % (numKeys + 1)) {
        1,4,6,9 -> return true
        else -> return false
    }
}

class PianoView: View {
    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap
    private val drawer = Drawer(resources)
    private var note: Int = 0

	constructor(context: Context) : super(context) {
        getRandomNote()
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        if (::extraBitmap.isInitialized) extraBitmap.recycle()
        extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
        extraCanvas.drawColor(drawer.background)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(extraBitmap, 0f, 0f, null)

        if (height > width) {
            var index = 0
            drawText(canvas, Rect(0,height/3 * index, width,height/3 * ++index))
            drawSheetMusic(canvas, Rect(0,height/3 * index, width,height/3 * ++index))
            drawKeys(canvas, Rect(0,height/3 * index, width,height/3 * ++index))
        }
//        else {
// TODO: support rotation
//            drawKeys(canvas, Rect(0,0,width/3,height))
//        }
    }

    fun getRandomNote() {
        note = (0..88).random()
    }

    fun drawText(canvas: Canvas, rect: Rect) {
        drawer.drawRect(canvas, rect, drawer.black)
        canvas.drawText(getName(note), rect.left + (rect.right - rect.left) / 2F, rect.top + (rect.bottom - rect.top) / 2F, drawer.getFill(drawer.white))
    }

    fun drawSheetMusic(canvas: Canvas, rect: Rect) {
        drawer.drawRect(canvas, rect, drawer.white)
    }

    fun drawKeys(canvas: Canvas, rect: Rect) {
        drawer.drawRect(canvas, rect, drawer.blue)

//        drawer.drawRect(canvas, rect.left, rect.top, 50, 50, drawer.white, drawer.black)
		if (isSharp(note)) {
            drawer.drawRect(canvas, rect.left + 60, rect.top, 50, 50, drawer.white, drawer.black)
        } else {
            drawer.drawRect(canvas, rect.left + 60, rect.top, 50, 50, drawer.white, drawer.white)
        }
    }
}
