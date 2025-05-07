package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.ui.diario

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

class DiarioViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance()
    private val tasksRef = database.getReference("tasks")
    private val homesRef = database.getReference("homes")
    private var currentListener: ValueEventListener? = null

    private val calendar = Calendar.getInstance().apply {
        // Configurar para empezar la semana en lunes
        firstDayOfWeek = Calendar.MONDAY
    }

    private val _tasks = MutableLiveData<List<Task>>()
    val tasks: LiveData<List<Task>> = _tasks

    private val _currentDay = MutableLiveData<String>().apply {
        value = getFormattedCurrentDate()
    }
    val currentDay: LiveData<String> = _currentDay

    private val _currentDayName = MutableLiveData<String>().apply {
        value = getDayName(calendar.get(Calendar.DAY_OF_WEEK))
    }
    val currentDayName: LiveData<String> = _currentDayName

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _homeCode = MutableLiveData<String>()
    val homeCode: LiveData<String> = _homeCode

    fun loadHomeCode(homeId: String) {
        _isLoading.value = true
        homesRef.child(homeId).get()
            .addOnSuccessListener { snapshot ->
                _homeCode.value = snapshot.getValue(Home::class.java)?.code ?: ""
                _isLoading.value = false
            }
            .addOnFailureListener {
                _homeCode.value = ""
                _isLoading.value = false
            }
    }

    fun loadTasksForCurrentDay(homeId: String) {
        _isLoading.value = true
        currentListener?.let { tasksRef.removeEventListener(it) }

        val currentDateFormatted = _currentDay.value ?: return
        val currentDayNameValue = _currentDayName.value ?: ""

        currentListener = tasksRef.orderByChild("homeId").equalTo(homeId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tasksList = mutableListOf<Task>()

                    for (taskSnapshot in snapshot.children) {
                        val task = taskSnapshot.getValue(Task::class.java)?.apply {
                            id = taskSnapshot.key ?: ""
                        }

                        task?.let { t ->
                            val isScheduled = when {
                                t.schedule.containsKey(currentDateFormatted) -> true
                                else -> {
                                    val englishDay = when (currentDayNameValue) {
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
                                tasksList.add(t)
                                Log.d("DiarioViewModel", "Tarea añadida: ${t.name} para $currentDateFormatted")
                            }
                        }
                    }

                    _tasks.value = tasksList
                    _isLoading.value = false
                    Log.d("DiarioViewModel", "Tareas cargadas: ${tasksList.size} para $currentDateFormatted")
                }

                override fun onCancelled(error: DatabaseError) {
                    _isLoading.value = false
                    Log.e("DiarioViewModel", "Error loading tasks", error.toException())
                }
            })
    }

    fun loadPreviousDayTasks(homeId: String) {
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        updateDayInfo()
        loadTasksForCurrentDay(homeId)
    }

    fun loadNextDayTasks(homeId: String) {
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        updateDayInfo()
        loadTasksForCurrentDay(homeId)
    }

    private fun updateDayInfo() {
        _currentDay.value = getFormattedCurrentDate()
        _currentDayName.value = getDayName(calendar.get(Calendar.DAY_OF_WEEK))
    }

    private fun getDayRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply {
            timeInMillis = calendar.timeInMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val endOfDay = cal.timeInMillis

        return Pair(startOfDay, endOfDay)
    }

    private fun getFormattedCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }

    private fun getDayName(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            Calendar.MONDAY -> "Lunes"
            Calendar.TUESDAY -> "Martes"
            Calendar.WEDNESDAY -> "Miércoles"
            Calendar.THURSDAY -> "Jueves"
            Calendar.FRIDAY -> "Viernes"
            Calendar.SATURDAY -> "Sábado"
            Calendar.SUNDAY -> "Domingo"
            else -> ""
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentListener?.let { tasksRef.removeEventListener(it) }
    }
}