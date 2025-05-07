package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.ui.semanal

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.domain.Home
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

    private val _homeCode = MutableLiveData<String>()
    val homeCode: LiveData<String> = _homeCode

    fun loadHomeCode(homeId: String) {
        FirebaseDatabase.getInstance().getReference("homes").child(homeId).get()
            .addOnSuccessListener { snapshot ->
                val code = snapshot.getValue(Home::class.java)?.code ?: ""
                _homeCode.value = code
            }
            .addOnFailureListener {
                _homeCode.value = ""
            }
    }

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

        // Obtener el rango de fechas de la semana actual
        val (startOfWeek, endOfWeek) = getWeekRange()

        tasksRef.orderByChild("homeId").equalTo(homeId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("SemanalViewModel", "Datos recibidos. Total hijos: ${snapshot.childrenCount}")
                    Log.d("SemanalViewModel", "Rango de semana: $startOfWeek - $endOfWeek")
                    val tasksByDay = HashMap<String, MutableList<Task>>().apply {
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
                            // Filtrar por semana usando el timestamp
                            if (t.timestamp in startOfWeek..endOfWeek) {
                                t.schedule.keys.forEach { englishDay ->
                                    val spanishDay = when (englishDay.uppercase()) {
                                        "MONDAY" -> "Lunes"
                                        "TUESDAY" -> "Martes"
                                        "WEDNESDAY" -> "Miércoles"
                                        "THURSDAY" -> "Jueves"
                                        "FRIDAY" -> "Viernes"
                                        "SATURDAY" -> "Sábado"
                                        "SUNDAY" -> "Domingo"
                                        else -> englishDay
                                    }

                                    if (t.schedule[englishDay]?.assignedTo?.isNotEmpty() == true) {
                                        tasksByDay[spanishDay]?.add(t)
                                        totalTasks++
                                        if (t.completed) completedTasks++
                                    }
                                }
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

    private fun getWeekRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply {
            timeInMillis = calendar.timeInMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Obtener inicio de la semana (Lunes)
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val startOfWeek = cal.timeInMillis

        // Obtener fin de la semana (Domingo)
        cal.add(Calendar.DAY_OF_WEEK, 6)
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val endOfWeek = cal.timeInMillis

        return Pair(startOfWeek, endOfWeek)
    }

    override fun onCleared() {
        super.onCleared()
        currentListener?.let { tasksRef.removeEventListener(it) }
    }
}