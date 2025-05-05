package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.ui.diario

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.DetalleTareaActivity
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.adapters.TaskAdapter
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.databinding.FragmentDiarioBinding
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.domain.Task

class DiarioFragment : Fragment() {
    private var _binding: FragmentDiarioBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: DiarioViewModel
    private var homeId: String = ""

    private lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(DiarioViewModel::class.java)
        _binding = FragmentDiarioBinding.inflate(inflater, container, false)

        homeId = arguments?.getString("HOME_ID") ?: ""
        val homeName = arguments?.getString("HOME_NAME") ?: ""
        taskAdapter = TaskAdapter(requireContext(), emptyList())
        binding.taskContainer.adapter = taskAdapter
        val emptyView = TextView(requireContext()).apply {
            text = "No hay tareas para hoy"
            textSize = 16f
            gravity = android.view.Gravity.CENTER
            setTextColor(android.graphics.Color.parseColor("#616161"))
        }
        (binding.taskContainer.parent as ViewGroup).addView(emptyView)
        binding.taskContainer.emptyView = emptyView
        binding.taskContainer.setOnItemClickListener { _, _, position, _ ->
            val task = taskAdapter.getItem(position)
            if (task != null) {
                navigateToTaskDetail(task)
            }
        }
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
            binding.progressTasks.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun loadInitialTasks() {
        viewModel.loadTasksForCurrentDay(homeId)
    }

    private fun updateTaskList(tasks: List<Task>) {
        taskAdapter = TaskAdapter(requireContext(), tasks)
        binding.taskContainer.adapter = taskAdapter
        if (tasks.isEmpty()) {
            binding.taskContainer.emptyView = TextView(requireContext()).apply {
                text = "No hay tareas para hoy"
                textSize = 16f
                gravity = android.view.Gravity.CENTER
                setTextColor(android.graphics.Color.parseColor("#616161"))
            }
        }
    }

    private fun navigateToTaskDetail(task: Task) {
        val intent = Intent(requireContext(), DetalleTareaActivity::class.java).apply {
            putExtra("taskId", task.id)
            putExtra("taskName", task.name)
            putExtra("taskDescription", task.description)
            val diasBundle = ArrayList<String>()
            task.days.forEach { (dia, miembros) ->
                if (binding.txtDay.text.equals(dia)) {
                    diasBundle.addAll(miembros)
                }
            }
            putStringArrayListExtra("assignedTo", diasBundle)
            putExtra("completed", task.completed)
        }
        startActivity(intent)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}