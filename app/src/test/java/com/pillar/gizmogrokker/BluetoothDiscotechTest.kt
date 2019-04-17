package com.pillar.gizmogrokker

import android.bluetooth.BluetoothAdapter
import android.content.Context
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Test

class BluetoothDiscotechTest {

    @Test
    fun whenBluetoothIsEnabledShouldStartDiscovery() = runBlocking {
        val bluetoothInterface = FakeBluetoothInterface(isEnabled = true, autoFinishDiscovery = true)
        val discotech = BluetoothDiscotech(bluetoothInterface)

        discotech.discoverize()

        assertEquals(1, bluetoothInterface.startDiscoveryCallCount)
    }.let { Unit }

    @Test
    fun whenBluetoothIsDisabledShouldEnableAndStartDiscovery() = runBlocking {
        val bluetoothInterface = FakeBluetoothInterface()
        val discotech = BluetoothDiscotech(bluetoothInterface)
        discotech.discoverize()

        bluetoothInterface.apply {
            assertEquals(1, enableCount)
            assertEquals(0, startDiscoveryCallCount)
            bluetoothEnabled.eventOccurred(Unit)
            assertEquals(1, startDiscoveryCallCount)
        }
    }.let { Unit }

    @Test
    fun afterEnablingWillNotDiscoverEachTimeBluetoothIsEnabled() = runBlocking {
        val bluetoothInterface = FakeBluetoothInterface()

        val discotech = BluetoothDiscotech(bluetoothInterface)
        discotech.discoverize()

        bluetoothInterface.apply {
            bluetoothEnabled.eventOccurred(Unit)
            bluetoothEnabled.eventOccurred(Unit)
            bluetoothEnabled.eventOccurred(Unit)
            bluetoothEnabled.eventOccurred(Unit)

            assertEquals(1, startDiscoveryCallCount)
        }
    }.let { Unit }

    @Test
    fun willReturnListContainingBluetoothDevices() = runBlocking {
        val bluetoothInterface = FakeBluetoothInterface(autoFinishDiscovery = false)

        val discotech = BluetoothDiscotech(bluetoothInterface)

        val discoveredDevicesDeferred = async { discotech.discoverize() }

        val devices = listOf(
            BloothDevice(
                name = "Banana",
                macAddress = "38-F9-D3-3E-3E-D3",
                type = DeviceType.Classic,
                majorClass = null,
                minorClass = null,
                services = emptyList()
            ),
            BloothDevice(
                name = null,
                macAddress = "10-94-BB-B5-F4-AA",
                type = DeviceType.Dual,
                majorClass = null,
                minorClass = null,
                services = emptyList()
            )
        )

        runBlocking {
            devices.forEach { bluetoothInterface.deviceDiscovered.eventOccurred(it) }
            bluetoothInterface.discoveryEnded.eventOccurred(Unit)
            Unit
        }

        val actualDevices = discoveredDevicesDeferred.await()
        assertEquals(devices, actualDevices)
    }.let { Unit }
}

class FakeBluetoothInterface(
    override val isEnabled: Boolean = false,
    val autoFinishDiscovery: Boolean = true
) : BluetoothInterface {
    override val bluetoothEnabled = DataEvent<Unit>()
    override val deviceDiscovered = DataEvent<BloothDevice>()
    override val adapter: BluetoothAdapter get() = throw NotImplementedError("Will not implement.")
    override val context: Context get() = throw NotImplementedError("Will not implement.")
    override val discoveryEnded = AutoFinishEvent(autoFinishDiscovery = autoFinishDiscovery)

    var startDiscoveryCallCount: Int = 0
    var enableCount: Int = 0

    override fun enable() {
        enableCount++
    }

    override fun startDiscovery() {
        startDiscoveryCallCount++
    }

}

class AutoFinishEvent(
    private val dataEvent: DataEvent<Unit> = DataEvent(),
    val autoFinishDiscovery: Boolean = true
) : Event<Unit> by dataEvent {
    override fun plus(listener: DiscoveryEndedCallback) {
        if (autoFinishDiscovery) {
            listener(Unit)
        } else {
            dataEvent.plus(listener)
        }
    }
}