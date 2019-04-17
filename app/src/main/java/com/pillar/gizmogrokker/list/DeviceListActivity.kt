package com.pillar.gizmogrokker.list

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.pillar.gizmogrokker.*
import com.pillar.gizmogrokker.detail.DeviceDetailActivity
import kotlinx.android.synthetic.main.device_fragment.view.*
import kotlinx.android.synthetic.main.device_list_activity.*
import kotlinx.coroutines.*

const val REQUEST_BLUETOOTH_PERMISSIONS = 1

class DeviceListActivity : AppCompatActivity() {

    companion object {
        private val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private val commandDispatcher by lazy {
        object : FindBloothDevicesCommandDispatcher {

            private val bluetoothInterface = object : BluetoothInterface {
                override val bluetoothEnabled = DataEvent<Unit>()
                override val discoveryEnded = DataEvent<Unit>()
                override val deviceDiscovered = DataEvent<BloothDevice>()
                override val context: Context get() = applicationContext
                override val adapter = BluetoothAdapter.getDefaultAdapter()
            }

            override val bluetoothDiscotech: BluetoothDiscotech =
                BluetoothDiscotech(bluetoothInterface)
            override val checker: BluetoothPermissionChecker =
                BluetoothPermissionChecker(bluetoothInterface, PermissionProxy(applicationContext))
        }
    }

    private lateinit var job: Job
    private lateinit var viewModel: DeviceListViewModel
    private val ioScope get() = CoroutineScope(Dispatchers.IO + job)
    private val uiScope get() = CoroutineScope(Dispatchers.Main + job)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.device_list_activity)
        job = Job()

        viewModel = ViewModelProviders.of(this).get(DeviceListViewModel::class.java)

        updateViewState()
    }

    private fun updateViewState() {
        deviceListFragment.deviceList = viewModel.deviceList
        findDevices.apply {
            isEnabled = viewModel.buttonEnabled && !viewModel.isScanning
            text = if (viewModel.isScanning) "Scanning" else viewModel.buttonText
        }
    }

    private val deviceListFragment get() = fragment as DeviceListFragment

    fun View.onFindBloothDevicesClick() {
        viewModel.isScanning = true
        updateViewState()

        ioScope.launch {
            FindBloothDevicesCommand()
                .perform()
                .showResultsInUI()
        }
    }

    private suspend fun FindBloothDevicesCommand.perform() = with(commandDispatcher) { perform() }

    private suspend fun FindBloothDevicesCommand.Result.showResultsInUI() {
        when (this) {
            is FindBloothDevicesCommand.Result.FoundDevices -> viewModel.run {
                deviceList = foundDeviceList
                isScanning = false
            }
            FindBloothDevicesCommand.Result.MustRequest -> requestPermissions()
            FindBloothDevicesCommand.Result.NoBlooth -> viewModel.run {
                buttonEnabled = false
                buttonText = getString(R.string.NoBloothText)
            }
        }

        performUiUpdate { updateViewState() }
    }

    private suspend fun performUiUpdate(block: suspend CoroutineScope.() -> Unit) {
        withContext(uiScope.coroutineContext, block)
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            permissions,
            REQUEST_BLUETOOTH_PERMISSIONS
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_BLUETOOTH_PERMISSIONS -> {
                when (grantResults[0]) {
                    PackageManager.PERMISSION_GRANTED -> permissionGranted()
                    else -> println("grant results ${grantResults[0]}")
                }
            }
        }
    }

    private fun permissionGranted() = ioScope.launch {
        FindBloothDevicesCommand().perform()
            .showResultsInUI()
    }

    fun View.onShowDeviceDetailClick() {
        val intent = Intent(context, DeviceDetailActivity::class.java)
            .apply { putExtra("device", device_tag.tag as BloothDevice) }

        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}

class DeviceListViewModel(
    var deviceList: List<BloothDevice> = emptyList(),
    var buttonText: String = "Find Blooth Devices!",
    var buttonEnabled: Boolean = true,
    var isScanning: Boolean = false
) : ViewModel()
