package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.domain

import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Home(
    val name: String = "",
    val icon: String = "",
    val creationDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val createdBy: String = "",
    val code: String = "",
    val participants: Map<String, String> = emptyMap()
) {
    // Función para obtener el ID del recurso del icono
    fun getIconResourceId(): Int {
        return when (icon) {
            "baseline_account_balance_24" -> R.drawable.baseline_account_balance_24
            "baseline_add_business_24" -> R.drawable.baseline_add_business_24
            "baseline_add_home_24" -> R.drawable.baseline_add_home_24
            "baseline_bedroom_baby_24" -> R.drawable.baseline_bedroom_baby_24
            "bar" -> R.drawable.bar
            "dormitorio" -> R.drawable.dormitorio
            "fabrica" -> R.drawable.fabrica
            "factory" -> R.drawable.factory
            "mosque" -> R.drawable.mosque
            "templo" -> R.drawable.templo
            else -> R.drawable.baseline_add_home_24 // Icono por defecto
        }
    }
}