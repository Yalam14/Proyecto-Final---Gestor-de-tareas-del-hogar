package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class RegistrarseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registrarse)

        val registar: Button = findViewById(R.id.buttonRegistrarse)

        registar.setOnClickListener {
            val intent: Intent = Intent(this, HogarActivity::class.java)
            startActivity(intent)
        }
    }
}