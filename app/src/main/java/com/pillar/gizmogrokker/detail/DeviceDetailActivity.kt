package com.pillar.gizmogrokker.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pillar.gizmogrokker.R

class DeviceDetailActivity : AppCompatActivity() {
    private val device get() = intent?.extras?.getSerializable("device")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.device_detail_activity)

        supportFragmentManager.beginTransaction()
            .replace(R.id.container, detailFragment())
            .commitNow()
    }

    private fun detailFragment(): DeviceDetailFragment = DeviceDetailFragment()
        .apply {
            arguments = Bundle().apply { putSerializable("device", device) }
        }
}