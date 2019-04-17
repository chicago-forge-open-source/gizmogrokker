package com.pillar.gizmogrokker

class FindBloothDevicesCommand {

    sealed class Result {

        data class FoundDevices(val foundDeviceList: List<BloothDevice>) :
            Result()

        object NoBlooth : Result()

        object MustRequest : Result()

    }
}

interface FindBloothDevicesCommandDispatcher {

    val bluetoothDiscotech: BluetoothDiscotech
    val checker: BluetoothPermissionChecker

    suspend fun FindBloothDevicesCommand.perform() = checkBluetoothState()
        .run {
            when (this) {
                GrokkerBluetoothState.Ready -> FindBloothDevicesCommand.Result.FoundDevices(
                    bluetoothDiscotech.discoverize()
                )
                GrokkerBluetoothState.MustRequest -> FindBloothDevicesCommand.Result.MustRequest
                GrokkerBluetoothState.NoBlooth -> FindBloothDevicesCommand.Result.NoBlooth
            }
        }

    private fun checkBluetoothState() = checker.checkBluetoothState()

}


