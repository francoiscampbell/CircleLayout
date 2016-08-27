package io.github.francoiscampbell.testapp

import android.animation.ObjectAnimator
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ObjectAnimator.ofInt(circleLayout, "radius", 0, 500)
                .setDuration(300)
                .apply {
                    repeatMode = ObjectAnimator.REVERSE
                    repeatCount = ObjectAnimator.INFINITE
                    addUpdateListener { Log.i(TAG, "animatedValue: ${it.animatedValue}"); }
                }
                .start()
    }
}
