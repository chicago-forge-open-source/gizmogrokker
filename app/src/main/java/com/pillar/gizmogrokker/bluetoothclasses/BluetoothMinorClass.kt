package com.pillar.gizmogrokker.bluetoothclasses

import com.pillar.gizmogrokker.IntEnum

enum class BluetoothMinorClass(override val value: Int, val display: String? = null) :
    IntEnum {
    ComputerUncategorized(0x0100, "Uncategorized"),
    DesktopComputer(0x0104, "Desktop Computer"),
    Laptop(0x010C),
    Server(0x0108),
    HandheldPDA(0x0110, "Handheld PC"),
    PalmPDA(0x0114, "Palm Sized PC"),
    ComputerWearable(0x0118, "Wearable PC"),

    PhoneUncategorized(0x0200, "Uncategorized"),
    Cellular(0x0204, "Cell Phone"),
    Cordless(0x0208, "Cordless Phone"),
    Smart(0x020C, "Smart Phone"),
    ISDN(0x0214, "Integrated Services Digital Network Phone"),
    ModemOrGateway(0x0210, "Modem or Gateway"),

    AVUncategorized(0x0400, "Uncategorized"),
    Headset(0x0404),
    HandsFree(0x0408, "Hands Free"),
    Microphone(0x0410),
    Loudspeaker(0x0414),
    Headphones(0x0418),
    Portable(0x041C, "Portable Speaker"),
    VCR(0x042C),
    Car(0x0420, "Car Audio"),
    TopBox(0x0424, "Top Box"),
    HiFi(0x0428),
    Camera(0x0430),
    Camcorder(0x0434),
    Monitor(0x0438),
    DisplayAndLoudspeaker(0x043C, "Display and Loudspeaker"),
    Conferencing(0x0440),
    VideoGamingToy(0x0448, "Gaming Toy"),

    WearableUncategorized(0x7000, "Uncategorized"),
    Watch(0x0704),
    Pager(0x0708),
    Jacket(0x070C),
    Helmet(0x0710),
    Glasses(0x0714),

    ToyUncategorized(0x0800, "Uncategorized"),
    Robot(0x0804),
    Vehicle(0x0808, "Toy Vehicle"),
    Controller(0x0810, "Toy Controller"),
    DollActionFigure(0x080C, "Doll or Action Figure"),
    Game(0x0814),

    HealthUncategorized(0x0900, "Uncategorized"),
    BloodPressure(0x0904, "Blood Pressure Monitor"),
    Thermometer(0x0908),
    Weighing(0x090C, "Scale"),
    Glucose(0x0914, "Glucose Monitor"),
    PulseOximeter(0x0914, "Pulse Oximeter"),
    PulseRate(0x0918, "Pulse Rate Monitor"),
    HealthDisplay(0x091C, "Health Display");


    companion object {
        fun fromInt(value: Int): BluetoothMinorClass? =
            IntEnum.fromInt(value)
    }

    fun displayName(): String = this.display ?: this.toString()
}