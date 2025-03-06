package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DetalleTareaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detalle_tarea)

        val taskName = intent.getStringExtra("taskName") ?: "Sin título"
        val assignedTo = intent.getStringExtra("assignedTo") ?: "Nadie"
        val completed = intent.getBooleanExtra("completed", false)

        val textViewTitulo = findViewById<TextView>(R.id.textViewTituloTarea)
        val textViewAsignados = findViewById<TextView>(R.id.textViewAsignados)
        val textViewEstado = findViewById<TextView>(R.id.textViewEstado)
        val editTextDescripcion = findViewById<EditText>(R.id.editTextDescripcion)
        val btnEliminar = findViewById<Button>(R.id.btnEliminar)
        val btnTerminar = findViewById<Button>(R.id.btnTerminar)

        textViewTitulo.text = taskName
        textViewAsignados.text = "Asignado a: $assignedTo"
        textViewEstado.text = if (completed) "Estado: Completada" else "Estado: Pendiente"

        btnTerminar.setOnClickListener {
            textViewEstado.text = "Estado: Completada"
        }

        btnEliminar.setOnClickListener {
            finish()
        }
    }
}
