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

abstract class BluetoothInterface {

    abstract val adapter: BluetoothAdapter?
    abstract val context: Context
    val bluetoothEnabled: Event<Unit> = DataEvent()
    open val discoveryEnded: Event<Unit> = DataEvent()
    open val deviceDiscovered: Event<BloothDevice> = DataEvent()

    val hasBluetoothSupport: Boolean get() = adapter != null
    open val isEnabled: Boolean get() = adapter?.isEnabled ?: false

    open fun enable() {
        adapter?.enable()
    }

    open fun startDiscovery() {
        if (adapter?.isDiscovering == true) {
            adapter?.cancelDiscovery()
        }

        adapter?.startDiscovery()

        context.registerReceiver(
            Receiver(bluetoothEnabled, discoveryEnded, deviceDiscovered),
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
                .apply {
                    addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                    addAction(BluetoothDevice.ACTION_FOUND)
                }
        )
    }

    companion object {
        private class Receiver(
            private val bluetoothEnabled: Event<Unit>,
            private val discoveryEnded: Event<Unit>,
            private val deviceDiscovered: Event<BloothDevice>
        ) : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    BluetoothAdapter.ACTION_STATE_CHANGED -> handleStateChanged()
                    BluetoothDevice.ACTION_FOUND -> intent.handleFound()
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> context.handleDiscoveryFinished()
                }
            }

            private fun Context.handleDiscoveryFinished() {
                unregisterReceiver(this)
                    .also { discoveryEnded.eventOccurred(Unit) }
            }

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

            private fun unregisterReceiver(context: Context) {
                context.unregisterReceiver(this)
            }
        }
    }
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
