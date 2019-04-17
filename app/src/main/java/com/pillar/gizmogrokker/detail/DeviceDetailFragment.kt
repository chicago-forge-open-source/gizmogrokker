package com.pillar.gizmogrokker.detail

import android.bluetooth.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pillar.gizmogrokker.BloothDevice
import com.pillar.gizmogrokker.R
import kotlinx.android.synthetic.main.device_detail_fragment.*
import kotlinx.android.synthetic.main.device_detail_fragment.view.*
import java.io.Serializable
import java.util.*

class DeviceDetailFragment : Fragment() {
    private val unknown = "Unknown"
    private val device: BloothDevice? get() = arguments?.serializable("device")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.device_detail_fragment, container, false)
        .apply { updateUIElements() }

    private fun View.updateUIElements() = device?.apply {
        device_mac_address.text = macAddress()
        device_name.text = name()
        device_type.text = displayType()
        device_major_class.text = majorClass()
        device_minor_class.text = minorClass()
        device_services.text = services()
    }

    private fun BloothDevice.macAddress() = macAddress
    private fun BloothDevice.name(): String = name ?: unknown
    private fun BloothDevice.displayType(): String = type.displayName
    private fun BloothDevice.majorClass(): String = majorClass?.toString() ?: unknown
    private fun BloothDevice.minorClass(): String = minorClass?.displayName() ?: unknown
    private fun BloothDevice.services(): String =
        services.ifEmpty { listOf(unknown) }.joinToString(", ")

    override fun onStart() {
        super.onStart()
        connect_device.setOnClickListener {
            val adapter = BluetoothAdapter.getDefaultAdapter()
            val btDevice = adapter.getRemoteDevice(device_mac_address.text.toString())
            val socket = btDevice.createRfcommSocketToServiceRecord(btDevice.uuids.first().uuid)
            socket.remoteDevice.connectGatt(context, true, Callback())
            println("connecting gatt...")
        }
    }
}

@Suppress("UNCHECKED_CAST")
private fun <T : Serializable> Bundle.serializable(s: String) = getSerializable(s) as T

class Callback : BluetoothGattCallback() {
    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        println("onConnectionStateChange")
        gatt?.discoverServices()
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        super.onServicesDiscovered(gatt, status)
        println("onServicesDiscovered")

        val batteryServiceUUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb")
        val batteryLevelUUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb")

        gatt?.services?.flatMap { it.characteristics }?.forEach {

            if(it.uuid == batteryLevelUUID || it.uuid == batteryServiceUUID) {
                gatt.readCharacteristic(it)
            }
        }

//        val char1 = BluetoothGattCharacteristic(batteryServiceUUID, 1, 1)
//        val char2 = BluetoothGattCharacteristic(batteryLevelUUID, 0, 0)
//
//        val descriptor1 = BluetoothGattDescriptor(batteryServiceUUID, 0)
//        val descriptor2 = BluetoothGattDescriptor(batteryLevelUUID, 1)
//
//        gatt?.readCharacteristic(char1)
//        gatt?.readCharacteristic(char2)
//        gatt?.readDescriptor(descriptor1)
//        gatt?.readDescriptor(descriptor2)
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        super.onCharacteristicRead(gatt, characteristic, status)
        println("onCharacteristicRead")
        println(status)
        if (status == BluetoothGatt.GATT_SUCCESS) {
            characteristic?.apply {
                println("uint8 " + getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0))
                println("string " + getStringValue(0))
            }
        }
    }

    override fun onDescriptorRead(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        super.onDescriptorRead(gatt, descriptor, status)
        println("onDescriptorRead")
        println(descriptor)
        println(status)
    }
}