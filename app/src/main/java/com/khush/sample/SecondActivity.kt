package com.khush.sample

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.khush.sample.tracking.MainTracker

class SecondActivity : AppCompatActivity() {

    private lateinit var btThirdAct: Button
    private lateinit var btGoBack: Button
    private lateinit var mainTracker: MainTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        mainTracker = MainTracker(this)
        mainTracker.screenOpen("SecondActivity")
        initViews()
        initClickListeners()
    }

    private fun initViews() {
        btThirdAct = findViewById(R.id.bt_third_act)
        btGoBack = findViewById(R.id.bt_go_back)
    }

    private fun initClickListeners() {
        btThirdAct.setOnClickListener {
            Intent(this, ThirdActivity::class.java).apply {
                startActivity(this)
            }
        }

        btGoBack.setOnClickListener {
            mainTracker.goBackClicked("SecondActivity")
            onBackPressedDispatcher.onBackPressed()
        }
    }
}