package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.ui.diario

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.domain.Task
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DiarioViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance()
    private val tasksRef = database.getReference("tasks")
    private var currentListener: ValueEventListener? = null
    private val calendar = Calendar.getInstance()

    private val _currentDay = MutableLiveData<String>().apply {
        value = getCurrentDay()
    }
    val currentDay: LiveData<String> = _currentDay

    private val _tasks = MutableLiveData<List<Task>>()
    val tasks: LiveData<List<Task>> = _tasks

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Método para cargar tareas del día actual
    fun loadTasksForCurrentDay(homeId: String) {
        loadTasksForDay(getCurrentDay(), homeId)
    }

    // Método para día anterior
    fun loadPreviousDayTasks(homeId: String) {
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        updateCurrentDay()
        loadTasksForDay(getCurrentDay(), homeId)
    }

    // Método para día siguiente
    fun loadNextDayTasks(homeId: String) {
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        updateCurrentDay()
        loadTasksForDay(getCurrentDay(), homeId)
    }

    private fun loadTasksForDay(day: String, homeId: String) {
        _isLoading.value = true
        currentListener?.let { tasksRef.removeEventListener(it) }

        val newListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val taskList = mutableListOf<Task>()
                for (taskSnapshot in snapshot.children) {
                    val task = taskSnapshot.getValue<Task>()?.apply {
                        id = taskSnapshot.key ?: ""
                    }
                    task?.takeIf { it.homeId == homeId && it.days.contains(day) }?.let {
                        taskList.add(it)
                    }
                }
                _tasks.value = taskList
                _isLoading.value = false
            }

            override fun onCancelled(error: DatabaseError) {
                _isLoading.value = false
            }
        }

        currentListener = newListener
        tasksRef.addValueEventListener(newListener)
    }

    private fun updateCurrentDay() {
        _currentDay.value = getCurrentDay()
    }

    private fun getCurrentDay(): String {
        val days = listOf("Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
        return days[calendar.get(Calendar.DAY_OF_WEEK) - 1]
    }

    override fun onCleared() {
        super.onCleared()
        currentListener?.let { tasksRef.removeEventListener(it) }
    }
}