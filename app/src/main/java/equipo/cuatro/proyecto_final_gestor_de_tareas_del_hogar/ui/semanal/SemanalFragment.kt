package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.ui.semanal

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.DetalleTareaActivity
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.R
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.databinding.FragmentSemanalBinding
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.domain.Task

class SemanalFragment : Fragment() {
    private var _binding: FragmentSemanalBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SemanalViewModel
    private lateinit var homeId: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(SemanalViewModel::class.java)
        _binding = FragmentSemanalBinding.inflate(inflater, container, false)

        homeId = arguments?.getString("HOME_ID") ?: ""
        setupWeekNavigation()
        setupObservers()
        loadInitialTasks()

        binding.btnShare.setOnClickListener {
            viewModel.loadHomeCode(homeId)

            val observer = object : Observer<String> {
                override fun onChanged(value: String) {
                    value?.let {
                        if (it.isNotEmpty()) {
                            copiarCodigo(it)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "No hay código disponible",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        viewModel.homeCode.removeObserver(this)
                    }
                }
            }
            viewModel.homeCode.observe(viewLifecycleOwner, observer)
        }

        return binding.root
    }

    private fun setupWeekNavigation() {
        binding.buttonPreviousWeek.setOnClickListener {
            viewModel.loadPreviousWeekTasks(homeId)
        }

        binding.buttonNextWeek.setOnClickListener {
            viewModel.loadNextWeekTasks(homeId)
        }
    }

    private fun setupObservers() {
        viewModel.currentWeek.observe(viewLifecycleOwner) { week ->
            binding.textWeekNumber.text = "Semana $week"
        }

        viewModel.tasksByDay.observe(viewLifecycleOwner) { tasksByDay ->
            tasksByDay?.let {
                updateTaskViews(it)
            }
        }

        viewModel.progress.observe(viewLifecycleOwner) { progress ->
            binding.progressBar.progress = progress
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressTasks.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.scrollView.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
        }

        viewModel.homeCode.observe(viewLifecycleOwner) { code ->
            Log.d("HomeCode", "Código actualizado: $code")
        }
    }

    private fun loadInitialTasks() {
        viewModel.loadTasksForCurrentWeek(homeId)
    }

    private fun updateTaskViews(tasksByDay: Map<String, List<Task>>) {
        // Lunes
        updateDayTasks("Lunes", tasksByDay["Lunes"] ?: emptyList(),
            binding.taskLunes1, binding.taskLunes2, binding.taskLunes3)

        // Martes
        updateDayTasks("Martes", tasksByDay["Martes"] ?: emptyList(),
            binding.taskMartes1, binding.taskMartes2, binding.taskMartes3)

        // Miércoles
        updateDayTasks("Miércoles", tasksByDay["Miércoles"] ?: emptyList(),
            binding.taskMiercoles1, binding.taskMiercoles2, binding.taskMiercoles3)

        // Jueves
        updateDayTasks("Jueves", tasksByDay["Jueves"] ?: emptyList(),
            binding.taskJueves1, binding.taskJueves2, binding.taskJueves3)

        // Viernes
        updateDayTasks("Viernes", tasksByDay["Viernes"] ?: emptyList(),
            binding.taskViernes1, binding.taskViernes2, binding.taskViernes3)

        // Sábado
        updateDayTasks("Sábado", tasksByDay["Sábado"] ?: emptyList(),
            binding.taskSabado1, binding.taskSabado2, binding.taskSabado3)

        // Domingo
        updateDayTasks("Domingo", tasksByDay["Domingo"] ?: emptyList(),
            binding.taskDomingo1, binding.taskDomingo2, binding.taskDomingo3)
    }

    private fun updateDayTasks(dia: String, tasks: List<Task>, vararg taskViews: View) {
        taskViews.forEachIndexed { index, view ->
            val textView = view as TextView
            if (index < tasks.size) {
                val task = tasks[index]
                textView.apply {
                    text = task.name
                    setBackgroundColor(
                        if (task.completed) resources.getColor(R.color.green_completed)
                        else resources.getColor(R.color.task_pending)
                    )
                    setOnClickListener { navigateToTaskDetail(task, dia) }
                    visibility = View.VISIBLE
                }
            } else {
                textView.visibility = View.INVISIBLE
            }
        }
    }

    private fun navigateToTaskDetail(task: Task, dia: String) {
        val intent = Intent(requireContext(), DetalleTareaActivity::class.java).apply {
            putExtra("taskId", task.id)
            putExtra("taskName", task.name)
            putExtra("taskDescription", task.description)

            val diasBundle = ArrayList<String>()
            task.schedule[dia]?.let { scheduledDay ->
                diasBundle.addAll(scheduledDay.assignedTo)
            }

            putStringArrayListExtra("assignedTo", diasBundle)
            putExtra("completed", task.completed)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

    private fun copiarCodigo(text: String) {
        try {
            val clipboard =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Código del Hogar", text)
            clipboard.setPrimaryClip(clip)

            Toast.makeText(
                requireContext(),
                "✅ Código copiado: $text",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "❌ Error al copiar código",
                Toast.LENGTH_SHORT
            ).show()
            Log.e("Clipboard", "Error: ${e.message}")
        }
    }
}