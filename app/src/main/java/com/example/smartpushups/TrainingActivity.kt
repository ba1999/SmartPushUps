package com.example.smartpushups

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.example.smartpushups.Constants.Companion.REPSVALUE
import com.example.smartpushups.Constants.Companion.SETSVALUE
import splitties.toast.toast

class TrainingActivity : AppCompatActivity() {

    private val btnList : Button by lazy{findViewById(R.id.btnListT)}
    private val btnSmartball : Button by lazy{findViewById(R.id.btnSmartballT)}
    private val btnPushup : Button by lazy{findViewById(R.id.btnPushup)}
    private val etSets : EditText by lazy{findViewById(R.id.etSets)}
    private val etReps : EditText by lazy{findViewById(R.id.etReps)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training)

        //Zur Fortschritte-Activity wechseln
        btnList.setOnClickListener {
            val intent = Intent(this, ListActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent)
        }

        //Zur Smartball-Activity wechseln
        btnSmartball.setOnClickListener {
            val intent = Intent(this, SmartballConnectActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent)
        }

        //Die Pushup-Activity starten und die Anzahl der Sätze und der Wiederholungen übergeben
        btnPushup.setOnClickListener {
            if(etSets.text.toString().isEmpty() || etReps.text.toString().isEmpty()){
                toast(getString(R.string.emptyError))
            }
            else {
                val intent = Intent(this, PushupActivity::class.java)
                val setsValue = etSets.text.toString().toInt()
                val repsValue = etReps.text.toString().toInt()
                intent.putExtra(SETSVALUE, setsValue)
                intent.putExtra(REPSVALUE, repsValue)
                startActivity(intent)
            }
        }
    }
}