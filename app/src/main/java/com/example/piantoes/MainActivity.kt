package com.example.piantoes

import android.app.ActionBar
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
		val myCanvasView = PianoView(this)
		setContentView(myCanvasView)
	}
}