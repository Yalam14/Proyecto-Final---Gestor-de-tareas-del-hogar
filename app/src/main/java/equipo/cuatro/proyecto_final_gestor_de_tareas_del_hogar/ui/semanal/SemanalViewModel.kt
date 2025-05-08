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

    private val _year = MutableLiveData<Int>().apply {
        value = calendar.get(Calendar.YEAR)
    }
    val year: LiveData<Int> = _year

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

        val weekDates = getWeekDates()
        Log.d("SemanalVM", "Fechas de la semana: $weekDates")

        currentListener = tasksRef.orderByChild("homeId").equalTo(homeId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tasksByDay = HashMap<String, MutableList<Task>>().apply {
                        listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo").forEach {
                            put(it, mutableListOf())
                        }
                    }

                    for (taskSnapshot in snapshot.children) {
                        val task = taskSnapshot.getValue(Task::class.java)?.apply {
                            id = taskSnapshot.key ?: ""
                        }

                        task?.let { t ->
                            Log.d("SemanalVM", "Procesando tarea: ${t.name}")
                            Log.d("SemanalVM", "Schedule keys: ${t.schedule.keys}")

                            weekDates.forEach { (date, dayName) ->
                                val englishDay = convertToEnglishDay(dayName)

                                // Verificar si es una tarea recurrente para este día
                                val isRecurrent = englishDay?.let { day ->
                                    t.schedule.containsKey(day) && isRecurrentTask(t)
                                } ?: false

                                // Verificar si es una tarea específica para esta fecha
                                val isSpecificDate = t.schedule.containsKey(date)

                                if (isRecurrent || isSpecificDate) {
                                    tasksByDay[dayName]?.add(t)
                                    Log.d("SemanalVM", "Añadida tarea a $dayName: ${t.name}")
                                }
                            }
                        }
                    }

                    _tasksByDay.value = tasksByDay
                    calculateProgress(tasksByDay)
                    _isLoading.value = false
                }

                override fun onCancelled(error: DatabaseError) {
                    _isLoading.value = false
                    Log.e("SemanalVM", "Error loading tasks", error.toException())
                }
            })
    }

    private fun isRecurrentTask(task: Task): Boolean {
        // Una tarea es recurrente si tiene al menos un día de la semana en inglés
        val englishDays = listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY",
            "FRIDAY", "SATURDAY", "SUNDAY")
        return task.schedule.keys.any { it in englishDays }
    }

    private fun convertToEnglishDay(spanishDay: String): String? {
        return when (spanishDay) {
            "Lunes" -> "MONDAY"
            "Martes" -> "TUESDAY"
            "Miércoles" -> "WEDNESDAY"
            "Jueves" -> "THURSDAY"
            "Viernes" -> "FRIDAY"
            "Sábado" -> "SATURDAY"
            "Domingo" -> "SUNDAY"
            else -> null
        }
    }

    private fun getWeekDates(): List<Pair<String, String>> {
        val dates = mutableListOf<Pair<String, String>>()
        val cal = calendar.clone() as Calendar
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

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

    private fun calculateProgress(tasksByDay: Map<String, List<Task>>) {
        val totalTasks = tasksByDay.values.sumOf { it.size }
        val completedTasks = tasksByDay.values.flatMap { it }.count { it.completed }

        _progress.value = if (totalTasks > 0) {
            (completedTasks.toFloat() / totalTasks.toFloat() * 100).toInt()
        } else {
            0
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentListener?.let { tasksRef.removeEventListener(it) }
    }
}