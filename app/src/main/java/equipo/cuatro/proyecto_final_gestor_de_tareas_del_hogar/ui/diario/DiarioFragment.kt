package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.ui.diario

import android.content.Intent
import android.os.Bundle
import android.util.Log
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

        // Inicialización con lista vacía
        taskAdapter = TaskAdapter(requireContext(), emptyList())
        binding.taskContainer.adapter = taskAdapter

        setupEmptyView()
        setupArguments()
        setupClickListeners()
        setupObservers()
        loadInitialTasks()

        return binding.root
    }

    private fun setupEmptyView() {
        val emptyView = TextView(requireContext()).apply {
            text = "No hay tareas para hoy"
            textSize = 16f
            gravity = android.view.Gravity.CENTER
            setTextColor(android.graphics.Color.parseColor("#616161"))
        }
        (binding.taskContainer.parent as ViewGroup).addView(emptyView)
        binding.taskContainer.emptyView = emptyView
    }

    private fun setupArguments() {
        arguments?.let {
            homeId = it.getString("HOME_ID") ?: ""
            val homeName = it.getString("HOME_NAME") ?: ""
            binding.texthome.text = homeName
            Log.d("DiarioFragment", "Home ID recibido: $homeId")
        } ?: run {
            Log.e("DiarioFragment", "No se recibieron argumentos")
        }
    }

    private fun setupClickListeners() {
        binding.taskContainer.setOnItemClickListener { _, _, position, _ ->
            taskAdapter.getItem(position)?.let { task ->
                navigateToTaskDetail(task)
            }
        }

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
            Log.d("DiarioFragment", "Día actualizado: $day")
        }

        viewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            Log.d("DiarioFragment", "Tareas recibidas: ${tasks?.size ?: 0}")
            updateTaskList(tasks ?: emptyList())
            updateProgressBar(tasks ?: emptyList())
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressTasks.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun updateTaskList(tasks: List<Task>) {
        taskAdapter = TaskAdapter(requireContext(), tasks)
        binding.taskContainer.adapter = taskAdapter
        Log.d("DiarioFragment", "Lista actualizada con ${tasks.size} tareas")
    }

    private fun loadInitialTasks() {
        if (homeId.isNotEmpty()) {
            viewModel.loadTasksForCurrentDay(homeId)
        } else {
            Log.e("DiarioFragment", "Home ID vacío, no se pueden cargar tareas")
        }
    }

    private fun navigateToTaskDetail(task: Task) {
        Intent(requireContext(), DetalleTareaActivity::class.java).apply {
            putExtra("taskId", task.id)
            putExtra("taskName", task.name)
            putExtra("taskDescription", task.description)

            val diaActual = binding.txtDay.text.toString()
            val asignados = task.schedule[diaActual]?.assignedTo ?: emptyList()

            putStringArrayListExtra("assignedTo", ArrayList(asignados))
            putExtra("completed", task.completed)
            startActivity(this)
        }
    }

    private fun updateProgressBar(tasks: List<Task>) {
        binding.progressTasks.progress = if (tasks.isNotEmpty()) {
            (tasks.count { it.completed }.toFloat() / tasks.size * 100).toInt()
        } else {
            0
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}