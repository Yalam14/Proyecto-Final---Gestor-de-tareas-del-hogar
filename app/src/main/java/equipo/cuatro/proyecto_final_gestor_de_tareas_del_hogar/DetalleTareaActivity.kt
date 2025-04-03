package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DetalleTareaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detalle_tarea)

        // Configuración estática de ejemplo
        val tvTitulo = findViewById<TextView>(R.id.tvTitulo)
        val tvDescripcion = findViewById<TextView>(R.id.et_descripcion)
        val btnEliminar = findViewById<Button>(R.id.btnEliminar)
        val btnTerminar = findViewById<Button>(R.id.btnTerminar)
        val btnEditar = findViewById<ImageButton>(R.id.btnEditar)

        // Datos de ejemplo
        tvTitulo.text = "Limpiar la cocina "
        tvDescripcion.text = "- Barrer, trapear, limpiar los platos, el refrigerador y la estufa."



        // Configuración visual de botones
        btnTerminar.setOnClickListener {
            btnTerminar.text = "Completado"
            btnTerminar.isEnabled = false
            btnTerminar.backgroundTintList = getColorStateList(android.R.color.darker_gray)
        }

        btnEliminar.setOnClickListener { finish() }
        btnEditar.setOnClickListener { /* Simular edición sin funcionalidad */ }
    }
}