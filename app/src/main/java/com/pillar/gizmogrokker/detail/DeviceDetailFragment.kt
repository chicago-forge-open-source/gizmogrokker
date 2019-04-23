package com.pillar.gizmogrokker.detail

import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothHeadset.*
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

    private lateinit var adapter: BluetoothAdapter
    private lateinit var recognizer: SpeechRecognizer

    private val speech: RecognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            println("READY FOR SPEECH")
        }

        override fun onRmsChanged(rmsdB: Float) {}

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onBeginningOfSpeech() {
            println("BEGINNING OF SPEECH")
        }

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

    private val mAudioReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val currentState = intent.getIntExtra(EXTRA_STATE, -1);
            if (STATE_AUDIO_CONNECTED == currentState) {
                val sweetIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,"voice.recognition.test");
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);
                }

                recognizer.startListening(sweetIntent);
            }
        }
    }

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
        adapter = BluetoothAdapter.getDefaultAdapter()


        recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(speech)
        }

        val intent = Intent(context, HeadsetService::class.java)
        context?.startService(intent)

        connect_device.setOnClickListener {
            println("CLICKO")

            val startVoice = Intent(context, HeadsetService::class.java).apply {
                action = HeadsetService.ACTION_STARTVOICE
            }
            context?.startService(startVoice)

//            val stopVoice = Intent(context, HeadsetService::class.java).apply {
//                intent.action = HeadsetService.ACTION_STOPVOICE
//            }
//
//            context?.startService(stopVoice)
//            context?.stopService(intent)

        }
    }

    override fun onResume() {
        super.onResume()
        context?.registerReceiver(
            mAudioReceiver,
            IntentFilter(ACTION_AUDIO_STATE_CHANGED)
        );
    }

    override fun onPause() {
        super.onPause()
        context?.unregisterReceiver(mAudioReceiver)
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
        context?.unregisterReceiver(mAudioReceiver)
    }

}

@Suppress("UNCHECKED_CAST")
private fun <T : Serializable> Bundle.serializable(s: String) = getSerializable(s) as T


class HeadsetService : Service() {

    lateinit var mBluetoothAdapter: BluetoothAdapter
    internal var mBluetoothHeadset: BluetoothHeadset? = null

    lateinit var mNotificationManager: NotificationManager

    private val mProfileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile == BluetoothProfile.HEADSET) {
                Log.d(TAG, "Connecting HeadsetService...")
                mBluetoothHeadset = proxy as BluetoothHeadset
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HEADSET) {
                Log.d(TAG, "Unexpected Disconnect of HeadsetService...")
                mBluetoothHeadset = null
            }
        }
    }

    private val mProfileReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (ACTION_AUDIO_STATE_CHANGED == action) {
                notifyAudioState(intent)
            }
            if (ACTION_CONNECTION_STATE_CHANGED == action) {
                notifyConnectState(intent)
            }
            if (ACTION_VENDOR_SPECIFIC_HEADSET_EVENT == action) {
                notifyATEvent(intent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Creating HeadsetService...")

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Establish connection to the proxy.
        mBluetoothAdapter.getProfileProxy(this, mProfileListener, BluetoothProfile.HEADSET)

        //Monitor profile events
        val filter = IntentFilter()
        filter.addAction(ACTION_AUDIO_STATE_CHANGED)
        filter.addAction(ACTION_CONNECTION_STATE_CHANGED)
        filter.addAction(ACTION_VENDOR_SPECIFIC_HEADSET_EVENT)
        registerReceiver(mProfileReceiver, filter)

        buildNotification("Service Started...")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_STICKY

        if (ACTION_STARTVOICE == intent.action) {
            startVoice()
        } else if (ACTION_STOPVOICE == intent.action) {
            stopVoice()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Destroying HeadsetService...")
        mBluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, mBluetoothHeadset)

        unregisterReceiver(mProfileReceiver)
        mNotificationManager.cancel(NOTE_ID)
    }

    fun startVoice(): Boolean {
        if (mBluetoothHeadset == null || mBluetoothHeadset!!.connectedDevices.isEmpty()) {
            //No valid connection to initiate
            Toast.makeText(this, "Failed to Start Voice Recognition", Toast.LENGTH_SHORT).show()
            return false
        }

        val device = mBluetoothHeadset!!.connectedDevices[0]
        mBluetoothHeadset!!.startVoiceRecognition(device)
        println("START VOICE")
        return true
    }

    fun stopVoice(): Boolean {
        if (mBluetoothHeadset == null || mBluetoothHeadset!!.connectedDevices.isEmpty()) {
            //No valid connection to initiate
            Toast.makeText(this, "Failed to Stop Voice Recognition", Toast.LENGTH_SHORT).show()
            return false
        }

        val device = mBluetoothHeadset!!.connectedDevices[0]
        mBluetoothHeadset!!.stopVoiceRecognition(device)
        return true
    }

    private fun buildNotification(text: String) {
        println("NOTIFICATION $text")
    }

    private fun notifyAudioState(intent: Intent) {
        val state = intent.getIntExtra(EXTRA_STATE, -1)
        val message: String
        when (state) {
            STATE_AUDIO_CONNECTED -> message = "Audio Connected"
            STATE_AUDIO_CONNECTING -> message = "Audio Connecting"
            STATE_AUDIO_DISCONNECTED -> message = "Audio Disconnected"
            else -> message = "Audio Unknown"
        }

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        buildNotification(message)

    }

    private fun notifyConnectState(intent: Intent) {
        val state = intent.getIntExtra(EXTRA_STATE, -1)
        val message: String
        when (state) {
            STATE_CONNECTED -> message = "Connected"
            STATE_CONNECTING -> message = "Connecting"
            STATE_DISCONNECTING -> message = "Disconnecting"
            STATE_DISCONNECTED -> message = "Disconnected"
            else -> message = "Connect Unknown"
        }

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        buildNotification(message)
    }

    private fun notifyATEvent(intent: Intent) {
        val command =
            intent.getStringExtra(EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_CMD)
        val type =
            intent.getIntExtra(EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_CMD_TYPE, -1)

        val typeString: String
        typeString = when (type) {
            AT_CMD_TYPE_ACTION -> "AT Action"
            AT_CMD_TYPE_READ -> "AT Read"
            AT_CMD_TYPE_TEST -> "AT Test"
            AT_CMD_TYPE_SET -> "AT Set"
            AT_CMD_TYPE_BASIC -> "AT Basic"
            else -> "AT Unknown"
        }

        Toast.makeText(this, "$typeString: $command", Toast.LENGTH_SHORT).show()
        buildNotification("$typeString: $command")
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    companion object {
        private val TAG = "BluetoothProxyMonitorService"
        private val NOTE_ID = 1000

        val ACTION_STARTVOICE = "com.pillar.gizmogrokker.action.STARTVOICE"
        val ACTION_STOPVOICE = "com.pillar.gizmogrokker.action.STOPVOICE"
    }
}