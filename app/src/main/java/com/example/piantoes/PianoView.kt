package com.example.piantoes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.core.content.res.ResourcesCompat

class PianoView(context: Context) : View(context) {
    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap
    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.colorBackground, null)


}
