package com.pillar.gizmogrokker.bluetoothclasses

import android.bluetooth.BluetoothClass

enum class BluetoothServiceClass(val value: Int) {
    Limited(0x00_2000),
    Positioning(0x01_0000),
    Networking(0x02_0000),
    Rendering(0x04_0000),
    Capture(0x08_0000),
    Transfer(0x10_0000),
    Audio(0x20_0000),
    Telephony(0x40_0000),
    Information(0x80_0000);

    companion object {
        fun getAvailableServices(btClass: BluetoothClass): List<BluetoothServiceClass> =
            BluetoothServiceClass.values().fold(mutableListOf()) { acc, serviceClass ->
                acc.apply {
                    if (btClass.hasService(serviceClass.value)) {
                        add(serviceClass)
                    }
                }
            }
    }
}