package com.froglife

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class TestActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val textView = TextView(this)
        textView.text = "🐸 Frog Life Works!\n\nIf you see this, the app is installed correctly."
        textView.textSize = 24f
        textView.setPadding(50, 50, 50, 50)

        setContentView(textView)
    }
}
