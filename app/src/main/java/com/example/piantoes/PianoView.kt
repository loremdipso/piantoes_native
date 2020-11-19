package com.example.piantoes

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Point
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnTouchListener

private const val MIDDLE_C = 39
private const val C_RANGE = 20

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

fun isInBase(note: Int): Boolean {
	return note < MIDDLE_C
}

class PianoView : View, View.OnClickListener, View.OnTouchListener {
	private lateinit var extraCanvas: Canvas
	private lateinit var extraBitmap: Bitmap
	private val drawer = Drawer(resources)
	private var note: Int = MIDDLE_C
	private var showAll = false

	constructor(context: Context) : super(context) {
		getRandomNote()
		this.setOnTouchListener(this);
		this.setOnClickListener(this);
	}

	override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
		super.onSizeChanged(width, height, oldWidth, oldHeight)
		if (::extraBitmap.isInitialized) extraBitmap.recycle()
		extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
		extraCanvas = Canvas(extraBitmap)
		extraCanvas.drawColor(drawer.background)
	}

	private var lastTouchDownXY: Point = Point(0, 0)

	override fun onTouch(v: View?, event: MotionEvent?): Boolean {
		// save the X,Y coordinates
		if (event?.actionMasked == MotionEvent.ACTION_DOWN) {
			lastTouchDownXY.x = event?.x.toInt()
			lastTouchDownXY.y = event?.y.toInt()
		}

		// let the touch event pass on to whoever needs it
		return false
	}

	override fun onClick(v: View?) {
		val x = lastTouchDownXY.x
		val y = lastTouchDownXY.y

		// top section: change note randomly
		if (textRect.contains(x, y)) {
			getRandomNote()
		} else if (sheetMusicRect.contains(x, y)) {
			showAll = !showAll
		} else { // bottom section: try to guess the key
			guessKey(x, y)
		}

		invalidate() // force redraw
	}


	private var textRect = Rect(0, 0, 0, 0)
	private var sheetMusicRect = Rect(0, 0, 0, 0)
	private var keysRect = Rect(0, 0, 0, 0)

	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)
		canvas.drawBitmap(extraBitmap, 0f, 0f, null)

		// TODO: support screen rotation
		if (height > width) {
			var index = 0
			var sectionHeight = height / 6

			var numSections = 1
			textRect = Rect(0, sectionHeight * index, width, sectionHeight * (index + numSections))
			index += numSections

			numSections = 3
			sheetMusicRect = Rect(0, sectionHeight * index, width, sectionHeight * (index + numSections))
			index += numSections

			numSections = 2
			keysRect = Rect(0, sectionHeight * index, width, sectionHeight * (index + numSections))

			drawText(canvas, textRect)

//			if (showAll) {
			drawSheetMusic(canvas, sheetMusicRect)
//			}
			
			drawKeys(canvas, keysRect)
		}
	}

	fun getRandomNote() {
		val wasInBase = isInBase(note)

		// make sure this next note isn't the same kind as the old one
		val oldNote = note % numKeys
		while (note % numKeys == oldNote) {
			note = ((MIDDLE_C - C_RANGE)..(MIDDLE_C + C_RANGE)).random()
		}

		// if we were in base, stay there, and vice versa
		if (wasInBase != isInBase(note)) {
			swapClefs()
		}
	}

	fun drawText(canvas: Canvas, rect: Rect) {
		drawer.drawRect(canvas, rect, drawer.blue)
		canvas.drawText(getName(note), rect.left + (rect.right - rect.left) / 2F, rect.top + (rect.bottom - rect.top) / 2F, drawer.getFill(drawer.white))
	}

	fun drawSheetMusic(canvas: Canvas, rect: Rect) {
		drawer.drawRect(canvas, rect, drawer.white)

		val svgWidth = 150
		val svgRect = Rect(rect.left, rect.top, rect.left + svgWidth, rect.bottom)

		val isBase = isInBase(note)

		if (isBase) {
			drawer.drawSVG(canvas, svgRect, drawer.base_clef)
		} else {
			drawer.drawSVG(canvas, svgRect, drawer.treble_clef)
		}

		val yMargin = rect.height() / 3

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

		var notes: MutableList<Int> = ArrayList()
		if (showAll) {
			var temp = (note % numKeys)
			val wasBase = isInBase(note)
			var min = MIDDLE_C - C_RANGE
			var max = MIDDLE_C + C_RANGE
			while (temp <= max) {
				if (temp >= min && wasBase == isInBase(temp)) {
					notes.add(temp)
				} else if (temp == MIDDLE_C) {
					// SPECIAL CASE: add middle C in either case
					notes.add(temp)
				}
				temp += numKeys
			}
		} else {
			notes.add(note)
		}

		for (tempNote in notes) {
			drawNote(tempNote, isBase, myTop, lineMargin, myBottom, canvas, stopX, startX)
		}
	}

	private fun drawNote(myNote: Int, isBase: Boolean, myTop: Int, lineMargin: Int, myBottom: Int, canvas: Canvas, stopX: Int, startX: Int) {
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

	// TODO: we shouldn't need two functions for this
	private fun getNumSharpsStrict(start: Int, end: Int): Int {
		var rv = 0
		for (n in start..end) {
			if (isSharp(n)) {
				rv++
			}
		}
		return rv
	}


	// using a linked hashmap to preserve insertion order
	var rectsToKeys: LinkedHashMap<Rect, Int> = LinkedHashMap(12)
	fun drawKeys(canvas: Canvas, rect: Rect) {
		drawer.drawRect(canvas, rect, drawer.blue)

		var noteWidth = rect.width() / 7
		var noteHeight = rect.height()
		var blackNoteHeight = rect.bottom - rect.top

//		var noteOffset = note - numKeys / 2
		var noteOffset = 3 // get a good starting place

		var targetNode = note % numKeys

		rectsToKeys.clear()

		var didIt = false
		// TODO: deduplicate
		// white keys
		for (i in (0..numKeys)) {
			var tempNote = i + noteOffset
			var tempI = i - getNumSharpsStrict(noteOffset, tempNote - 1)
			if (!isSharp(tempNote)) {
				var foreground = drawer.white
				var background = drawer.black
				if (targetNode == tempNote % numKeys) {
					foreground = drawer.red
				}
				val rect = drawer.drawRect(canvas, noteWidth * tempI, rect.top, noteWidth, noteHeight, foreground, background)
				rectsToKeys[rect] = MIDDLE_C + i
			}
		}

		for (i in (0..numKeys)) {
			var tempNote = i + noteOffset
			var tempI = i - getNumSharpsStrict(noteOffset, tempNote - 1)
			if (isSharp(tempNote)) {
				var foreground = drawer.black
				var background = drawer.black
				if (targetNode == tempNote % numKeys) {
					foreground = drawer.red
				}
				val rect = drawer.drawRect(canvas, noteWidth * tempI - noteWidth / 4, rect.top, noteWidth / 2, noteHeight / 2, foreground, background)
				rectsToKeys[rect] = MIDDLE_C + i
			}
		}
	}

	fun guessKey(x: Int, y: Int) {
		for ((rect, tempNote) in rectsToKeys.asIterable().reversed()) {
			if (rect.contains(x, y)) {
				if (note % numKeys != tempNote % numKeys) {
					val wasBase = isInBase(note)
					note = tempNote
					if (wasBase != isInBase(note)) {
						swapClefs()
					}
				} else {
					swapClefs()
				}
				return
			}
		}
	}

	fun swapClefs() {
		if (note < MIDDLE_C) {
			while (note < MIDDLE_C) {
				note += numKeys
			}
		} else {
			while (note >= MIDDLE_C) {
				note -= numKeys
			}
		}
	}
}
