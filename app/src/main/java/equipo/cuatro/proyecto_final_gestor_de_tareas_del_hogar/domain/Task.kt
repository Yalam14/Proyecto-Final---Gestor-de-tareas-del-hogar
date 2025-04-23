
package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.domain

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Task(
    var id: String = "",
    val name: String = "",
    val description: String = "",
    val days: List<String> = emptyList(),
    val assignedTo: List<String> = emptyList(),
    val homeId: String = "",
    val createdBy: String = "",
    val creationDate: String = "",
    val timestamp: Long = 0L,  // Nuevo campo
    val completed: Boolean = false
)