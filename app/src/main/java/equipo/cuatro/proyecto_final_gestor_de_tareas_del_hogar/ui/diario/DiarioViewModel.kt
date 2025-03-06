package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.ui.diario

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DiarioViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Departamento 1"
    }
    val text: LiveData<String> = _text

    private val _tasks = MutableLiveData<List<Task>>().apply {
        value = listOf(
            Task("Limpiar la cocina", listOf("OM", "SV", "AL", "JV"), true),
            Task("Lavar el porche", listOf("OM", "SV"), true),
            Task("Bañar al perro", listOf("SV", "JV"), false),
            Task("Jugar Roblox", listOf("OM", "SV", "AL", "JV"), false)
        )
    }
    val tasks: LiveData<List<Task>> = _tasks

    fun toggleTaskCompletion(taskIndex: Int) {
        _tasks.value = _tasks.value?.mapIndexed { index, task ->
            if (index == taskIndex) task.copy(completed = !task.completed) else task
        }
    }
}

data class Task(val name: String, val assignedTo: List<String>, val completed: Boolean)
