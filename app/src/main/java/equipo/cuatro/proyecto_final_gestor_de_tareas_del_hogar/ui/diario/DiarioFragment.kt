package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.ui.diario

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.DetalleTareaActivity
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.R
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.databinding.FragmentDiarioBinding
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.domain.Task

class DiarioFragment : Fragment() {

    private var _binding: FragmentDiarioBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: DiarioViewModel
    private var homeId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(DiarioViewModel::class.java)
        _binding = FragmentDiarioBinding.inflate(inflater, container, false)

        homeId = arguments?.getString("HOME_ID") ?: ""
        val homeName = arguments?.getString("HOME_NAME") ?: ""

        // Mantener diseño original
        binding.texthome.text = homeName
        setupDayNavigation()
        setupObservers()
        loadInitialTasks()

        return binding.root
    }

    private fun setupDayNavigation() {
        binding.btnPrevDay.setOnClickListener {
            viewModel.loadPreviousDayTasks(homeId)
        }

        binding.btnNextDay.setOnClickListener {
            viewModel.loadNextDayTasks(homeId)
        }
    }

    private fun setupObservers() {
        viewModel.currentDay.observe(viewLifecycleOwner) { day ->
            binding.txtDay.text = day
        }

        viewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            updateTaskList(tasks)
            updateProgressBar(tasks)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressTasks.visibility = if (isLoading) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun loadInitialTasks() {
        viewModel.loadTasksForCurrentDay(homeId)
    }

    private fun updateTaskList(tasks: List<Task>) {
        binding.taskContainer.removeAllViews()

        tasks.forEach { task ->
            val taskView = createTaskView(task)
            binding.taskContainer.addView(taskView)

            // Añadir separador si no es el último elemento
            if (task != tasks.last()) {
                val divider = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        2
                    ).apply {
                        setMargins(0, 16, 0, 16)
                    }
                    setBackgroundColor(Color.parseColor("#EEEEEE"))
                }
                binding.taskContainer.addView(divider)
            }
        }
    }

    private fun createTaskView(task: Task): View {
        return LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(0, 8, 0, 8)

            // Título de la tarea
            TextView(requireContext()).apply {
                text = task.name
                textSize = 18f
                setTextColor(Color.parseColor("#212121"))
                setTypeface(null, Typeface.BOLD)
                addView(this)
            }

            // Asignados y estado
            TextView(requireContext()).apply {
                text = "Asignado a: ${task.assignedTo.joinToString(", ")} • ${if(task.completed) "✓ Completada" else "○ Pendiente"}"
                textSize = 14f
                setTextColor(Color.parseColor("#616161"))
                setPadding(0, 4, 0, 0)
                addView(this)
            }

            setOnClickListener {
                navigateToTaskDetail(task)
            }
        }
    }

    private fun updateProgressBar(tasks: List<Task>) {
        if (tasks.isNotEmpty()) {
            val completedTasks = tasks.count { it.completed }
            val progress = (completedTasks.toFloat() / tasks.size * 100).toInt()
            binding.progressTasks.progress = progress
        } else {
            binding.progressTasks.progress = 0
        }
    }

    private fun navigateToTaskDetail(task: Task) {
        val intent = Intent(requireContext(), DetalleTareaActivity::class.java).apply {
            putExtra("taskId", task.id)
            putExtra("taskName", task.name)
            putStringArrayListExtra("assignedTo", ArrayList(task.assignedTo))
            putExtra("completed", task.completed)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}