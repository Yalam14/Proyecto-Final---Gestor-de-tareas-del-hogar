
package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.domain

data class Task(
    var id: String = "",
    val name: String = "",
    val description: String = "",
    val schedule: Map<String, ScheduledDay> = emptyMap(),  // "MONDAY", "TUESDAY", etc.
    val homeId: String = "",
    val createdBy: String = "",
    val creationDate: String = "",
    val timestamp: Long = 0,
    var completed: Boolean = false
)
