package com.example.smartpushups

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.example.smartpushups.Constants.Companion.CHECK
import com.example.smartpushups.Constants.Companion.REPSRESULT
import com.example.smartpushups.Constants.Companion.REPSVALUE
import com.example.smartpushups.Constants.Companion.SETSRESULT
import com.example.smartpushups.Constants.Companion.SETSVALUE
import splitties.alertdialog.alertDialog
import splitties.alertdialog.negativeButton
import splitties.alertdialog.okButton
import splitties.alertdialog.positiveButton
import splitties.toast.toast

class PushupActivity : AppCompatActivity() {

    private val tvCounter : TextView by lazy{ findViewById(R.id.tvCounter) }
    private val tvSetsCounter : TextView by lazy{ findViewById(R.id.tvSetsCounter)}
    private val tvRepsCounter : TextView by lazy{ findViewById(R.id.tvRepsCounter)}
    private val btnStart : Button by lazy{ findViewById(R.id.btnStart) }
    private val pbSets : ProgressBar by lazy{findViewById(R.id.pbSets)}
    private val pbReps : ProgressBar by lazy{findViewById(R.id.pbReps)}

    private val sensorManager : SensorManager by lazy { applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    private val lightSensor : Sensor by lazy { sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) }
    private lateinit var tonePlayer : ToneGenerator

    private var repsOverallCounter = 0
    private var repsCounter = 0
    private var setsCounter = 0
    private var prevValue = 0f
    private var isStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pushup)

        //Prüfen ob Lichtsensor verfügbar
        if(lightSensor != null){
            toast(getString(R.string.light_available))
        }
        else{
            toast(getString(R.string.light_not_available))
            finish()
        }

        tonePlayer = ToneGenerator(AudioManager.STREAM_MUSIC, 100)

        val intent = intent
        val setsMax = intent.getIntExtra(SETSVALUE, 0)
        val repsMax = intent.getIntExtra(REPSVALUE, 0)

        pbSets.max = setsMax
        pbSets.progress = 0
        pbReps.max = repsMax
        pbReps.progress = 0





        btnStart.setOnClickListener {
            //Training starten
            if(!isStarted) {
                isStarted = true
                btnStart.text = getString(R.string.pause)
                btnStart.setBackgroundColor(getColor(R.color.btn_orange))
            }
            //Training pausieren
            else {
                isStarted = false
                btnStart.text = getString(R.string.keepgoing)
                btnStart.setBackgroundColor(getColor(R.color.green))
                tonePlayer.stopTone()

                //Trainingsergebnisse anzeigen
                alertDialog (title = getString(R.string.result),
                message = getString(R.string.trainingpause) + getString(R.string.save_sets_reps, setsCounter, repsOverallCounter )){
                    positiveButton(R.string.keepgoing){
                        isStarted = true
                        btnStart.text = getString(R.string.pause)
                        btnStart.setBackgroundColor(getColor(R.color.btn_orange))
                    }
                    negativeButton(R.string.save){
                        changeActivitynotFinisched()
                    }

                }.show()
            }
        }


        lightEvent()

    }

    //Funktion für das Messen und Auswerten der Helligkeit
    private fun lightEvent() {

        val lightListener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {


            }
            override fun onSensorChanged(event: SensorEvent) {



                if (isStarted) {
                    if (event.values[2] < 0.5f && prevValue > 0.5f) {

                        //Wiederholungen hochzählen
                        repsCounter++
                        repsOverallCounter++
                        tonePlayer.startTone(ToneGenerator.TONE_CDMA_PIP, 150)

                        pbReps.progress = repsCounter
                        tvCounter.text = repsCounter.toString()
                        tvRepsCounter.text = getString(R.string.repsCount, repsOverallCounter)

                        if(pbReps.max == repsCounter){
                            //Sätze hochzählen
                            setsCounter++
                            pbSets.progress = setsCounter
                            tvSetsCounter.text = getString(R.string.setsCount, setsCounter)
                            repsCounter = 0

                            //Training nach einem Satz pausieren
                            isStarted = false
                            btnStart.text = getString(R.string.keepgoing)
                            btnStart.setBackgroundColor(getColor(R.color.green))
                            tonePlayer.stopTone()


                            if(pbSets.max == setsCounter){
                                isStarted = false
                                btnStart.text = getString(R.string.start)
                                tonePlayer.stopTone()
                                setsCounter = 0

                                changeActivity()
                            }


                            //Trainingsergebnisse anzeigen
                            alertDialog (title = getString(R.string.result),
                                message = getString(R.string.trainingpause) + getString(R.string.save_sets_reps, setsCounter, repsOverallCounter )){
                                positiveButton(R.string.keepgoing){
                                    isStarted = true
                                    btnStart.text = getString(R.string.pause)
                                    btnStart.setBackgroundColor(getColor(R.color.btn_orange))
                                }
                                negativeButton(R.string.save){
                                    changeActivitynotFinisched()
                                }

                            }.show()
                        }
                    }

                    prevValue = event.values[2]
                }
            }
        }

        sensorManager.registerListener(lightListener, lightSensor, SensorManager.SENSOR_DELAY_FASTEST)
    }

    //Activity zu ListActivity wechseln, wenn Training absolviert ist
    fun changeActivity(){
        val intent = Intent(this, ListActivity::class.java)
        val setsResult = pbSets.max
        val repsResult = repsOverallCounter
        val check = 1
        intent.putExtra(CHECK, check)
        intent.putExtra(SETSRESULT, setsResult)
        intent.putExtra(REPSRESULT, repsResult)
        startActivity(intent)
    }

    //Activity zu ListActivity wechseln, wenn Training noch nicht absolviert ist
    fun changeActivitynotFinisched(){
        val intent = Intent(this, ListActivity::class.java)
        val setsResult = setsCounter
        val repsResult = repsOverallCounter
        val check = 1
        intent.putExtra(CHECK, check)
        intent.putExtra(SETSRESULT, setsResult)
        intent.putExtra(REPSRESULT, repsResult)
        startActivity(intent)
    }
    
}