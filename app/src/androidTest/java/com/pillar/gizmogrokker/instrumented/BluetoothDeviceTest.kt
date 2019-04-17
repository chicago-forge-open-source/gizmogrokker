package com.pillar.gizmogrokker.instrumented

import android.Manifest
import android.bluetooth.BluetoothAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.pillar.gizmogrokker.BloothDevice
import com.pillar.gizmogrokker.R
import com.pillar.gizmogrokker.detail.DeviceDetailActivity
import com.pillar.gizmogrokker.list.DeviceListActivity
import com.pillar.gizmogrokker.list.DeviceListFragment
import com.schibsted.spain.barista.interaction.PermissionGranter.allowPermissionsIfNeeded
import com.zegreatrob.testmints.setup
import kotlinx.android.synthetic.main.device_list_activity.*
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.Matchers.equalTo
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class BluetoothDeviceTest {

    @get:Rule
    val activityRule = IntentsTestRule(DeviceListActivity::class.java)

    private val findDevicesButton get() = onView(withId(R.id.findDevices))

    private val deviceList get() = onView(withId(R.id.fragment))

    private val deviceListFragment get() = activityRule.activity.fragment as DeviceListFragment

    @Before
    fun setUp() {
        assumeTrue(BluetoothAdapter.getDefaultAdapter() != null)
    }

    @Test
    fun canFindBluetoothDevicesAndShowDetails(): Unit = setup(object {}) exercise {
        findDevicesButton.perform(click())
        ifPermissionBlockedGrantThem()
        waitToFindDevices()
    } verify {
        deviceList.check(matches(hasMinimumChildCount(1)))

        nextStageOfTest()
    } exercise {
        deviceList.perform(clickOnItemAtIndex(0))
    } verify {
        intended(hasComponent(DeviceDetailActivity::class.java.name))
        intended(
            hasExtra(equalTo("device"), instanceOf<BloothDevice>())
        )
    }

    private fun ifPermissionBlockedGrantThem() {
        allowPermissionsIfNeeded(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun duration(start: Long) = System.currentTimeMillis() - start

    private fun waitUntil(waitTime: Int, shouldContinue: () -> Boolean) {
        val start = System.currentTimeMillis()
        while (!shouldContinue() && duration(start) < waitTime) {
            Thread.yield()
        }
    }

    private fun waitToFindDevices() {
        deviceListFragment.run {
            waitUntil(30_000) { deviceList.isNotEmpty() }
        }
    }

    private fun clickOnItemAtIndex(position: Int) =
        RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
            position,
            click()
         )

    private inline fun <reified T> instanceOf() = instanceOf<T>(T::class.java)
}

fun <C : Any> C.nextStageOfTest() = setup(this)