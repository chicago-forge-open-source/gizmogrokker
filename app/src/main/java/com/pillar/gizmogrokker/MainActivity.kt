package com.pillar.gizmogrokker

import android.Manifest
import android.app.PendingIntent.getActivity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


const val REQUEST_ENABLE_BT = 100
const val REQUEST_BLUETOOTH_PERMISSIONS = 1

class MainActivity : AppCompatActivity() {
    companion object {
        private val permissions = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
        private val defaultAdapter = BluetoothAdapter.getDefaultAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions()
        registerBroadCastReceiver()

        if (defaultAdapter != null) {
            defaultAdapter.enable()
            startDiscoveryMaybe()

        } else {
            log("No bluetooth on this device")
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

//    BT LOCATION PERMISSIONS
    private fun hasPermissions(): Boolean {
        return permissions.all {
            return ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            permissions,
            REQUEST_BLUETOOTH_PERMISSIONS
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_BLUETOOTH_PERMISSIONS -> {
                startDiscoveryMaybe()
            }
        }
    }

//    BROADCAST RECEIVER
    private fun registerBroadCastReceiver() {
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(receiver, filter)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    intent.bluetoothDevice()
                        .run {
                            log("device found $name ${BluetoothDeviceType.fromCode(type)} ${bluetoothClass.majorDeviceClass.toString(16)} $address")
                        }
                }
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    startDiscoveryMaybe()
                }
            }
        }

        private fun Intent.bluetoothDevice(): BluetoothDevice = getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
    }

    private fun startDiscoveryMaybe() {
        if(defaultAdapter.isEnabled && hasPermissions()) {
            defaultAdapter.startDiscovery()
                .let { started ->
                    log("Discovery... $started")
                }
        } else {
            log("Missing permissions or bluetooth")
        }
    }

    private fun log(message: String) = Log.d("MURDOCK", message)
}

enum class BluetoothDeviceType(val code: Int) {
    UNKNOWN(0), CLASSIC(1), LE(2), DUAL(3);

    companion object {
        fun fromCode(code: Int) = values().find { it.code == code }
    }
}