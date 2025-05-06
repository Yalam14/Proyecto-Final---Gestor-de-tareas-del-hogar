package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.domain

data class ScheduledDay(
    var assignedTo: List<String> = emptyList(),
    var completed: Boolean = false
)