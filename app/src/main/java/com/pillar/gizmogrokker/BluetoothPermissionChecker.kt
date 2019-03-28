package com.pillar.gizmogrokker

import android.Manifest

class BluetoothPermissionChecker(
    val bluetoothInterface: BluetoothInterface,
    val permissionProxy: PermissionProxy
) {
    fun checkBluetoothState() = when {
        hasBluetoothSupport() -> checkPermission()
        else -> GrokkerBluetoothState.NoBlooth
    }

    private fun hasBluetoothSupport() = bluetoothInterface.hasBluetoothSupport

    private fun checkPermission() = if (hasAccessFineLocation()) {
        GrokkerBluetoothState.Ready
    } else {
        GrokkerBluetoothState.MustRequest
    }

    private fun hasAccessFineLocation() =
        permissionProxy.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
        permissionProxy.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
}

