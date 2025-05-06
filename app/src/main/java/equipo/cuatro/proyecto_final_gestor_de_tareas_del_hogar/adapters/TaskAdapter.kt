package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.R
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.domain.Task

class TaskAdapter(
    context: Context,
    private val tasks: List<Task>
) : ArrayAdapter<Task>(context, R.layout.task_item_daily, tasks) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.task_item_daily, parent, false)
        val task = getItem(position) ?: return view

        val title = view.findViewById<TextView>(R.id.taskTitle)
        val description = view.findViewById<TextView>(R.id.taskDescription)
        val assignedTo = view.findViewById<TextView>(R.id.assignedTo)

        title.text = task.name
        description.text = task.description?.takeIf { it.isNotBlank() } ?: "Sin descripción"

        // Obtener asignados para la primera fecha programada
        val assignedMembers = task.schedule.values
            .flatMap { it.assignedTo }
            .distinct()
            .joinToString(", ")

        assignedTo.text = if (assignedMembers.isNotEmpty()) "Asignado a: $assignedMembers" else "Sin asignar"

        return view
    }

    override fun getItemId(position: Int): Long {
        return getItem(position)?.id?.hashCode()?.toLong() ?: super.getItemId(position)
    }

    override fun hasStableIds(): Boolean = true
}