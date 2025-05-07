package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.domain

data class Task(
    var id: String = "",
    val name: String = "",
    val description: String = "",
    val schedule: Map<String, ScheduledDay> = emptyMap(),
    val homeId: String = "",
    val createdBy: String = "",
    val creationDate: String = "",
    val timestamp: Long = 0,
    var completed: Boolean = false
) {

    fun getScheduledDaysFormatted(): List<String> {
        return schedule.keys.map { dayKey ->
            when (dayKey.uppercase()) {
                "MONDAY" -> "Lunes"
                "TUESDAY" -> "Martes"
                "WEDNESDAY" -> "Miércoles"
                "THURSDAY" -> "Jueves"
                "FRIDAY" -> "Viernes"
                "SATURDAY" -> "Sábado"
                "SUNDAY" -> "Domingo"
                else -> dayKey
            }
        }
    }

    fun isScheduledForDay(day: String): Boolean {
        val englishDay = when (day) {
            "Lunes" -> "MONDAY"
            "Martes" -> "TUESDAY"
            "Miércoles" -> "WEDNESDAY"
            "Jueves" -> "THURSDAY"
            "Viernes" -> "FRIDAY"
            "Sábado" -> "SATURDAY"
            "Domingo" -> "SUNDAY"
            else -> day
        }
        return schedule.containsKey(englishDay)
    }
}