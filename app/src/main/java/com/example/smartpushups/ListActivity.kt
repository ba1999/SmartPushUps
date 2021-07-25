package com.example.smartpushups

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.core.view.contains
import androidx.core.view.get
import androidx.core.view.size
import com.example.smartpushups.Constants.Companion.CHECK
import com.example.smartpushups.Constants.Companion.REPSRESULT
import com.example.smartpushups.Constants.Companion.SETSRESULT
import splitties.alertdialog.alertDialog
import splitties.alertdialog.negativeButton
import splitties.alertdialog.positiveButton
import java.text.SimpleDateFormat
import java.util.*

class ListActivity : AppCompatActivity() {

    private val btnTraining : Button by lazy{findViewById(R.id.btnTrainingL)}
    private val btnSmartball : Button by lazy{findViewById(R.id.btnSmartballL)}
    private val lvResult : ListView by lazy { findViewById(R.id.lvResult) }

    private var setsResult = 0
    private var repsResult = 0


    private var training = arrayListOf<String>()
    private lateinit var adapt : ArrayAdapter <String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        //Abgerufene Trainingseinträge wieder in die ListView hinzufügen
        adapt = ArrayAdapter<String>(applicationContext, android.R.layout.simple_list_item_1, training )
        lvResult.adapter = adapt

        //Trainingsergebnisse abrufen
        val intent = intent
        val check = intent.getIntExtra(CHECK,0)
        setsResult = intent.getIntExtra(SETSRESULT, 0)
        repsResult = intent.getIntExtra(REPSRESULT, 0)

        //Trainingsergebnisse anzeigen
        if(check == 1){
            Thread.sleep(1000)
            alertDialog (title = getString(R.string.result),
                message = getString(R.string.save_sets_reps, setsResult, repsResult ))
            {
                //Trainingsergebnisse speichern
                positiveButton(R.string.save){
                    training.add(getString(R.string.training_counter) + getString(R.string.save_sets_reps, setsResult, repsResult ))
                    adapt = ArrayAdapter<String>(applicationContext, android.R.layout.simple_list_item_1, training )
                    lvResult.adapter = adapt
                }
                //Trainingsergebnisse verwerfen
                negativeButton(R.string.stop){
                    toTrainingActivity()
                }

            }.show()
        }

        //Zur Trainings-Activity wechseln
        btnTraining.setOnClickListener {
            val intent = Intent(this, TrainingActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }

        //Zur SmartballConnect-Activity wechseln
        btnSmartball.setOnClickListener {
            val intent = Intent(this, SmartballConnectActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }

        //Trainingseintrag löschen
        lvResult.setOnItemClickListener { adapterView, view, i, l ->
            alertDialog(
                    title = getString(R.string.delete_result),
                    message = lvResult.getItemAtPosition(i).toString()) {
                positiveButton(R.string.delete) {
                    training.removeAt(i)
                    adapt.notifyDataSetChanged()

                }
                negativeButton(R.string.cancel){
                }
            }.show()
        }
    }

    //Zwischenspeichern der Trainingseinträge des Listviews
    override fun onPause() {
        super.onPause()

        val sp = getPreferences(Context.MODE_PRIVATE)
        val edit = sp.edit()

        edit.putInt(Constants.ANZAHL, training.size)
        for(i in 0 until training.size){
            edit.putString("Result_$i", training[i])
        }
        edit.commit()


    }

    //Wiederabrufen der Trainingseinträge des Listviews
    override fun onResume() {
        super.onResume()

        val sp = getPreferences(Context.MODE_PRIVATE)
        val anzahl = sp.getInt(Constants.ANZAHL, 0)

        for(i in 0 until anzahl){
            val t = sp.getString("Result_$i", "").toString()
            training.add(t)

        }


    }

    //Zur Trainins-Activity wechseln
    fun toTrainingActivity(){
        val intent = Intent(this, TrainingActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
    }
}