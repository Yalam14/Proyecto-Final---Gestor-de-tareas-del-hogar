package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.UnirseActivity
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.domain.Home
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.domain.Task
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.domain.User
import kotlinx.coroutines.awaitAll
import org.checkerframework.checker.units.qual.s
import java.text.SimpleDateFormat
import java.util.*
import kotlin.jvm.java

class AgregarTareaActivity : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var tasksRef: DatabaseReference
    private lateinit var usersRef: DatabaseReference
    private lateinit var homeRef: DatabaseReference
    private lateinit var homeId: String
    private lateinit var currentUser: String

    private lateinit var containerDias: LinearLayout
    private lateinit var templateDia: LinearLayout
    private lateinit var templateMiembro: LinearLayout
    private var miembrosHogar = mutableListOf<String>()
    private val asignacionesPorDia = mutableMapOf<String, MutableList<String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_tarea)

        database = FirebaseDatabase.getInstance()
        tasksRef = database.getReference("tasks")
        usersRef = database.getReference("users")
        homeRef = database.getReference("homes")

        homeId = intent.getStringExtra("HOME_ID") ?: ""
        currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        containerDias = findViewById(R.id.container_dias)
        templateDia = findViewById(R.id.template_dia)
        templateMiembro = findViewById(R.id.template_miembro)

        templateDia.visibility = View.GONE
        templateMiembro.visibility = View.GONE

        agregarMiembrosHogar()

        val diasSemana = listOf(
            "Lunes" to R.string.lunes,
            "Martes" to R.string.martes,
            "Miércoles" to R.string.miercoles,
            "Jueves" to R.string.jueves,
            "Viernes" to R.string.viernes,
            "Sábado" to R.string.sabado,
            "Domingo" to R.string.domingo
        )

        diasSemana.forEach { (diaKey, diaStringRes) ->
            val diaView = LayoutInflater.from(this).inflate(
                R.layout.template_dia,
                containerDias,
                false
            ) as LinearLayout

            val cbDia = diaView.findViewById<CheckBox>(R.id.template_cb_dia)
            val layoutMiembros = diaView.findViewById<LinearLayout>(R.id.template_layout_miembros)

            cbDia.text = getString(diaStringRes)

            cbDia.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    mostrarMiembrosParaDia(diaKey, layoutMiembros)
                } else {
                    layoutMiembros.visibility = View.GONE
                    asignacionesPorDia.remove(diaKey)
                }
            }

            containerDias.addView(diaView)
        }


        val btnAgregar: Button = findViewById(R.id.btn_agregar)
        btnAgregar.setOnClickListener {
            agregarTarea()
        }
    }

    private fun agregarTarea() {
        val nombre = findViewById<EditText>(R.id.et_nombre).text.toString()
        val descripcion = findViewById<EditText>(R.id.et_descripcion).text.toString()

        if (nombre.isEmpty() || descripcion.isEmpty()) {
            Toast.makeText(this, "Nombre y descripción son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        if (asignacionesPorDia.isEmpty()) {
            Toast.makeText(this, "Selecciona al menos un día", Toast.LENGTH_SHORT).show()
            return
        }

        val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val timestamp = System.currentTimeMillis()

        val diasConAsignaciones = asignacionesPorDia.mapValues { (_, miembros) ->
            miembros.toList()
        }

        val nuevaTarea = Task(
            name = nombre,
            description = descripcion,
            days = diasConAsignaciones,
            homeId = homeId,
            createdBy = currentUser,
            creationDate = fechaActual,
            timestamp = timestamp,
            completed = false
        )

        tasksRef.push().setValue(nuevaTarea)
            .addOnSuccessListener {
                Toast.makeText(this, "Tarea agregada correctamente", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al agregar tarea: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarMiembrosParaDia(dia: String, container: LinearLayout) {
        container.removeAllViews() // Limpiar contenedor

        miembrosHogar.forEach { miembro ->
            val miembroView = LayoutInflater.from(this).inflate(
                R.layout.template_miembro,
                container,
                false
            ) as LinearLayout

            val tvMiembro = miembroView.findViewById<TextView>(R.id.template_tv_miembro)
            val switchMiembro = miembroView.findViewById<MaterialSwitch>(R.id.template_switch_miembro)

            tvMiembro.text = miembro
            switchMiembro.isChecked = asignacionesPorDia[dia]?.contains(miembro) ?: false

            switchMiembro.setOnCheckedChangeListener { _, isChecked ->
                val miembrosDia = asignacionesPorDia.getOrPut(dia) { mutableListOf() }
                if (isChecked) {
                    if (!miembrosDia.contains(miembro)) {
                        miembrosDia.add(miembro)
                    }
                } else {
                    miembrosDia.remove(miembro)
                }
            }

            container.addView(miembroView)
        }

        container.visibility = View.VISIBLE
    }

    private fun View.clone(): View {
        val inflater = LayoutInflater.from(context)
        return when (this) {
            is LinearLayout -> inflater.inflate(R.layout.template_dia, null) as LinearLayout
            else -> inflater.inflate(R.layout.template_miembro, null)
        }
    }

    fun agregarMiembrosHogar() {
        homeRef.child(homeId).get().addOnSuccessListener { homeSnapshot ->
            if (homeSnapshot.exists()) {
                val home = homeSnapshot.getValue(Home::class.java)
                home?.let {
                    miembrosHogar = home.participants.values.toMutableList()
                    Log.e("MIEMBROS", miembrosHogar.toString())
                } ?: Toast.makeText(this, "Hogar no encontrado", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Hogar no encontrado", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error al cargar información del hogar", Toast.LENGTH_SHORT).show()
        }
    }
}