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
import java.text.SimpleDateFormat
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

        // Obtener todas las fechas de la semana actual
        val weekDates = getWeekDates()

        currentListener = tasksRef.orderByChild("homeId").equalTo(homeId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tasksByDay = HashMap<String, MutableList<Task>>().apply {
                        // Inicializar con los días de la semana
                        listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo").forEach {
                            put(it, mutableListOf())
                        }
                    }

                    for (taskSnapshot in snapshot.children) {
                        val task = taskSnapshot.getValue(Task::class.java)?.apply {
                            id = taskSnapshot.key ?: ""
                        }

                        task?.let { t ->
                            // Verificar para cada día de la semana
                            weekDates.forEachIndexed { index, (date, dayName) ->
                                val isScheduled = when {
                                    // Formato por fecha (2025-05-08)
                                    t.schedule.containsKey(date) -> true

                                    // Formato por día de semana (MONDAY)
                                    else -> {
                                        val englishDay = when (dayName) {
                                            "Lunes" -> "MONDAY"
                                            "Martes" -> "TUESDAY"
                                            "Miércoles" -> "WEDNESDAY"
                                            "Jueves" -> "THURSDAY"
                                            "Viernes" -> "FRIDAY"
                                            "Sábado" -> "SATURDAY"
                                            "Domingo" -> "SUNDAY"
                                            else -> ""
                                        }
                                        t.schedule.containsKey(englishDay)
                                    }
                                }

                                if (isScheduled) {
                                    tasksByDay[dayName]?.add(t)
                                }
                            }
                        }
                    }

                    _tasksByDay.value = tasksByDay
                    _isLoading.value = false
                }

                override fun onCancelled(error: DatabaseError) {
                    _isLoading.value = false
                }
            })
    }

    private fun getWeekDates(): List<Pair<String, String>> {
        val dates = mutableListOf<Pair<String, String>>()
        val cal = Calendar.getInstance().apply {
            // Ajustar al inicio de la semana (Lunes)
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayNameFormat = SimpleDateFormat("EEEE", Locale.getDefault())

        // Para cada día de la semana (Lunes a Domingo)
        repeat(7) {
            val date = dateFormat.format(cal.time)
            val dayName = when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "Lunes"
                Calendar.TUESDAY -> "Martes"
                Calendar.WEDNESDAY -> "Miércoles"
                Calendar.THURSDAY -> "Jueves"
                Calendar.FRIDAY -> "Viernes"
                Calendar.SATURDAY -> "Sábado"
                Calendar.SUNDAY -> "Domingo"
                else -> ""
            }
            dates.add(date to dayName)
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        return dates
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