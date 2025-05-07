package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.ui.semanal

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.DetalleTareaActivity
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.R
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.adapters.TaskAdapter
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.databinding.FragmentSemanalBinding
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.domain.Task
import java.util.Calendar

class SemanalFragment : Fragment() {
    private var _binding: FragmentSemanalBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SemanalViewModel
    private lateinit var homeId: String

    // Adapters para cada día de la semana
    private lateinit var mondayAdapter: TaskAdapter
    private lateinit var tuesdayAdapter: TaskAdapter
    private lateinit var wednesdayAdapter: TaskAdapter
    private lateinit var thursdayAdapter: TaskAdapter
    private lateinit var fridayAdapter: TaskAdapter
    private lateinit var saturdayAdapter: TaskAdapter
    private lateinit var sundayAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(SemanalViewModel::class.java)
        _binding = FragmentSemanalBinding.inflate(inflater, container, false)

        homeId = arguments?.getString("HOME_ID") ?: ""

        // Inicializar los adapters
        initAdapters()

        setupWeekNavigation()
        setupObservers()
        loadInitialTasks()

        return binding.root
    }

    private fun initAdapters() {
        val onTaskClick: (Task) -> Unit = { task ->
            navigateToTaskDetail(task)
        }

        mondayAdapter = TaskAdapter(requireContext(), emptyList(), onTaskClick)
        tuesdayAdapter = TaskAdapter(requireContext(), emptyList(), onTaskClick)
        wednesdayAdapter = TaskAdapter(requireContext(), emptyList(), onTaskClick)
        thursdayAdapter = TaskAdapter(requireContext(), emptyList(), onTaskClick)
        fridayAdapter = TaskAdapter(requireContext(), emptyList(), onTaskClick)
        saturdayAdapter = TaskAdapter(requireContext(), emptyList(), onTaskClick)
        sundayAdapter = TaskAdapter(requireContext(), emptyList(), onTaskClick)

        // Asignar los adapters a los ListViews
        binding.listViewMonday.adapter = mondayAdapter
        binding.listViewTuesday.adapter = tuesdayAdapter
        binding.listViewWednesday.adapter = wednesdayAdapter
        binding.listViewThursday.adapter = thursdayAdapter
        binding.listViewFriday.adapter = fridayAdapter
        binding.listViewSaturday.adapter = saturdayAdapter
        binding.listViewSunday.adapter = sundayAdapter
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
            binding.textWeekNumber.text = getString(R.string.week_number, week)
            binding.textYear.text = Calendar.getInstance().get(Calendar.YEAR).toString()
        }

        viewModel.tasksByDay.observe(viewLifecycleOwner) { tasksByDay ->
            tasksByDay?.let {
                updateTaskViews(it)
            }
        }

        viewModel.progress.observe(viewLifecycleOwner) { progress ->
            binding.progressBar.progress = progress
            binding.textProgress.text = getString(R.string.progress_percentage, progress)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressTasks.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.scrollView.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
        }
    }

    private fun loadInitialTasks() {
        viewModel.loadTasksForCurrentWeek(homeId)
    }

    private fun updateTaskViews(tasksByDay: Map<String, List<Task>>) {
        // Actualizar los adapters con las nuevas listas de tareas
        mondayAdapter.updateTareas(tasksByDay["Lunes"] ?: emptyList())
        tuesdayAdapter.updateTareas(tasksByDay["Martes"] ?: emptyList())
        wednesdayAdapter.updateTareas(tasksByDay["Miércoles"] ?: emptyList())
        thursdayAdapter.updateTareas(tasksByDay["Jueves"] ?: emptyList())
        fridayAdapter.updateTareas(tasksByDay["Viernes"] ?: emptyList())
        saturdayAdapter.updateTareas(tasksByDay["Sábado"] ?: emptyList())
        sundayAdapter.updateTareas(tasksByDay["Domingo"] ?: emptyList())

        // Asegurarse de que los ListViews se actualicen
        mondayAdapter.notifyDataSetChanged()
        tuesdayAdapter.notifyDataSetChanged()
        wednesdayAdapter.notifyDataSetChanged()
        thursdayAdapter.notifyDataSetChanged()
        fridayAdapter.notifyDataSetChanged()
        saturdayAdapter.notifyDataSetChanged()
        sundayAdapter.notifyDataSetChanged()
    }

    private fun navigateToTaskDetail(task: Task) {
        val intent = Intent(requireContext(), DetalleTareaActivity::class.java).apply {
            putExtra("taskId", task.id)
            putExtra("taskName", task.name)
            putExtra("taskDescription", task.description)

            // Obtener todos los asignados de todos los días
            val assignedMembers = task.schedule.values
                .flatMap { it.assignedTo }
                .distinct()
                .toMutableList()

            putStringArrayListExtra("assignedTo", ArrayList(assignedMembers))
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
}