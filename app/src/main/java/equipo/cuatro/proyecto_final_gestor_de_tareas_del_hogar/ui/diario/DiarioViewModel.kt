package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.ui.diario

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.domain.Task
import java.util.*

class DiarioViewModel : ViewModel() {
    private val baseDatos = FirebaseDatabase.getInstance()
    private val referenciaTareas = baseDatos.getReference("tasks")
    private var listenerActual: ValueEventListener? = null
    private val calendario = Calendar.getInstance()

    private val _diaActual = MutableLiveData<String>()
    val diaActual: LiveData<String> = _diaActual

    private val _tareas = MutableLiveData<List<Task>?>()
    val tareas: LiveData<List<Task>?> = _tareas

    private val _estaCargando = MutableLiveData<Boolean>()
    val estaCargando: LiveData<Boolean> = _estaCargando

    init {
        actualizarFecha(calendario)
    }

    fun actualizarFecha(calendario: Calendar) {
        this.calendario.timeInMillis = calendario.timeInMillis
        _diaActual.value = obtenerNombreDia(this.calendario)
    }

    fun cargarTareasParaDia(dia: String, homeId: String) {
        _estaCargando.value = true
        listenerActual?.let { referenciaTareas.removeEventListener(it) }

        val diaNormalizado = normalizarNombreDia(dia)
        Log.d("DiarioVM", "Consultando tareas para homeId: $homeId, día: $diaNormalizado")

        listenerActual = referenciaTareas
            .orderByChild("homeId")
            .equalTo(homeId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val listaTareas = mutableListOf<Task>()

                    for (tareaSnapshot in snapshot.children) {
                        try {
                            val tarea = tareaSnapshot.getValue(Task::class.java)?.apply {
                                id = tareaSnapshot.key ?: ""
                            }

                            tarea?.takeIf {
                                it.days.any { day ->
                                    normalizarNombreDia(day) == diaNormalizado
                                }
                            }?.let { listaTareas.add(it) }
                        } catch (e: Exception) {
                            Log.e("DiarioVM", "Error al parsear tarea", e)
                        }
                    }

                    _tareas.value = listaTareas.sortedByDescending { it.timestamp }
                    _estaCargando.value = false
                    Log.d("DiarioVM", "Tareas encontradas: ${listaTareas.size}")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("DiarioVM", "Error en Firebase", error.toException())
                    _tareas.value = null
                    _estaCargando.value = false
                }
            })
    }

    private fun normalizarNombreDia(dia: String): String {
        return dia.lowercase()
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
            .replace("miercoles", "miércoles")
            .replace("sabado", "sábado")
    }

    private fun obtenerNombreDia(calendario: Calendar): String {
        return listOf("Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")[
            calendario.get(Calendar.DAY_OF_WEEK) - 1
        ]
    }

    override fun onCleared() {
        super.onCleared()
        listenerActual?.let { referenciaTareas.removeEventListener(it) }
    }
}