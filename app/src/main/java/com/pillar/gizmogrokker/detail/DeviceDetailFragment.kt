package com.pillar.gizmogrokker.detail

import android.bluetooth.*
import android.bluetooth.BluetoothGattCharacteristic.*
import android.bluetooth.BluetoothGattService.SERVICE_TYPE_PRIMARY
import android.bluetooth.BluetoothGattService.SERVICE_TYPE_SECONDARY
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.beepiz.bluetooth.gattcoroutines.GattConnection
import com.pillar.gizmogrokker.BloothDevice
import com.pillar.gizmogrokker.R
import kotlinx.android.synthetic.main.device_detail_fragment.*
import kotlinx.android.synthetic.main.device_detail_fragment.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import splitties.bitflags.hasFlag
import java.io.Serializable

class DeviceDetailFragment : Fragment() {
    private val unknown = "Unknown"
    private val device: BloothDevice? get() = arguments?.serializable("device")

    private lateinit var job: Job
    private val mainScope get() = CoroutineScope(Dispatchers.Main + job)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.device_detail_fragment, container, false)
        .apply { updateUIElements() }

    override fun onStart() {
        super.onStart()

        val theDevice = device ?: return

        connect_device.setOnClickListener {
            try {
                BluetoothAdapter.getDefaultAdapter().getRemoteDevice(theDevice.macAddress)
                    .logGattServices()
            } catch (mew: Throwable) {
                mew.printStackTrace()
                println("wut")
            }
        }
    }

    fun BluetoothDevice.logGattServices(tag: String = "BleGattCoroutines") = mainScope.launch {
        val deviceConnection = GattConnection(bluetoothDevice = this@logGattServices)
        deviceConnection.connect() // Suspends until connection is established
        val gattServices = deviceConnection.discoverServices() // Suspends until completed
        gattServices.forEach { service ->
            service.characteristics.forEach { characteristic ->

                Log.v(tag, "permission ${characteristic.permissions.toString(16)}")

                try {

                    deviceConnection.readCharacteristic(characteristic) // Suspends until characteristic is read
                } catch (e: Exception) {
                    Log.e(tag, "Couldn't read characteristic with uuid: ${characteristic.uuid}", e)
                }
            }
            Log.v(tag, service.print(printCharacteristics = true))
        }
        deviceConnection.close() // Close when no longer used it NOT optional
    }

    @RequiresApi(18)
    fun BluetoothGattService.print(printCharacteristics: Boolean = true): String {
        return if (printCharacteristics) printWithCharacteristics() else printWithoutCharacteristics()
    }

    @RequiresApi(18)
    fun BluetoothGattService.printWithoutCharacteristics(): String = """UUID: $uuid
        instance ID: $instanceId
        type: $typeString
        characteristics count: ${characteristics.count()}
        included services count: ${includedServices?.count()}
    """

    @RequiresApi(18)
    fun BluetoothGattService.printWithCharacteristics(): String = """UUID: $uuid
        instance ID: $instanceId
        type: $typeString
        characteristics: { ${characteristics.joinToString { it.print() }.prependIndent()} }
        included services count: ${includedServices?.count()}
    """


    @RequiresApi(18)
    fun BluetoothGattCharacteristic.print(): String = """UUID: $uuid
        instance ID: $instanceId
        permissions: $permissionsString
        writeType: $writeTypeString
        properties: $propertiesString
        value: $value
        stringValue: ${getStringValue(0)}
    """

    @RequiresApi(18)
    fun BluetoothGattDescriptor.print(): String = """UUID: $uuid
        permissions: $permissions
        value: $value
        characteristic: ${characteristic?.print()}
    """

    private val BluetoothGattCharacteristic.writeTypeString: String
        @RequiresApi(18) get() = when (writeType) {
            WRITE_TYPE_DEFAULT -> "DEFAULT"
            WRITE_TYPE_NO_RESPONSE -> "NO_RESPONSE"
            WRITE_TYPE_SIGNED -> "SIGNED"
            else -> "UNKNOWN"
        }

    private val BluetoothGattCharacteristic.propertiesString: String
        @RequiresApi(18) get() = propertiesString(properties)

    private val BluetoothGattCharacteristic.permissionsString: String
        @RequiresApi(18) get() {
            return "$permissions"
            //return permissionsString(permissions)
        }

    @Suppress("DEPRECATION")
    @Deprecated("Doesn't seem to work")
    private val BluetoothGattDescriptor.permissionsString: String
        @RequiresApi(18) get() = permissionsString(permissions)

    @RequiresApi(18)
    @Deprecated("Doesn't seem to work")
    private fun permissionsString(permissions: Int): String = StringBuilder().apply {
        if (permissions.hasFlag(PERMISSION_READ)) append("READ, ")
        if (permissions.hasFlag(PERMISSION_READ_ENCRYPTED)) append("READ_ENCRYPTED, ")
        if (permissions.hasFlag(PERMISSION_READ_ENCRYPTED_MITM)) append("READ_ENCRYPTED_MITM, ")
        if (permissions.hasFlag(PERMISSION_WRITE)) append("WRITE, ")
        if (permissions.hasFlag(PERMISSION_WRITE_ENCRYPTED)) append("WRITE_ENCRYPTED, ")
        if (permissions.hasFlag(PERMISSION_WRITE_ENCRYPTED_MITM)) append("WRITE_ENCRYPTED_MITM, ")
        if (permissions.hasFlag(PERMISSION_WRITE_SIGNED)) append("WRITE_SIGNED, ")
        if (permissions.hasFlag(PERMISSION_WRITE_SIGNED_MITM)) append("WRITE_SIGNED_MITM, ")
    }.toString()

    @RequiresApi(18)
    private fun propertiesString(properties: Int): String = StringBuilder().apply {
        if (properties.hasFlag(PROPERTY_READ)) append("READ, ")
        if (properties.hasFlag(PROPERTY_WRITE)) append("WRITE, ")
        if (properties.hasFlag(PROPERTY_WRITE_NO_RESPONSE)) append("WRITE_NO_RESPONSE, ")
        if (properties.hasFlag(PROPERTY_SIGNED_WRITE)) append("SIGNED_WRITE, ")
        if (properties.hasFlag(PROPERTY_INDICATE)) append("INDICATE, ")
        if (properties.hasFlag(PROPERTY_NOTIFY)) append("NOTIFY, ")
        if (properties.hasFlag(PROPERTY_BROADCAST)) append("BROADCAST, ")
        if (properties.hasFlag(PROPERTY_EXTENDED_PROPS)) append("EXTENDED_PROPS, ")
    }.toString()

    private val BluetoothGattService.typeString: String
        @RequiresApi(18) get() = when (type) {
            SERVICE_TYPE_PRIMARY -> "PRIMARY"
            SERVICE_TYPE_SECONDARY -> "SECONDARY"
            else -> "UNKNOWN"
        }


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

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

}

@Suppress("UNCHECKED_CAST")
private fun <T : Serializable> Bundle.serializable(s: String) = getSerializable(s) as T