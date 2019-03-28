package com.pillar.gizmogrokker

import kotlinx.coroutines.CompletableDeferred

class BluetoothDiscotech(val bluetoothInterface: BluetoothInterface) {

    suspend fun discoverize() = beginDiscovery()
        .run { collectDevices() }

    private fun beginDiscovery() = if (bluetoothInterface.isEnabled) {
        bluetoothInterface.startDiscovery()
    } else {
        bluetoothInterface.enable()
        bluetoothInterface.registerEnabled(::onBluetoothEnabled)
    }

    private suspend fun collectDevices() = mutableListOf<BloothDevice>()
        .also { devices ->
            bluetoothInterface.registerDeviceDiscovered {
                println("device discovered")
                devices += it
            }
            waitForDiscoveryToFinish()
        }

    private suspend fun waitForDiscoveryToFinish() = CompletableDeferred<Unit>()
        .apply { bluetoothInterface.registerDiscoveryEnded { complete(Unit) } }
        .await()

    private fun onBluetoothEnabled() {
        bluetoothInterface.startDiscovery()
        bluetoothInterface.unregisterEnabled(::onBluetoothEnabled)
    }

}
