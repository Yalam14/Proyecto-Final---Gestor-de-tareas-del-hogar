package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.ui.semanal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.domain.Task



class SemanalViewModel : ViewModel() {
    private val _currentWeek = MutableLiveData<Int>(1)
    val currentWeek: LiveData<Int> = _currentWeek

    private val _tasks = MutableLiveData<List<Task>>()
    val tasks: LiveData<List<Task>> = _tasks

    fun changeWeek(isNext: Boolean) {
        _currentWeek.value = (currentWeek.value ?: 1).let {
            if (isNext) it + 1 else it - 1
        }
        loadTasksForWeek()
    }

    private fun loadTasksForWeek() {

    }
}