package com.example.piantoes

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import androidx.core.content.res.ResourcesCompat
import com.caverock.androidsvg.SVG


private const val STROKE_WIDTH = 12f
private const val LINE_THICKNESS = 9f
private const val TEXT_SIZE = 150F

class Drawer {
	var black: Int;
	var white: Int;
	var red: Int;
	var blue: Int;
	var yellow: Int;
	var licorice: Int;
	var background: Int;
	var treble_clef: SVG;
	var base_clef: SVG;
	var quarter_note: SVG;
	var sharp: SVG;
	var muted: SVG;
	var unmuted: SVG;

	constructor(resources: Resources) {
		black = ResourcesCompat.getColor(resources, R.color.black, null)
		white = ResourcesCompat.getColor(resources, R.color.white, null)
		red = ResourcesCompat.getColor(resources, R.color.red, null)
		blue = ResourcesCompat.getColor(resources, R.color.blue, null)
		yellow = ResourcesCompat.getColor(resources, R.color.yellow, null)
		licorice = ResourcesCompat.getColor(resources, R.color.licorice, null)
		background = ResourcesCompat.getColor(resources, R.color.background, null)
		treble_clef = SVG.getFromResource(resources, R.raw.treble_clef)
		base_clef = SVG.getFromResource(resources, R.raw.base_clef)
		quarter_note = SVG.getFromResource(resources, R.raw.quarter_note)
		sharp = SVG.getFromResource(resources, R.raw.sharp)
		muted = SVG.getFromResource(resources, R.raw.muted)
		unmuted = SVG.getFromResource(resources, R.raw.unmuted)
	}

	fun getFill(colorVal: Int): Paint {
		return Paint().apply {
			color = colorVal
			isAntiAlias = true
			isDither = true
			textAlign = Paint.Align.CENTER
			textSize = TEXT_SIZE
		}
	}

	fun drawSVG(canvas: Canvas, rect: Rect, svg: SVG) {
		svg.documentHeight = rect.height().toFloat();
		svg.documentWidth = rect.width().toFloat();
		val bitmap = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888)
		val scratchCanvas = Canvas(bitmap)
		svg.renderToCanvas(scratchCanvas)
		canvas.drawBitmap(bitmap, null, rect, null)
		// TODO: is this a memory leak? Or crazy inefficient?
	}

	fun getStroke(inColor: Int): Paint {
		return Paint().apply {
			color = inColor
			isAntiAlias = true
			isDither = true
			style = Paint.Style.STROKE // default: FILL
			strokeWidth = STROKE_WIDTH // default: Hairline-width (really thin)
			textAlign = Paint.Align.CENTER
			textSize = TEXT_SIZE
		}
	}

	fun drawLine(canvas: Canvas, startX: Int, startY: Int, stopX: Int, stopY: Int) {
		canvas.drawLine(startX.toFloat(), startY.toFloat(), stopX.toFloat(), stopY.toFloat(), getStroke(black).apply { strokeWidth = LINE_THICKNESS })
	}

	fun drawRect(canvas: Canvas, rect: Rect, fillColor: Int, strokeColor: Int? = null): Rect {
		canvas.drawRect(rect, getFill(fillColor))
		if (strokeColor != null) {
			canvas.drawRect(rect, getStroke(strokeColor))
		}
		return rect
	}

	fun drawRect(canvas: Canvas, left: Int, top: Int, width: Int, height: Int, fillColor: Int, strokeColor: Int? = null): Rect {
		val rect = Rect(left, top, left + width, top + height)
		canvas.drawRect(rect, getFill(fillColor))
		if (strokeColor != null) {
			canvas.drawRect(rect, getStroke(strokeColor))
		}
		return rect
	}

	fun drawNote(canvas: Canvas, centerXIn: Int, centerYIn: Int, isSharp: Boolean, shouldDrawLine: Boolean, noteWidth: Int, offsetDelta: Int) {
		val noteHeight = ((noteWidth * quarter_note.documentHeight) / quarter_note.documentWidth).toInt()
		var centerX = centerXIn
		var centerY = centerYIn
		centerY -= (offsetDelta * LINE_THICKNESS * 0.9).toInt()

		val noteRect = Rect(
				centerX - noteWidth / 2,
				centerY - noteHeight / 2,
				centerX + noteWidth / 2,
				centerY + noteHeight / 2
		)

		drawSVG(canvas, noteRect, quarter_note)

		if (shouldDrawLine) {
			var lineWidth = (noteWidth * 1.5).toInt()
			var lineY = centerY
			drawLine(canvas, centerX - lineWidth / 2, lineY, centerX + lineWidth / 2, lineY)
		}

		if (isSharp) {
			val sharpWidth = (noteWidth * 0.75).toInt()
			centerX -= noteWidth
			drawSVG(canvas, Rect(centerX - sharpWidth / 2, centerY - sharpWidth / 2, centerX + sharpWidth / 2, centerY + sharpWidth / 2), sharp)
		}
	}
}
