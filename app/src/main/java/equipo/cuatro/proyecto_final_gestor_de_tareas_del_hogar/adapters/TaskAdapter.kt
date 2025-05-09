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
    private var tasks: List<Task>,
    private val onTaskClick: ((Task) -> Unit)? = null,
    private val dayOfWeek: String? = null
) : ArrayAdapter<Task>(context, R.layout.task_item_daily, tasks) {

    fun updateTareas(newTasks: List<Task>) {
        this.tasks = newTasks
        notifyDataSetChanged()
    }

    override fun getCount(): Int = tasks.size

    override fun getItem(position: Int): Task = tasks[position]

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.task_item_daily, parent, false)
        val task = getItem(position)
        val title = view.findViewById<TextView>(R.id.taskTitle)
        val description = view.findViewById<TextView>(R.id.taskDescription)
        val assignedTo = view.findViewById<TextView>(R.id.assignedTo)
        title.text = task.name
        description.text = task.description?.takeIf { it.isNotBlank() } ?: "Sin descripción"
        val assignedMembers = getAssignedMembersForDay(task, dayOfWeek)
        assignedTo.text = if (assignedMembers.isNotEmpty()) {
            "Asignado a: ${assignedMembers.joinToString(", ")}"
        } else {
            "Sin asignar"
        }
        view.setBackgroundColor(
            context.resources.getColor(
                if (task.completed) R.color.green_completed
                else R.color.task_pending
            )
        )
        view.setOnClickListener {
            onTaskClick?.invoke(task)
        }
        return view
    }

    private fun getAssignedMembersForDay(task: Task, day: String?): List<String> {
        return when {
            day == null -> {
                task.schedule.values.flatMap { it.assignedTo }.distinct()
            }
            else -> {
                val englishDay = when (day) {
                    "Lunes" -> "MONDAY"
                    "Martes" -> "TUESDAY"
                    "Miércoles" -> "WEDNESDAY"
                    "Jueves" -> "THURSDAY"
                    "Viernes" -> "FRIDAY"
                    "Sábado" -> "SATURDAY"
                    "Domingo" -> "SUNDAY"
                    else -> null
                }
                englishDay?.let { task.schedule[it]?.assignedTo ?: emptyList() } ?: emptyList()
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id.hashCode().toLong()
    }

    override fun hasStableIds(): Boolean = true
}