package com.pillar.gizmogrokker

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.pillar.gizmogrokker.bluetoothclasses.BluetoothMajorClass
import com.pillar.gizmogrokker.bluetoothclasses.BluetoothMinorClass
import com.pillar.gizmogrokker.bluetoothclasses.BluetoothServiceClass

typealias DiscoveryEndedCallback = (Unit) -> Unit

interface BluetoothInterface {

    val adapter: BluetoothAdapter?
    val context: Context
    val bluetoothEnabled: Event<Unit>
    val discoveryEnded: Event<Unit>
    val deviceDiscovered: Event<BloothDevice>

    val hasBluetoothSupport: Boolean get() = adapter != null
    val isEnabled: Boolean get() = adapter?.isEnabled ?: false

    fun enable() {
        adapter?.enable()
    }

    fun startDiscovery() {
        if (adapter?.isDiscovering == true) {
            adapter?.cancelDiscovery()
        }

        adapter?.startDiscovery()

        context.registerReceiver(
            Receiver(
                bluetoothEnabled,
                discoveryEnded,
                deviceDiscovered
            ),
            Receiver.intentFilter()
        )
    }
}

private class Receiver(
    private val bluetoothEnabled: Event<Unit>,
    private val discoveryEnded: Event<Unit>,
    private val deviceDiscovered: Event<BloothDevice>
) : BroadcastReceiver() {

    companion object {
        fun intentFilter() = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
            .apply { addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED) }
            .apply { addAction(BluetoothDevice.ACTION_FOUND) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            BluetoothAdapter.ACTION_STATE_CHANGED -> handleStateChanged()
            BluetoothDevice.ACTION_FOUND -> intent.handleFound()
            BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> context.handleDiscoveryFinished()
        }
    }

    private fun Context.handleDiscoveryFinished() = unregisterReceiver(this@Receiver)
        .also { discoveryEnded.eventOccurred(Unit) }

    private fun handleStateChanged() {
        bluetoothEnabled.eventOccurred(Unit)
    }

    private fun Intent.handleFound() {
        bluetoothDevice()
            .bloothDevice()
            .let { deviceDiscovered.eventOccurred(it) }
    }

            private fun BluetoothDevice.bloothDevice() = BloothDevice(
                name = name,
                macAddress = address,
                type = DeviceType.fromInt(type),
                majorClass = BluetoothMajorClass.fromInt(bluetoothClass.majorDeviceClass),
                minorClass = BluetoothMinorClass.fromInt(bluetoothClass.deviceClass),
                services = BluetoothServiceClass.getAvailableServices(bluetoothClass)
            )

    private fun Intent.bluetoothDevice(): BluetoothDevice =
        getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

}

typealias Listener<P> = (P) -> Unit

interface Event<P> {
    operator fun plus(listener: Listener<P>)
    operator fun minus(listener: Listener<P>)
    fun eventOccurred(input: P)
}

class DataEvent<P> : Event<P> {

    private var listenerList = listOf<Listener<P>>()

    override fun plus(listener: Listener<P>) {
        listenerList = listenerList + listener
    }

    override fun minus(listener: Listener<P>) {
        listenerList = listenerList - listener
    }

    override fun eventOccurred(input: P) {
        listenerList.toList().forEach { it.invoke(input) }
    }

}
