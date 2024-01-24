package com.khush.sample

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.khush.sample.tracking.MainTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ThirdActivity : AppCompatActivity() {

    private lateinit var btGoBack: Button
    private lateinit var mainTracker: MainTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)
        mainTracker = MainTracker(this)
        mainTracker.screenOpen("ThirdActivity")
        initViews()
        initClickListeners()
        networkCallSimulator()
    }

    private fun initViews() {
        btGoBack = findViewById(R.id.bt_go_back)
    }

    private fun initClickListeners() {
        btGoBack.setOnClickListener {
            mainTracker.goBackClicked("ThirdActivity")
            onBackPressedDispatcher.onBackPressed()
        }
    }

    /**
     * Test: Concurrent Modification
     */
    private fun networkCallSimulator() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                for (i in 1..5) {
                    mainTracker.networkCall("A${i}")
                }
            }
        }

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                for (i in 1..5) {
                    mainTracker.networkCall("B${i}")
                }
            }
        }

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                for (i in 1..5) {
                    mainTracker.networkCall("C${i}")
                }
            }
        }
    }

}