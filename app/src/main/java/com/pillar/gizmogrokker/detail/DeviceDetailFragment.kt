package com.pillar.gizmogrokker.detail

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothHeadset.*
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothProfile.ServiceListener
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.IBinder
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
import java.io.Serializable


class DeviceDetailFragment : Fragment() {
    private val unknown = "Unknown"
    private val device: BloothDevice? get() = arguments?.serializable("device")

    private val serviceIntent get() = Intent(context, HeadsetService::class.java)

    private val speech: RecognitionListener = object : RecognitionListener {
        override fun onRmsChanged(rmsdB: Float) {}

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEvent(eventType: Int, params: Bundle?) {}

        override fun onReadyForSpeech(params: Bundle?) {}

        override fun onBeginningOfSpeech() {}

        override fun onEndOfSpeech() {}

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

            val intent = serviceIntent.apply { action = HeadsetService.ACTION_STOP_VOICE }
            context?.startService(intent)
        }
    }

    private val recognizer
        get() = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(speech)
        }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.getIntExtra(EXTRA_STATE, -1)) {
                STATE_AUDIO_CONNECTED -> startSpeechRecognition()
                else -> println("AUDIO NOT CONNECTED")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.device_detail_fragment, container, false)
        .apply { updateUIElements() }

    override fun onStart() {
        super.onStart()
        context?.registerReceiver(receiver, IntentFilter(ACTION_AUDIO_STATE_CHANGED))
        context?.startService(serviceIntent)
        connect_device.setOnClickListener { startVoice() }
    }

    private fun startVoice() {
        val startVoice = serviceIntent.apply { action = HeadsetService.ACTION_START_VOICE }
        context?.startService(startVoice)
    }

    private fun startSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "com.pillar.gizmogrokker");
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        }

        recognizer.startListening(intent)
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
        context?.unregisterReceiver(receiver)
        context?.stopService(serviceIntent)
    }

}

@Suppress("UNCHECKED_CAST")
private fun <T : Serializable> Bundle.serializable(s: String) = getSerializable(s) as T


class HeadsetService : Service() {

    private lateinit var adapter: BluetoothAdapter
    private lateinit var headset: BluetoothHeadset

    private val listener = object : ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            headset = proxy as BluetoothHeadset
        }

        override fun onServiceDisconnected(profile: Int) {}
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_AUDIO_STATE_CHANGED -> notifyAudioState(intent)
                ACTION_CONNECTION_STATE_CHANGED -> notifyConnectState(intent)
                ACTION_VENDOR_SPECIFIC_HEADSET_EVENT -> notifyATEvent(intent)
                else -> println("WHAT?!")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        adapter = BluetoothAdapter.getDefaultAdapter().apply {
            getProfileProxy(this@HeadsetService, listener, BluetoothProfile.HEADSET)
        }

        val filter = IntentFilter(ACTION_AUDIO_STATE_CHANGED).apply {
            addAction(ACTION_CONNECTION_STATE_CHANGED)
            addAction(ACTION_VENDOR_SPECIFIC_HEADSET_EVENT)
        }

        registerReceiver(receiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_VOICE -> startVoice()
            ACTION_STOP_VOICE -> stopVoice()
            else -> println("WHAT DO?!")
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.closeProfileProxy(BluetoothProfile.HEADSET, headset)
        unregisterReceiver(receiver)
    }

    private fun startVoice() {
        val device = headset.connectedDevices[0]
        headset.startVoiceRecognition(device)
    }

    private fun stopVoice() {
        val device = headset.connectedDevices[0]
        headset.stopVoiceRecognition(device)
    }

    private fun notifyAudioState(intent: Intent) {
        val message = when (intent.getIntExtra(EXTRA_STATE, -1)) {
            STATE_AUDIO_CONNECTED -> "AUDIO CONNECTED"
            STATE_AUDIO_CONNECTING -> "AUDIO CONNECTING"
            STATE_AUDIO_DISCONNECTED -> "AUDIO DISCONNECTED"
            else -> "AUDIO UNKNOWN??"
        }

        println(message)
    }

    private fun notifyConnectState(intent: Intent) {
        val message = when (intent.getIntExtra(EXTRA_STATE, -1)) {
            STATE_CONNECTED -> "Connected"
            STATE_CONNECTING -> "Connecting"
            STATE_DISCONNECTING -> "Disconnecting"
            STATE_DISCONNECTED -> "Disconnected"
            else -> "Connect Unknown"
        }

        println(message)
    }

    private fun notifyATEvent(intent: Intent) {
        val command = intent.getStringExtra(EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_CMD)

        val typeString =
            when (intent.getIntExtra(EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_CMD_TYPE, -1)) {
                AT_CMD_TYPE_ACTION -> "AT Action"
                AT_CMD_TYPE_READ -> "AT Read"
                AT_CMD_TYPE_TEST -> "AT Test"
                AT_CMD_TYPE_SET -> "AT Set"
                AT_CMD_TYPE_BASIC -> "AT Basic"
                else -> "AT Unknown"
            }

        println("$typeString: $command")
    }

    override fun onBind(intent: Intent): IBinder? = null

    companion object {
        const val ACTION_START_VOICE = "com.pillar.gizmogrokker.action.START_VOICE"
        const val ACTION_STOP_VOICE = "com.pillar.gizmogrokker.action.STOP_VOICE"
    }
}