package com.pillar.gizmogrokker.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pillar.gizmogrokker.BloothDevice
import com.pillar.gizmogrokker.R

class DeviceDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.device_detail_activity)
        if (savedInstanceState == null) {
            val device : BloothDevice = intent?.extras?.getSerializable("device") as BloothDevice

            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.container,
                    DeviceDetailFragment.create(device)
                )
                .commitNow()
        }
    }

}