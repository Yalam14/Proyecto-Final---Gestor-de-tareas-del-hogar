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
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DiarioViewModel : ViewModel() {
    private val _tasks = MutableLiveData<List<Task>>()
    val tasks: LiveData<List<Task>> = _tasks

    private val _currentDay = MutableLiveData<String>()
    val currentDay: LiveData<String> = _currentDay

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _canEdit = MutableLiveData<Boolean>()
    val canEdit: LiveData<Boolean> = _canEdit

    private val _creator = MutableLiveData<String>()
    val creator: LiveData<String> = _creator

    private val _homeCode = MutableLiveData<String>()
    val homeCode: LiveData<String> = _homeCode

    init {
        _currentDay.value = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun loadCreator(homeId: String) {
        FirebaseDatabase.getInstance().getReference("homes").child(homeId).get()
            .addOnSuccessListener { snapshot ->
                val creator = snapshot.getValue(Home::class.java)?.createdBy ?: ""
                _creator.value = creator
            }
            .addOnFailureListener {
                _creator.value = ""
            }
    }

    fun loadCanEdit(homeId: String) {
        FirebaseDatabase.getInstance().getReference("homes").child(homeId).get()
            .addOnSuccessListener { snapshot ->
                val canEdit = snapshot.getValue(Home::class.java)?.canParticipantEdit ?: false
                _canEdit.value = canEdit
            }
            .addOnFailureListener {
                _canEdit.value = false
            }
    }

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

    fun loadTasksForCurrentDay(homeId: String) {
        _isLoading.value = true
        FirebaseDatabase.getInstance().getReference("tasks")
            .orderByChild("homeId").equalTo(homeId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tasks = snapshot.children.mapNotNull { taskSnapshot ->
                        taskSnapshot.getValue(Task::class.java)?.apply {
                            id = taskSnapshot.key ?: ""
                        }
                    }
                    _tasks.value = tasks
                    _isLoading.value = false
                }

                override fun onCancelled(error: DatabaseError) {
                    _isLoading.value = false
                    Log.e("DiarioViewModel", "Error loading tasks", error.toException())
                }
            })
    }

    fun loadPreviousDayTasks(homeId: String) {
        _currentDay.value?.let { currentDate ->
            _currentDay.value = getAdjacentDate(currentDate, -1)
            loadTasksForCurrentDay(homeId)
        }
    }

    fun loadNextDayTasks(homeId: String) {
        _currentDay.value?.let { currentDate ->
            _currentDay.value = getAdjacentDate(currentDate, 1)
            loadTasksForCurrentDay(homeId)
        }
    }

    private fun getAdjacentDate(currentDate: String, daysToAdd: Int): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).run {
            parse(currentDate)?.let { date ->
                Calendar.getInstance().apply {
                    time = date
                    add(Calendar.DAY_OF_YEAR, daysToAdd)
                }.time.let { format(it) }
            } ?: currentDate
        }
    }
}