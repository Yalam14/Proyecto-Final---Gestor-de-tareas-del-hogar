package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonRegistrarse = findViewById<Button>(R.id.buttonRegistrarse)
        buttonRegistrarse.setOnClickListener {
            val intent = Intent(
                this@MainActivity,
                RegistrarseActivity::class.java
            )
            startActivity(intent)
        }
    }
}