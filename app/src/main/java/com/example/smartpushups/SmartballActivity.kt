package com.example.smartpushups

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import splitties.toast.toast
import java.io.IOException
import java.util.*

class SmartballActivity : AppCompatActivity() {

    private val tv : TextView by lazy{ findViewById(R.id.tv)}
    private val btn : Button by lazy{ findViewById(R.id.btn)}
    private val ivLeft : ImageView by lazy{ findViewById(R.id.ivLeft)}
    private val ivRight : ImageView by lazy{ findViewById(R.id.ivRight)}
    private val ivUp : ImageView by lazy{ findViewById(R.id.ivUp)}
    private val ivDown : ImageView by lazy{ findViewById(R.id.ivDown)}
    private val ivLeftHand : ImageView by lazy{ findViewById(R.id.ivLeftHand)}
    private val ivRightHand : ImageView by lazy{ findViewById(R.id.ivRightHand)}

    private lateinit var address : String

    private var isConnected = false
    private var receivingData = false
    private var isChanged = false

    private val mHandler: Handler by lazy { Handler() }
    private lateinit var mRunnable: Runnable
    private val mBluetooth: BluetoothAdapter by lazy { BluetoothAdapter.getDefaultAdapter() }
    private var mSocket: BluetoothSocket? = null

    val mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private val left = 1
    private val right = 2
    private val top = 3
    private val down = 4
    private var prevDirection = 0

    private val leftHand = 1
    private val rightHand = 2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_smartball)

        ivLeftHand.setColorFilter(resources.getColor(R.color.black))
        ivRightHand.setColorFilter(resources.getColor(R.color.black))
        ivLeft.visibility = View.INVISIBLE
        ivRight.visibility = View.INVISIBLE
        ivUp.visibility = View.INVISIBLE
        ivDown.visibility = View.INVISIBLE

        val intent = intent
        address = intent.getStringExtra(SmartballConnectActivity.EXTRA_ADDRESS).toString()

        toast(address)

        ConnectBT().execute()

        if(isConnected) {
            readBuffer()
        }

        //Training starten und beenden
        btn.setOnClickListener {
            receivingData = !receivingData
            dataStart()

            if (receivingData) {
                sendBTMessage("T")
                btn.text = getString(R.string.stop)
                btn.setBackgroundColor(getColor(R.color.btn_orange))
            } else {
                sendBTMessage("F")
                val intent = Intent(this, SmartballActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent)
            }
        }


    }

    //Verbindung mit ESP32 aufbauen
    private inner class ConnectBT : AsyncTask<Void, Void, Void>() {
        private var connectSuccess = true

        override fun onPreExecute() {
            toast(getString(R.string.connecting))
        }

        override fun doInBackground(vararg devices: Void?): Void? {
            try {
                if (mSocket == null || !isConnected) {
                    val device = mBluetooth.getRemoteDevice(address)
                    mSocket = device.createInsecureRfcommSocketToServiceRecord(mUUID)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    mSocket!!.connect()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                connectSuccess = false
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)

            if (!connectSuccess) {
                toast(getString(R.string.connect_failed))
                finish()
            } else {
                toast(getString(R.string.connect_success))
                isConnected = true
            }
        }
    }

    //Daten vom ESP32 empfangen
    private fun readBuffer() {
        val buffer = ByteArray(2048)
        val builder = StringBuilder()

        try {
            val mInputStream = mSocket!!.inputStream
            val bytes = mInputStream.read(buffer)
            val message = String(buffer, 0, bytes)
            builder.append(message)
            if (builder.toString().contains(":") && builder.toString().contains(";")) {
                val msg = builder.toString()
                val output = processString(msg, ":", true)
                var finalOutput: Array<String>

                output.forEach { string ->
                    if (string.contains(";")) {
                        finalOutput = processString(string, ";", false)

                        finalOutput.forEach {
                            if (it.isNotEmpty()) {

                                if(it.toInt() != prevDirection || prevDirection == 0){
                                    isChanged = true
                                }
                                prevDirection = it.toInt()

                                //Für die empfangene Richtung, die dazu gehörige Anzeige darstellen
                                when(it.toInt()){
                                    left -> {
                                        tv.text = getString(R.string.left)
                                        ivLeft.visibility = View.VISIBLE
                                        ivRight.visibility = View.INVISIBLE
                                        ivUp.visibility = View.INVISIBLE
                                        ivDown.visibility = View.INVISIBLE
                                    }
                                    right -> {
                                        tv.text = getString(R.string.right)
                                        ivLeft.visibility = View.INVISIBLE
                                        ivRight.visibility = View.VISIBLE
                                        ivUp.visibility = View.INVISIBLE
                                        ivDown.visibility = View.INVISIBLE
                                    }
                                    top -> {
                                        tv.text = getString(R.string.up)
                                        ivLeft.visibility = View.INVISIBLE
                                        ivRight.visibility = View.INVISIBLE
                                        ivUp.visibility = View.VISIBLE
                                        ivDown.visibility = View.INVISIBLE
                                    }
                                    down -> {
                                        tv.text = getString(R.string.down)
                                        ivLeft.visibility = View.INVISIBLE
                                        ivRight.visibility = View.INVISIBLE
                                        ivUp.visibility = View.INVISIBLE
                                        ivDown.visibility = View.VISIBLE
                                    }

                                }

                            }
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            toast(e.localizedMessage!!)
            mSocket = null
            finish()
        }
    }

    //Empfangene Daten decodieren
    private fun processString(msg: String, splitter: String, first: Boolean): Array<String> {
        val m1: Array<String>

        m1 = if (!msg.startsWith(":") && first) {
            val index = msg.indexOfFirst { it == ':' }
            val text = msg.substring(index, msg.length)
            text.split(splitter).toTypedArray()
        } else {
            msg.split(splitter).toTypedArray()
        }
        return m1
    }

    //Daten an ESP32 senden
    private fun sendBTMessage(send: String) {
        try {
            mSocket!!.outputStream.write(send.toByteArray())
        } catch (e: IOException) {
            e.printStackTrace()
            toast(e.localizedMessage!!)
            mSocket = null
            finish()
        }
    }

    private fun dataStart() {
        if (receivingData) {
            mRunnable = Runnable {
                mHandler.postDelayed(mRunnable, 100)
                //Zufällig linke oder rechte Hand auswählen
                if(isChanged) {
                    val randomHand = (1..2).random()

                    if(randomHand == leftHand){
                        ivLeftHand.setColorFilter(resources.getColor(R.color.orange))
                        ivRightHand.setColorFilter(resources.getColor(R.color.black))
                    }
                    else if(randomHand == rightHand){
                        ivRightHand.setColorFilter(resources.getColor(R.color.orange))
                        ivLeftHand.setColorFilter(resources.getColor(R.color.black))
                    }

                    isChanged = false
                }
                readBuffer()
            }
            mHandler.postDelayed(mRunnable, 100)
        } else {
            if (mRunnable != null) {
                mHandler.removeCallbacks(mRunnable)
            }
        }
    }

}