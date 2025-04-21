package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.ui.semanal

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.R
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.adapters.TaskAdapter
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.domain.Task

class SemanalFragment : Fragment() {
    private lateinit var homeId: String

    // Views para Lunes
    private lateinit var taskLunes1: TextView
    private lateinit var taskLunes2: TextView
    private lateinit var taskLunes3: TextView

    // Views para Martes
    private lateinit var taskMartes1: TextView
    private lateinit var taskMartes2: TextView
    private lateinit var taskMartes3: TextView

    // Views para Miércoles
    private lateinit var taskMiercoles1: TextView
    private lateinit var taskMiercoles2: TextView
    private lateinit var taskMiercoles3: TextView

    // Views para Jueves
    private lateinit var taskJueves1: TextView
    private lateinit var taskJueves2: TextView
    private lateinit var taskJueves3: TextView

    // Views para Viernes
    private lateinit var taskViernes1: TextView
    private lateinit var taskViernes2: TextView
    private lateinit var taskViernes3: TextView

    // Views para Sábado
    private lateinit var taskSabado1: TextView
    private lateinit var taskSabado2: TextView
    private lateinit var taskSabado3: TextView

    // Views para Domingo
    private lateinit var taskDomingo1: TextView
    private lateinit var taskDomingo2: TextView
    private lateinit var taskDomingo3: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_semanal, container, false)
        homeId = arguments?.getString("HOME_ID") ?: ""

        // Inicializar todas las vistas
        initViews(view)

        // Cargar tareas desde Firebase
        loadTasks()

        return view
    }

    private fun initViews(view: View) {
        // Lunes
        taskLunes1 = view.findViewById(R.id.task_lunes_1)
        taskLunes2 = view.findViewById(R.id.task_lunes_2)
        taskLunes3 = view.findViewById(R.id.task_lunes_3)

        // Martes
        taskMartes1 = view.findViewById(R.id.task_martes_1)
        taskMartes2 = view.findViewById(R.id.task_martes_2)
        taskMartes3 = view.findViewById(R.id.task_martes_3)

        // Miércoles
        taskMiercoles1 = view.findViewById(R.id.task_miercoles_1)
        taskMiercoles2 = view.findViewById(R.id.task_miercoles_2)
        taskMiercoles3 = view.findViewById(R.id.task_miercoles_3)

        // Jueves
        taskJueves1 = view.findViewById(R.id.task_jueves_1)
        taskJueves2 = view.findViewById(R.id.task_jueves_2)
        taskJueves3 = view.findViewById(R.id.task_jueves_3)

        // Viernes
        taskViernes1 = view.findViewById(R.id.task_viernes_1)
        taskViernes2 = view.findViewById(R.id.task_viernes_2)
        taskViernes3 = view.findViewById(R.id.task_viernes_3)

        // Sábado
        taskSabado1 = view.findViewById(R.id.task_sabado_1)
        taskSabado2 = view.findViewById(R.id.task_sabado_2)
        taskSabado3 = view.findViewById(R.id.task_sabado_3)

        // Domingo
        taskDomingo1 = view.findViewById(R.id.task_domingo_1)
        taskDomingo2 = view.findViewById(R.id.task_domingo_2)
        taskDomingo3 = view.findViewById(R.id.task_domingo_3)
    }

    private fun loadTasks() {
        val database = FirebaseDatabase.getInstance()
        val tasksRef = database.getReference("tasks")

        tasksRef.orderByChild("homeId").equalTo(homeId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Limpiar todas las vistas primero
                    clearAllTaskViews()

                    // Agrupar tareas por día
                    val tasksByDay = mutableMapOf<String, MutableList<Task>>().withDefault { mutableListOf() }

                    for (taskSnapshot in snapshot.children) {
                        val task = taskSnapshot.getValue(Task::class.java)
                        task?.days?.forEach { day ->
                            tasksByDay[day]?.add(task)
                        }
                    }

                    // Asignar tareas a cada día
                    assignTasksToDay("Lunes", tasksByDay["Lunes"] ?: emptyList())
                    assignTasksToDay("Martes", tasksByDay["Martes"] ?: emptyList())
                    assignTasksToDay("Miércoles", tasksByDay["Miércoles"] ?: emptyList())
                    assignTasksToDay("Jueves", tasksByDay["Jueves"] ?: emptyList())
                    assignTasksToDay("Viernes", tasksByDay["Viernes"] ?: emptyList())
                    assignTasksToDay("Sábado", tasksByDay["Sábado"] ?: emptyList())
                    assignTasksToDay("Domingo", tasksByDay["Domingo"] ?: emptyList())
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Error al cargar tareas", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun clearAllTaskViews() {
        // Limpiar todas las vistas de tareas
        val allTaskViews = listOf(
            taskLunes1, taskLunes2, taskLunes3,
            taskMartes1, taskMartes2, taskMartes3,
            taskMiercoles1, taskMiercoles2, taskMiercoles3,
            taskJueves1, taskJueves2, taskJueves3,
            taskViernes1, taskViernes2, taskViernes3,
            taskSabado1, taskSabado2, taskSabado3,
            taskDomingo1, taskDomingo2, taskDomingo3
        )

        allTaskViews.forEach { it.text = "" }
    }

    private fun assignTasksToDay(day: String, tasks: List<Task>) {
        val dayTasks = tasks.take(3) // Solo mostramos hasta 3 tareas por día

        when (day) {
            "Lunes" -> {
                dayTasks.getOrNull(0)?.let { taskLunes1.text = it.name }
                dayTasks.getOrNull(1)?.let { taskLunes2.text = it.name }
                dayTasks.getOrNull(2)?.let { taskLunes3.text = it.name }
            }
            "Martes" -> {
                dayTasks.getOrNull(0)?.let { taskMartes1.text = it.name }
                dayTasks.getOrNull(1)?.let { taskMartes2.text = it.name }
                dayTasks.getOrNull(2)?.let { taskMartes3.text = it.name }
            }
            "Miércoles" -> {
                dayTasks.getOrNull(0)?.let { taskMiercoles1.text = it.name }
                dayTasks.getOrNull(1)?.let { taskMiercoles2.text = it.name }
                dayTasks.getOrNull(2)?.let { taskMiercoles3.text = it.name }
            }
            "Jueves" -> {
                dayTasks.getOrNull(0)?.let { taskJueves1.text = it.name }
                dayTasks.getOrNull(1)?.let { taskJueves2.text = it.name }
                dayTasks.getOrNull(2)?.let { taskJueves3.text = it.name }
            }
            "Viernes" -> {
                dayTasks.getOrNull(0)?.let { taskViernes1.text = it.name }
                dayTasks.getOrNull(1)?.let { taskViernes2.text = it.name }
                dayTasks.getOrNull(2)?.let { taskViernes3.text = it.name }
            }
            "Sábado" -> {
                dayTasks.getOrNull(0)?.let { taskSabado1.text = it.name }
                dayTasks.getOrNull(1)?.let { taskSabado2.text = it.name }
                dayTasks.getOrNull(2)?.let { taskSabado3.text = it.name }
            }
            "Domingo" -> {
                dayTasks.getOrNull(0)?.let { taskDomingo1.text = it.name }
                dayTasks.getOrNull(1)?.let { taskDomingo2.text = it.name }
                dayTasks.getOrNull(2)?.let { taskDomingo3.text = it.name }
            }
        }
    }

    companion object {
        fun newInstance(homeId: String): SemanalFragment {
            return SemanalFragment().apply {
                arguments = Bundle().apply {
                    putString("HOME_ID", homeId)
                }
            }
        }
    }
}