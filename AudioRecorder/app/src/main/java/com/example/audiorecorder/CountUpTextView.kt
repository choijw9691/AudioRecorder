package com.example.audiorecorder

import android.content.Context
import android.os.SystemClock
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView

class CountUpTextView(context: Context, attrs: AttributeSet? = null) :
    AppCompatTextView(context, attrs) {

    private var startTimeStamp: Long = 0L
    private val countUpAction: Runnable = object : Runnable {
        override fun run() {

            val currentTimeStamp = SystemClock.elapsedRealtime()
            val countTimeSeconds = ((currentTimeStamp - startTimeStamp) / 1000L).toInt()
            updateCountTime(countTimeSeconds)

            handler?.postDelayed(this, 1000L)
        }

    }

    fun startCountUp() {
        startTimeStamp = SystemClock.elapsedRealtime()

        Log.d("startCountUp", "startCountUp")
        handler?.post(countUpAction)
    }

    fun stopCountUp() {
        handler?.removeCallbacks(countUpAction)
    }

    fun updateCountTime(countTimeSeconds: Int) {
        val m = countTimeSeconds / 60
        val s = countTimeSeconds % 60

        text = "%02d:%02d".format(m, s)

    }


    fun clearCountTime() {

        updateCountTime(0)
    }

}