package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.domain

data class User(val id: String, val user: String, val mail: String) {
    @Suppress("unused")
    constructor() : this("", "", "")
}