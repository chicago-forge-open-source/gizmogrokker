package com.pillar.gizmogrokker.instrumented

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.pillar.gizmogrokker.BloothDevice
import com.pillar.gizmogrokker.DeviceType
import com.pillar.gizmogrokker.R
import com.pillar.gizmogrokker.bluetoothclasses.BluetoothMajorClass
import com.pillar.gizmogrokker.bluetoothclasses.BluetoothMinorClass
import com.pillar.gizmogrokker.bluetoothclasses.BluetoothServiceClass
import com.pillar.gizmogrokker.detail.DeviceDetailFragment
import com.zegreatrob.testmints.setup
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class DeviceDetailFragmentTest {

    @Test
    fun showsDeviceMacAddress(): Unit = setup(object {
        val device = minimumValidBlooth()
    }) exercise {
        device.asFragmentArgs()
            .launch<DeviceDetailFragment>()
    } verify {
        onView(withId(R.id.device_mac_address))
            .check(matches(withText(device.macAddress)))
    }

    @Test
    fun showsDeviceName(): Unit = setup(object {
        val device = stubBloothDevice(name = "Lucille")
    }) exercise {
        device.asFragmentArgs()
            .launch<DeviceDetailFragment>()
    } verify {
        onView(withId(R.id.device_name))
            .check(matches(withText(device.name)))
    }

    @Test
    fun whenNoNameShowsNoNameMessage(): Unit = setup(object {
        val noNameBlooth = minimumValidBlooth()
    }) exercise {
        noNameBlooth.asFragmentArgs()
            .launch<DeviceDetailFragment>()
    } verify {
        onView(withId(R.id.device_name))
            .check(matches(withText("Unknown")))
    }

    private fun BloothDevice.asFragmentArgs() = Bundle().apply {
        putSerializable("device", this@asFragmentArgs)
    }

    @Test
    fun showsDeviceType(): Unit = setup(object {
        val device = stubBloothDevice(type = DeviceType.LE)
    }) exercise {
        device.asFragmentArgs()
            .launch<DeviceDetailFragment>()
    } verify {
        onView(withId(R.id.device_type))
            .check(matches(withText("Low Energy")))
    }

    @Test
    fun showsMajorClass(): Unit = setup(object {
        val device = stubBloothDevice(majorClass = BluetoothMajorClass.Toy)
    }) exercise {
        device.asFragmentArgs()
            .launch<DeviceDetailFragment>()
    } verify {
        onView(withId(R.id.device_major_class))
            .check(matches(withText("Toy")))
    }

    @Test
    fun whenNoMajorClassShowUnknown(): Unit = setup(object {
        val device = minimumValidBlooth()
    }) exercise {
        device.asFragmentArgs()
            .launch<DeviceDetailFragment>()
    } verify {
        onView(withId(R.id.device_major_class))
            .check(matches(withText("Unknown")))
    }

    @Test
    fun showMinorClass(): Unit = setup(object {
        val device = stubBloothDevice(minorClass = BluetoothMinorClass.ToyUncategorized)
    }) exercise {
        device.asFragmentArgs()
            .launch<DeviceDetailFragment>()
    } verify {
        onView(withId(R.id.device_minor_class))
            .check(matches(withText("Uncategorized")))
    }

    @Test
    fun whenNoMinorClassShowUnknown(): Unit = setup(object {
        val device = minimumValidBlooth()
    }) exercise {
        device.asFragmentArgs()
            .launch<DeviceDetailFragment>()
    } verify {
        onView(withId(R.id.device_minor_class))
            .check(matches(withText("Unknown")))
    }

    @Test
    fun whenServicesAreAvailableShowsDeviceServices(): Unit = setup(object {
        val device = stubBloothDevice(
            services = listOf(BluetoothServiceClass.Audio, BluetoothServiceClass.Capture)
        )
    }) exercise {
        device.asFragmentArgs()
            .launch<DeviceDetailFragment>()
    } verify {
        onView(withId(R.id.device_services))
            .check(matches(withText("Audio, Capture")))
    }

    @Test
    fun whenNoDeviceServicesAreAvailableShowUnknown(): Unit = setup(object {
        val device = minimumValidBlooth()
    }) exercise {
        device.asFragmentArgs()
            .launch<DeviceDetailFragment>()
    } verify {
        onView(withId(R.id.device_services))
            .check(matches(withText("Unknown")))
    }

    private fun minimumValidBlooth(): BloothDevice = BloothDevice(
        name = null,
        macAddress = "12:34:56:78",
        type = DeviceType.Classic,
        majorClass = null,
        minorClass = null,
        services = emptyList()
    )
}

private inline fun <reified T : Fragment> Bundle.launch() = launchFragmentInContainer<T>(this)

fun stubBloothDevice(
    name: String? = "Buster",
    macAddress: String = "Big",
    type: DeviceType = DeviceType.LE,
    majorClass: BluetoothMajorClass? = BluetoothMajorClass.Toy,
    minorClass: BluetoothMinorClass? = BluetoothMinorClass.DollActionFigure,
    services: List<BluetoothServiceClass> = listOf(
        BluetoothServiceClass.Audio,
        BluetoothServiceClass.Capture
    )
): BloothDevice = BloothDevice(
    name = name,
    macAddress = macAddress,
    type = type,
    majorClass = majorClass,
    minorClass = minorClass,
    services = services
)