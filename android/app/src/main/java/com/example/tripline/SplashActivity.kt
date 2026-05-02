package com.example.tripline

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        findViewById<View?>(R.id.buttonSplashLogin)?.setOnClickListener {
            startActivity(TriplineScreenActivity.intent(this, TriplineScreenActivity.Screen.LOGIN))
        }

        findViewById<View?>(R.id.buttonSplashLocker)?.setOnClickListener {
            startActivity(MainActivity.intentForTab(this, R.id.fragment_locker))
            finish()
        }

        findViewById<View?>(R.id.buttonSplashSchedule)?.setOnClickListener {
            startActivity(MainActivity.intentForTab(this, R.id.fragment_schedule))
            finish()
        }
    }
}
