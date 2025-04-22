package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.domain

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Home(
    val name: String = "",
    val icon: String = "",
    val creationDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val createdBy: String = "",
    val code: String = "",
    val participants: Map<String, Boolean> = emptyMap()
)
