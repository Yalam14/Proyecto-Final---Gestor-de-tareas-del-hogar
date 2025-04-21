package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.domain.Task
import java.text.SimpleDateFormat
import java.util.*

class AgregarTareaActivity : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var tasksRef: DatabaseReference
    private lateinit var homeId: String
    private lateinit var currentUser: String

    // CheckBoxes para los días
    private lateinit var cbLunes: CheckBox
    private lateinit var cbMartes: CheckBox
    private lateinit var cbMiercoles: CheckBox
    private lateinit var cbJueves: CheckBox
    private lateinit var cbViernes: CheckBox
    private lateinit var cbSabado: CheckBox
    private lateinit var cbDomingo: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_tarea)

        // Inicializar Firebase
        database = FirebaseDatabase.getInstance()
        tasksRef = database.getReference("tasks")

        // Obtener el ID del hogar del Intent
        homeId = intent.getStringExtra("HOME_ID") ?: ""
        currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Inicializar vistas
        cbLunes = findViewById(R.id.cb_lunes)
        cbMartes = findViewById(R.id.cb_martes)
        cbMiercoles = findViewById(R.id.cb_miercoles)
        cbJueves = findViewById(R.id.cb_jueves)
        cbViernes = findViewById(R.id.cb_viernes)
        cbSabado = findViewById(R.id.cb_sabado)
        cbDomingo = findViewById(R.id.cb_domingo)

        // Configurar el botón de agregar
        val btnAgregar: Button = findViewById(R.id.btn_agregar)
        btnAgregar.setOnClickListener {
            agregarTarea()
        }
    }

    private fun agregarTarea() {
        val nombre = findViewById<EditText>(R.id.et_nombre).text.toString()
        val descripcion = findViewById<EditText>(R.id.et_descripcion).text.toString()

        // Validar campos obligatorios
        if (nombre.isEmpty() || descripcion.isEmpty()) {
            Toast.makeText(this, "Nombre y descripción son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener días seleccionados
        val diasSeleccionados = mutableListOf<String>()
        if (cbLunes.isChecked) diasSeleccionados.add("Lunes")
        if (cbMartes.isChecked) diasSeleccionados.add("Martes")
        if (cbMiercoles.isChecked) diasSeleccionados.add("Miércoles")
        if (cbJueves.isChecked) diasSeleccionados.add("Jueves")
        if (cbViernes.isChecked) diasSeleccionados.add("Viernes")
        if (cbSabado.isChecked) diasSeleccionados.add("Sábado")
        if (cbDomingo.isChecked) diasSeleccionados.add("Domingo")

        // Validar que se seleccionó al menos un día
        if (diasSeleccionados.isEmpty()) {
            Toast.makeText(this, "Selecciona al menos un día", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener la fecha actual
        val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Crear la tarea
        val nuevaTarea = Task(
            name = nombre,
            description = descripcion,
            days = diasSeleccionados,
            assignedTo = listOf("Samuel Vega", "Oscar Minarez"), // Puedes cambiarlo después
            homeId = homeId,
            createdBy = currentUser,
            creationDate = fechaActual,
            completed = false
        )

        // Guardar en Firebase
        tasksRef.push().setValue(nuevaTarea)
            .addOnSuccessListener {
                Toast.makeText(this, "Tarea agregada correctamente", Toast.LENGTH_SHORT).show()
                finish() // Cierra la actividad y regresa a TareasActivity
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al agregar tarea: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}