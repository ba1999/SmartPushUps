package com.example.smartpushups

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import splitties.toast.toast

class SmartballConnectActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ADDRESS = "device_address"
    }

    private val btnList : Button by lazy{findViewById(R.id.btnListS)}
    private val btnTraining : Button by lazy{findViewById(R.id.btnTrainingS)}

    private val listview : ListView by lazy{ findViewById(R.id.lvDevices) }
    private val btnSearch : Button by lazy{ findViewById(R.id.btnSearch) }

    private lateinit var mBluetooth: BluetoothAdapter
    private var devices = arrayListOf<String>()

    private var isStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_smartball_connect)


        listview.visibility = View.INVISIBLE


        mBluetooth = BluetoothAdapter.getDefaultAdapter()
        if(mBluetooth == null)
        {
            toast(getString(R.string.bt_not_available))
            finish()
        }


        //Starten und Stoppen der Bluetooth-Suche
        btnSearch.setOnClickListener {
            isStarted = !isStarted

            if(isStarted) {
                btnSearch.text = getString(R.string.stop_search)
                listview.visibility = View.VISIBLE
                checkBTPermission()
                getDiscoverDevices()
            }
            else {
                btnSearch.text = getString(R.string.start_search)
                listview.visibility = View.INVISIBLE
            }
        }

        //Zur Trainings-Activity wechseln
        btnTraining.setOnClickListener {
            val intent = Intent(this, TrainingActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent)
        }

        //Zur Fortschritte-Activity wechseln
        btnList.setOnClickListener {
            val intent = Intent(this, ListActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent)
        }

        listview.onItemClickListener = lvClickListener
    }

    //Zur SmartballActivity wechseln und mit dem ausgewählten Gerärt verbinden
    private val lvClickListener = AdapterView.OnItemClickListener {
        parent, view, position, id ->
        val info = (view as TextView).text.toString()
        val address = info.substring(info.length - 17)
        val intent = Intent(this@SmartballConnectActivity, SmartballActivity::class.java)
        intent.putExtra(EXTRA_ADDRESS, address)
        startActivity(intent)
    }


    override fun onResume() {
        super.onResume()
        if (!mBluetooth.isEnabled) {
            val turnBTOn = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(turnBTOn, 1)
        }

    }

    private fun checkBTPermission() {
        var permissionCheck = checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION")
        permissionCheck += checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION")
        if (permissionCheck != 0) {
            requestPermissions(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION), 1001)
        }
    }

    private fun getDiscoverDevices() {
        if (!mBluetooth.isDiscovering) { // Suche ist nicht gestartet
            mBluetooth.startDiscovery();  // starte Suche
            btnSearch.text = getString(R.string.stop_search)
            val discoverDevicesIntent = IntentFilter(BluetoothDevice.ACTION_FOUND) //auf diese Signale soll unser Broadcast Receiver filtern
            registerReceiver(mBroadcastReceiver, discoverDevicesIntent)
        } else {                        // Suche ist gestartet
            mBluetooth.cancelDiscovery(); // Stoppe suche
            unregisterReceiver(mBroadcastReceiver);
            btnSearch.text = getString(R.string.start_search)
        }
    }

    private val mBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if (action == BluetoothDevice.ACTION_FOUND) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                val deviceInfo = """${device!!.name}${device.address}""".trimIndent()


                // gefundenes Gerät der Liste hinzufügen, wenn es noch nicht aufgeführt ist
                if (!devices.contains(deviceInfo)) {
                    devices.add(deviceInfo)
                }

                // aktualisierte Liste im Listview anzeigen
                val adapt = ArrayAdapter(applicationContext, android.R.layout.simple_list_item_1, devices)
                listview.adapter = adapt
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mBroadcastReceiver)
        mBluetooth.cancelDiscovery()
    }

}