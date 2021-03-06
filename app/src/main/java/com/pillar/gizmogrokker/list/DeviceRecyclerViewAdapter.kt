package com.pillar.gizmogrokker.list


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pillar.gizmogrokker.BloothDevice
import com.pillar.gizmogrokker.R
import kotlinx.android.synthetic.main.device_fragment.view.*

class DeviceRecyclerViewAdapter(
    private val deviceList: List<BloothDevice>
) : RecyclerView.Adapter<DeviceRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.device_fragment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = deviceList[position]
        with(holder) {
            deviceLabel.text = device.name ?: device.macAddress
            deviceView.tag = device
        }
    }

    override fun getItemCount(): Int = deviceList.size

    inner class ViewHolder(val deviceView: View) : RecyclerView.ViewHolder(deviceView) {
        val deviceLabel: TextView = deviceView.deviceLabel

        override fun toString(): String {
            return "${super.toString()} '${deviceLabel.text}'"
        }
    }
}
