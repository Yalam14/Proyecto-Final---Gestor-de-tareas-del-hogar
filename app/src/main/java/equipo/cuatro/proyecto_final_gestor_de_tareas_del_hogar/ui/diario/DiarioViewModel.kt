package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.ui.diario

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DiarioViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Departamento 1"
    }
    val text: LiveData<String> = _text
}