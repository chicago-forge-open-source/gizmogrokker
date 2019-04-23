package com.pillar.gizmogrokker.detail

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothProfile.ServiceListener
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pillar.gizmogrokker.BloothDevice
import com.pillar.gizmogrokker.R
import kotlinx.android.synthetic.main.device_detail_fragment.*
import kotlinx.android.synthetic.main.device_detail_fragment.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.io.Serializable

class DeviceDetailFragment : Fragment() {
    private val unknown = "Unknown"
    private val device: BloothDevice? get() = arguments?.serializable("device")

    private lateinit var job: Job
    private val mainScope get() = CoroutineScope(Dispatchers.Main + job)
    private lateinit var thingo: Thingo
    private lateinit var adapter: BluetoothAdapter
    private lateinit var recognizer: SpeechRecognizer

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
        connect_device.setOnClickListener {
            println("CLICKO")

            adapter = BluetoothAdapter.getDefaultAdapter()
            val device = adapter.getRemoteDevice(device_mac_address.text.toString())


            recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(Speech())
            }

            thingo = Thingo(context!!, device, recognizer)
            adapter.getProfileProxy(context, thingo, BluetoothProfile.HEADSET)
        }
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
        context?.unregisterReceiver(thingo.receiver)
        adapter.closeProfileProxy(BluetoothProfile.HEADSET, thingo.headset)
    }

}

@Suppress("UNCHECKED_CAST")
private fun <T : Serializable> Bundle.serializable(s: String) = getSerializable(s) as T

class Thingo(val context: Context, var device: BluetoothDevice, val recognizer: SpeechRecognizer) :
    ServiceListener {
    var receiver: BroadcastReceiver
    lateinit var headset: BluetoothHeadset

    init {
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED -> audioStateChange(context, intent)
                    BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT -> vendorEvent(intent)
                    BluetoothHeadset.VENDOR_RESULT_CODE_COMMAND_ANDROID -> vendorResultCode(intent)
                }
            }
        }

        context.registerReceiver(receiver, intentFilter())
    }

    companion object {
        fun intentFilter(): IntentFilter =
            IntentFilter(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED).apply {
                addAction(BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT)
                addAction(BluetoothHeadset.VENDOR_RESULT_CODE_COMMAND_ANDROID)
            }
    }

    fun audioStateChange(context: Context?, intent: Intent?) {
        println("AUDIO STATE CHANGED")
        val state = intent?.getIntExtra(BluetoothHeadset.EXTRA_STATE, -1)
        if (BluetoothHeadset.STATE_AUDIO_CONNECTED == state) {
            recognizer.startListening(RecognizerIntent.getVoiceDetailsIntent(context))
        }
    }

    fun vendorEvent(intent: Intent?) {
        println("VENDOR HEADSET EVENT")
    }

    fun vendorResultCode(intent: Intent?) {
        println("VENDOR RESULT CODE")
    }

    override fun onServiceDisconnected(profile: Int) {
        println("DISCONNECTED!")
    }

    override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
        headset = proxy as BluetoothHeadset
        println(headset.isAudioConnected(device))
        headset.startVoiceRecognition(device)

    }
}

class Speech : RecognitionListener {
    override fun onReadyForSpeech(params: Bundle?) {}

    override fun onRmsChanged(rmsdB: Float) {}

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onBeginningOfSpeech() {}

    override fun onEvent(eventType: Int, params: Bundle?) {}

    override fun onEndOfSpeech() {
        println("END OF SPEECH")
    }

    override fun onError(error: Int) {
        println("ERROR $error")
    }

    override fun onPartialResults(partialResults: Bundle?) {
        processSpeech(partialResults)
    }

    override fun onResults(results: Bundle?) {
        processSpeech(results)
    }

    private fun processSpeech(results: Bundle?) {
        val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?: arrayListOf("No results")

        println(text)
    }
}