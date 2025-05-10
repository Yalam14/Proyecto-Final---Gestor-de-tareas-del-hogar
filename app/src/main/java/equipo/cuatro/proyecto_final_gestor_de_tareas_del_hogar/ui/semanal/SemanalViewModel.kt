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
    private val homesRef = database.getReference("homes")
    private var currentListener: ValueEventListener? = null
    private val calendar = Calendar.getInstance().apply {
        firstDayOfWeek = Calendar.MONDAY
    }

    private val _currentWeekDisplay = MutableLiveData<String>()
    val currentWeekDisplay: LiveData<String> = _currentWeekDisplay

    private val _tasksByDay = MutableLiveData<Map<String, List<Task>>>()
    val tasksByDay: LiveData<Map<String, List<Task>>> = _tasksByDay

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _progress = MutableLiveData<Int>(0)
    val progress: LiveData<Int> = _progress

    private val _canEdit = MutableLiveData<Boolean>()
    val canEdit: LiveData<Boolean> = _canEdit

    private val _creator = MutableLiveData<String>()
    val creator: LiveData<String> = _creator

    init {
        updateWeekDisplay()
    }

    private fun updateWeekDisplay() {
        val monthName = when (calendar.get(Calendar.MONTH)) {
            Calendar.JANUARY -> "Enero"
            Calendar.FEBRUARY -> "Febrero"
            Calendar.MARCH -> "Marzo"
            Calendar.APRIL -> "Abril"
            Calendar.MAY -> "Mayo"
            Calendar.JUNE -> "Junio"
            Calendar.JULY -> "Julio"
            Calendar.AUGUST -> "Agosto"
            Calendar.SEPTEMBER -> "Septiembre"
            Calendar.OCTOBER -> "Octubre"
            Calendar.NOVEMBER -> "Noviembre"
            Calendar.DECEMBER -> "Diciembre"
            else -> ""
        }
        val weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH)
        _currentWeekDisplay.value = "$monthName Semana $weekOfMonth"
    }

    fun loadEditPermissions(homeId: String) {
        homesRef.child(homeId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _canEdit.value = snapshot.child("canParticipantEdit").getValue(Boolean::class.java) ?: false
                _creator.value = snapshot.child("createdBy").getValue(String::class.java) ?: ""
            }

            override fun onCancelled(error: DatabaseError) {
                _canEdit.value = false
                _creator.value = ""
            }
        })
    }

    fun loadTasksForCurrentWeek(homeId: String) {
        loadTasksForWeek(homeId)
    }

    fun loadPreviousWeekTasks(homeId: String) {
        calendar.add(Calendar.WEEK_OF_YEAR, -1)
        updateWeekDisplay()
        loadTasksForWeek(homeId)
    }

    fun loadNextWeekTasks(homeId: String) {
        calendar.add(Calendar.WEEK_OF_YEAR, 1)
        updateWeekDisplay()
        loadTasksForWeek(homeId)
    }

    private fun loadTasksForWeek(homeId: String) {
        _isLoading.value = true
        currentListener?.let { tasksRef.removeEventListener(it) }

        val weekDates = getWeekDates()

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
                            weekDates.forEach { (date, dayName) ->
                                val englishDay = when (dayName) {
                                    "Lunes" -> "MONDAY"
                                    "Martes" -> "TUESDAY"
                                    "Miércoles" -> "WEDNESDAY"
                                    "Jueves" -> "THURSDAY"
                                    "Viernes" -> "FRIDAY"
                                    "Sábado" -> "SATURDAY"
                                    "Domingo" -> "SUNDAY"
                                    else -> null
                                }

                                // Verificar si es tarea recurrente o para fecha específica
                                val isRecurrent = englishDay?.let { t.schedule.containsKey(it) } ?: false
                                val isSpecificDate = t.schedule.containsKey(date)

                                if (isRecurrent || isSpecificDate) {
                                    // Asignar miembros específicos para este día
                                    val assignedMembers = when {
                                        t.schedule.containsKey(date) -> t.schedule[date]?.assignedTo ?: emptyList()
                                        englishDay?.let { t.schedule.containsKey(it) } ?: false ->
                                            t.schedule[englishDay]?.assignedTo ?: emptyList()
                                        else -> emptyList()
                                    }

                                    // Crear copia de la tarea con miembros asignados específicos
                                    val taskCopy = t.copy().apply {
                                        this.assignedMembersForDay = assignedMembers
                                    }

                                    tasksByDay[dayName]?.add(taskCopy)
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

    private fun calculateProgress(tasksByDay: Map<String, List<Task>>) {
        val totalTasks = tasksByDay.values.sumOf { it.size }
        val completedTasks = tasksByDay.values.flatMap { it }.count { it.completed }

        _progress.value = if (totalTasks > 0) {
            (completedTasks.toFloat() / totalTasks.toFloat() * 100).toInt()
        } else {
            0
        }
    }

    fun getWeekDates(): List<Pair<String, String>> {
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

    override fun onCleared() {
        super.onCleared()
        currentListener?.let { tasksRef.removeEventListener(it) }
    }
}