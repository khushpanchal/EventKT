package com.khush.sample

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.khush.sample.tracking.MainTracker

class MainActivity : AppCompatActivity() {

    companion object {
        private val nameList = listOf(
            "John",
            "Sam",
            "Alice",
            "Bob",
            "Emily",
            "Jake",
            "Olivia",
            "Ethan",
            "Mia",
            "Liam",
            "Ava",
            "Noah",
            "Sophia",
            "Jackson",
            "Isabella",
            "Aiden",
            "Grace",
            "Lucas",
            "Lily",
            "Logan",
            "Emma",
            "Oliver",
            "Sophia",
            "Mason",
            "Harper",
            "Caleb",
            "Ella",
            "Benjamin",
            "Amelia",
            "Henry",
            "Scarlett",
            "Carter",
            "Chloe",
            "Samuel",
            "Avery",
            "Daniel",
            "Abigail",
            "Gabriel",
            "Addison",
            "Wyatt",
            "Zoey",
            "Owen",
            "Lily",
            "Isaac",
            "Aurora",
            "Alexander",
            "Hannah",
            "Leo",
            "Stella",
            "Jack"
        )
    }

    private lateinit var rv: RecyclerView
    private lateinit var btSecondAct: Button
    private lateinit var btThirdAct: Button
    private lateinit var mainAdapter: MainAdapter
    private lateinit var mainTracker: MainTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainTracker = MainTracker(this)
        mainTracker.screenOpen("MainActivity")
        initViews()
        initClickListeners()
        rv.apply {
            adapter = mainAdapter
            layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(
                DividerItemDecoration(
                    this@MainActivity,
                    DividerItemDecoration.VERTICAL
                )
            )
        }
    }

    private fun initViews() {
        rv = findViewById(R.id.rv)
        btSecondAct = findViewById(R.id.bt_second_act)
        btThirdAct = findViewById(R.id.bt_third_act)
        mainAdapter = MainAdapter(nameList)
    }

    private fun initClickListeners() {
        btSecondAct.setOnClickListener {
            Intent(this, SecondActivity::class.java).apply {
                startActivity(this)
            }
        }

        btThirdAct.setOnClickListener {
            Intent(this, ThirdActivity::class.java).apply {
                startActivity(this)
            }
        }

        mainAdapter.setOnItemClickListener { name, pos ->
            Toast.makeText(this@MainActivity, name, Toast.LENGTH_SHORT).show()
            mainTracker.rvItemClicked(name, pos)
        }
    }
}