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

    fun cargarTareasParaDiaActual(homeId: String) {
        cargarTareasParaDia(obtenerNombreDia(calendario), homeId)
    }

    fun cargarTareasDiaAnterior(homeId: String) {
        calendario.add(Calendar.DAY_OF_YEAR, -1)
        actualizarFecha(calendario)
        cargarTareasParaDia(obtenerNombreDia(calendario), homeId)
    }

    fun cargarTareasDiaSiguiente(homeId: String) {
        calendario.add(Calendar.DAY_OF_YEAR, 1)
        actualizarFecha(calendario)
        cargarTareasParaDia(obtenerNombreDia(calendario), homeId)
    }

    fun cargarTareasParaDia(dia: String, homeId: String) {
        _estaCargando.value = true

        // Remover listener anterior para evitar duplicados
        listenerActual?.let { referenciaTareas.removeEventListener(it) }

        // Normalizar nombre del día
        val diaNormalizado = normalizarNombreDia(dia)
        Log.d("DiarioViewModel", "Buscando tareas para día: $diaNormalizado, homeId: $homeId")

        listenerActual = referenciaTareas.orderByChild("homeId").equalTo(homeId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("DiarioViewModel", "Recibidos datos de Firebase")
                    val listaTareas = mutableListOf<Task>()

                    for (tareaSnapshot in snapshot.children) {
                        try {
                            val tarea = tareaSnapshot.getValue(Task::class.java)?.apply {
                                id = tareaSnapshot.key ?: ""
                            }

                            if (tarea != null) {
                                Log.d("DiarioViewModel", "Tarea encontrada: ${tarea.name}, Días: ${tarea.days}")
                                if (tarea.days.any { normalizarNombreDia(it) == diaNormalizado }) {
                                    listaTareas.add(tarea)
                                    Log.d("DiarioViewModel", "Tarea añadida: ${tarea.name}")
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("DiarioViewModel", "Error al parsear tarea: ${e.message}")
                        }
                    }

                    _tareas.value = listaTareas.sortedByDescending { it.timestamp }
                    _estaCargando.value = false
                    Log.d("DiarioViewModel", "Tareas cargadas: ${listaTareas.size}")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("DiarioViewModel", "Error en Firebase: ${error.message}")
                    _tareas.value = emptyList()
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
    }
    private fun obtenerNombreDia(calendario: Calendar): String {
        val dias = listOf("Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
        return dias[calendario.get(Calendar.DAY_OF_WEEK) - 1]
    }

    override fun onCleared() {
        super.onCleared()
        listenerActual?.let { referenciaTareas.removeEventListener(it) }
    }
    private fun actualizarListaTareas(tareas: List<Task>) {
        Log.d("DiarioFragment", "Mostrando ${tareas.size} tareas")
        tareas.forEach { tarea ->
            Log.d("DiarioFragment", "Tarea: ${tarea.name}, Días: ${tarea.days}")
        }
    }
}