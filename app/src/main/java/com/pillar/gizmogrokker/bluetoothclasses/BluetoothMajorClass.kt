package com.pillar.gizmogrokker.bluetoothclasses

import com.pillar.gizmogrokker.IntEnum

enum class BluetoothMajorClass(override val value: Int) : IntEnum {
    Misc(0x0000),
    Computer(0x0100),
    Phone(0x0200),
    Networking(0x0300),
    AV(0x0400),
    Peripheral(0x0500),
    Imaging(0x0600),
    Wearable(0x0700),
    Toy(0x0800),
    Health(0x0900),
    Uncategorized(0x1F00);

    companion object {
        fun fromInt(value: Int): BluetoothMajorClass? =
            IntEnum.fromInt(value)
    }
}