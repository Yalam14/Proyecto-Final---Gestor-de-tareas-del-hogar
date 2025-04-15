package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HogarActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_hogar)

        val layout = findViewById<ConstraintLayout>(R.id.main)

        layout?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        val btnCrearHogar: Button = findViewById(R.id.btnCrearHogar)
        val btnUnirseHogar: Button = findViewById(R.id.btnUnirseHogar)

        btnCrearHogar.setOnClickListener {
            val intent = Intent(this, CrearHogarActivity::class.java)
            startActivity(intent)
        }

        btnUnirseHogar.setOnClickListener {
            val intent = Intent(this, UnirseActivity::class.java)
            startActivity(intent)
        }
    }
}
