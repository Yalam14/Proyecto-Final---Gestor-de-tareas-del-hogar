package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.ui.semanal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.domain.Task
import java.util.*
import kotlin.collections.HashMap

class SemanalViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance()
    private val tasksRef = database.getReference("tasks")
    private var currentListener: ValueEventListener? = null
    private val calendar = Calendar.getInstance().apply {
        firstDayOfWeek = Calendar.MONDAY
    }

    private val _currentWeek = MutableLiveData<Int>().apply {
        value = calendar.get(Calendar.WEEK_OF_YEAR)
    }
    val currentWeek: LiveData<Int> = _currentWeek

    private val _tasksByDay = MutableLiveData<Map<String, List<Task>>>()
    val tasksByDay: LiveData<Map<String, List<Task>>> = _tasksByDay

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _progress = MutableLiveData<Int>(0)
    val progress: LiveData<Int> = _progress

    fun loadTasksForCurrentWeek(homeId: String) {
        loadTasksForWeek(homeId)
    }

    fun loadPreviousWeekTasks(homeId: String) {
        calendar.add(Calendar.WEEK_OF_YEAR, -1)
        _currentWeek.value = calendar.get(Calendar.WEEK_OF_YEAR)
        loadTasksForWeek(homeId)
    }

    fun loadNextWeekTasks(homeId: String) {
        calendar.add(Calendar.WEEK_OF_YEAR, 1)
        _currentWeek.value = calendar.get(Calendar.WEEK_OF_YEAR)
        loadTasksForWeek(homeId)
    }

    private fun loadTasksForWeek(homeId: String) {
        _isLoading.value = true
        currentListener?.let { tasksRef.removeEventListener(it) }

        tasksRef.orderByChild("homeId").equalTo(homeId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tasksByDay = HashMap<String, MutableList<Task>>().apply {
                        // Inicializar todos los días
                        listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo").forEach {
                            put(it, mutableListOf())
                        }
                    }

                    var totalTasks = 0
                    var completedTasks = 0

                    for (taskSnapshot in snapshot.children) {
                        val task = taskSnapshot.getValue(Task::class.java)?.apply {
                            id = taskSnapshot.key ?: ""
                        }

                        task?.let { t ->
                            t.days?.forEach { day ->
                                tasksByDay[day]?.add(t)
                                totalTasks++
                                if (t.completed) completedTasks++
                            }
                        }
                    }

                    _tasksByDay.value = tasksByDay
                    _progress.value = if (totalTasks > 0) (completedTasks * 100 / totalTasks) else 0
                    _isLoading.value = false
                }

                override fun onCancelled(error: DatabaseError) {
                    _isLoading.value = false
                }
            })
    }

    override fun onCleared() {
        super.onCleared()
        currentListener?.let { tasksRef.removeEventListener(it) }
    }
}