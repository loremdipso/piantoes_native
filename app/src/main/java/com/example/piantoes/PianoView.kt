package com.example.piantoes

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.caverock.androidsvg.SVG
import java.lang.Math.*
import kotlin.math.pow
import kotlin.math.roundToInt


private const val STROKE_WIDTH = 12f
private const val LINE_WIDTH = 9f
private const val TEXT_SIZE = 150F
private const val MIDDLE_C = 39

class Drawer {
	var black: Int;
	var white: Int;
	var red: Int;
	var blue: Int;
	var yellow: Int;
	var background: Int;
	var treble_clef: SVG;
	var base_clef: SVG;
	var quarter_note: SVG;
	var sharp: SVG;

	constructor(resources: Resources) {
		black = ResourcesCompat.getColor(resources, R.color.black, null)
		white = ResourcesCompat.getColor(resources, R.color.white, null)
		red = ResourcesCompat.getColor(resources, R.color.red, null)
		blue = ResourcesCompat.getColor(resources, R.color.blue, null)
		yellow = ResourcesCompat.getColor(resources, R.color.yellow, null)
		background = ResourcesCompat.getColor(resources, R.color.background, null)
		treble_clef = SVG.getFromResource(resources, R.raw.treble_clef)
		base_clef = SVG.getFromResource(resources, R.raw.base_clef)
		quarter_note = SVG.getFromResource(resources, R.raw.quarter_note)
		sharp = SVG.getFromResource(resources, R.raw.sharp)
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

	fun drawSVG(canvas: Canvas, rect: Rect, svg: SVG) {
		svg.documentHeight = (rect.bottom - rect.top).toFloat();
		svg.documentWidth = (rect.right - rect.left).toFloat();
		val bitmap = Bitmap.createBitmap(rect.right - rect.left, rect.bottom - rect.top, Bitmap.Config.ARGB_8888)
		val scratchCanvas = Canvas(bitmap)
		svg.renderToCanvas(scratchCanvas)
		canvas.drawBitmap(bitmap, null, rect, null)
		// TODO: is this a memory leak? Or crazy inefficient?
//		val bitmap = Bitmap.createBitmap(svg.documentWidth.toInt(), svg.documentWidth.toInt(), Bitmap.Config.ARGB_8888)
//		canvas.drawBitmap(bitmap, null, rect, null)
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

	fun drawLine(canvas: Canvas, startX: Int, startY: Int, stopX: Int, stopY: Int) {
		canvas.drawLine(startX.toFloat(), startY.toFloat(), stopX.toFloat(), stopY.toFloat(), getStroke(black).apply { strokeWidth = LINE_WIDTH })
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

	fun drawNote(canvas: Canvas, centerXIn: Int, centerYIn: Int, isSharp: Boolean, shouldDrawLine: Boolean) {
		val noteWidth = 80
		val noteHeight = noteWidth * 2
		var centerX = centerXIn
		var centerY = centerYIn - (noteHeight * 0.35).toInt()
		drawSVG(canvas, Rect(centerX - noteWidth / 2, centerY - noteHeight / 2, centerX + noteWidth / 2, centerY + noteHeight / 2), quarter_note)

		if (shouldDrawLine) {
			var lineWidth = (noteWidth * 1.5).toInt()
			var lineMarginY = noteHeight * -0.13
			var lineY = (centerY + noteHeight / 2 + lineMarginY).toInt()
			drawLine(canvas, centerX - lineWidth / 2, lineY, centerX + lineWidth / 2, lineY)
		}

		if (isSharp) {
			val sharpWidth = (noteHeight * 0.625).toInt()
			centerX -= noteWidth
			centerY += (noteHeight * 0.35).toInt()
			drawSVG(canvas, Rect(centerX - sharpWidth / 2, centerY - sharpWidth / 2, centerX + sharpWidth / 2, centerY + sharpWidth / 2), sharp)
		}
	}
}

var keys = arrayOf("A", "A Sharp", "B", "C", "C Sharp", "D", "D Sharp", "E", "F", "F Sharp", "G", "G Sharp")
val numKeys = keys.size
fun getName(note: Int): String {
	return keys[note % keys.size]
}

fun isSharp(note: Int): Boolean {
	when (note % numKeys) {
		1, 4, 6, 9, 11 -> return true
		else -> return false
	}
}

class PianoView : View {
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
			drawText(canvas, Rect(0, height / 3 * index, width, height / 3 * ++index))
			drawSheetMusic(canvas, Rect(0, height / 3 * index, width, height / 3 * ++index))
			drawKeys(canvas, Rect(0, height / 3 * index, width, height / 3 * ++index))
		}
//        else {
// TODO: support rotation
//            drawKeys(canvas, Rect(0,0,width/3,height))
//        }
	}

	fun getRandomNote() {
		var range = 20
//		note = ((MIDDLE_C - range)..(MIDDLE_C + range)).random()
//		note = MIDDLE_C + 1 // let's assume this is middle C
		note = MIDDLE_C - 1
	}

	fun drawText(canvas: Canvas, rect: Rect) {
		drawer.drawRect(canvas, rect, drawer.blue)
		canvas.drawText(getName(note), rect.left + (rect.right - rect.left) / 2F, rect.top + (rect.bottom - rect.top) / 2F, drawer.getFill(drawer.white))
	}

	fun drawSheetMusic(canvas: Canvas, rect: Rect) {
		drawer.drawRect(canvas, rect, drawer.white)

		val svgWidth = 150
		val svgRect = Rect(rect.left, rect.top, rect.left + svgWidth, rect.bottom)

		val isBase = note < MIDDLE_C

		if (isBase) {
			drawer.drawSVG(canvas, svgRect, drawer.base_clef)
		} else {
			drawer.drawSVG(canvas, svgRect, drawer.treble_clef)
		}

		val yMargin = 150
		val xMargin = 0
		val myTop = rect.top + yMargin
		val myBottom = rect.bottom - yMargin
		val lineMargin = ((rect.bottom - rect.top) - (yMargin * 2)) / 4
		var startX = rect.left + xMargin
		var stopX = rect.right
		for (i in 0 until 5) {
			var y = myTop + lineMargin * i
			drawer.drawLine(canvas, startX, y, stopX, y)
		}

		// TDOO: make this more correct
		var myNote = note
		if (isSharp(note)) {
//			myNote -= 1
		}

		var relativeIndex = (myNote - MIDDLE_C)

		var shouldDrawLine = false
		var y = 0
		if (isBase) {
			var numSharps = getNumSharps(myNote + 1, MIDDLE_C)
			relativeIndex += numSharps
			var C_OFFSET = 2 // so we start at middle C
			relativeIndex += C_OFFSET
			y = myTop - (lineMargin / 2) * relativeIndex

			if (relativeIndex % 2 == 0 && relativeIndex > 1) {
				shouldDrawLine = true
			}
		} else {
			var numSharps = getNumSharps(myNote, MIDDLE_C)
			relativeIndex -= numSharps
			var C_OFFSET = -2 // so we start at middle C
			relativeIndex += C_OFFSET
			y = myBottom - (lineMargin / 2) * relativeIndex

			if (relativeIndex % 2 == 0 && ((relativeIndex - C_OFFSET) <= 0 || (relativeIndex - C_OFFSET) > numKeys)) {
				shouldDrawLine = true
			}
		}
		drawer.drawNote(canvas, (stopX - startX) / 2, y, isSharp(note), shouldDrawLine)
	}

	private fun getNumSharps(startIn: Int, endIn: Int): Int {
		var start = kotlin.math.min(startIn, endIn)
		var end = kotlin.math.max(startIn, endIn)
		var rv = 0
		for (n in start..end) {
			if (isSharp(n)) {
				rv++
			}
		}
		return rv
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
