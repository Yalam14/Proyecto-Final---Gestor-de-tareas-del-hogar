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
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.DetalleTareaActivity
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.R
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.adapters.TaskAdapter
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.databinding.FragmentSemanalBinding
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.domain.Task
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SemanalFragment : Fragment() {
    private var _binding: FragmentSemanalBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SemanalViewModel
    private lateinit var homeId: String
    private lateinit var homeName: String

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
        homeName = arguments?.getString("HOME_NAME") ?: ""
        binding.texthome.text = homeName

        initAdapters()
        setupWeekNavigation()
        setupObservers()
        loadInitialTasks()

        binding.btnShare.setOnClickListener {
            // Lógica para compartir código del hogar
        }

        return binding.root
    }

    private fun initAdapters() {
        mondayAdapter = TaskAdapter(
            requireContext(),
            emptyList(),
            { task -> navigateToTaskDetail(task, "Lunes") },
            "Lunes"
        )
        tuesdayAdapter = TaskAdapter(
            requireContext(),
            emptyList(),
            { task -> navigateToTaskDetail(task, "Martes") },
            "Martes"
        )
        wednesdayAdapter = TaskAdapter(
            requireContext(),
            emptyList(),
            { task -> navigateToTaskDetail(task, "Miércoles") },
            "Miércoles"
        )
        thursdayAdapter = TaskAdapter(
            requireContext(),
            emptyList(),
            { task -> navigateToTaskDetail(task, "Jueves") },
            "Jueves"
        )
        fridayAdapter = TaskAdapter(
            requireContext(),
            emptyList(),
            { task -> navigateToTaskDetail(task, "Viernes") },
            "Viernes"
        )
        saturdayAdapter = TaskAdapter(
            requireContext(),
            emptyList(),
            { task -> navigateToTaskDetail(task, "Sábado") },
            "Sábado"
        )
        sundayAdapter = TaskAdapter(
            requireContext(),
            emptyList(),
            { task -> navigateToTaskDetail(task, "Domingo") },
            "Domingo"
        )

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
        viewModel.currentWeekDisplay.observe(viewLifecycleOwner) { weekDisplay ->
            binding.textWeekNumber.text = weekDisplay
        }

        viewModel.tasksByDay.observe(viewLifecycleOwner) { tasksByDay ->
            tasksByDay?.let {
                updateTaskViews(it)
            }
        }

        viewModel.progress.observe(viewLifecycleOwner) { progress ->
            binding.progressBar.progress = progress
            binding.textProgress.text = "${progress}%"
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressTasks.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.scrollView.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
        }
    }

    private fun updateTaskViews(tasksByDay: Map<String, List<Task>>) {
        mondayAdapter.updateTareas(tasksByDay["Lunes"] ?: emptyList())
        tuesdayAdapter.updateTareas(tasksByDay["Martes"] ?: emptyList())
        wednesdayAdapter.updateTareas(tasksByDay["Miércoles"] ?: emptyList())
        thursdayAdapter.updateTareas(tasksByDay["Jueves"] ?: emptyList())
        fridayAdapter.updateTareas(tasksByDay["Viernes"] ?: emptyList())
        saturdayAdapter.updateTareas(tasksByDay["Sábado"] ?: emptyList())
        sundayAdapter.updateTareas(tasksByDay["Domingo"] ?: emptyList())

        setListViewHeightBasedOnChildren(binding.listViewMonday)
        setListViewHeightBasedOnChildren(binding.listViewTuesday)
        setListViewHeightBasedOnChildren(binding.listViewWednesday)
        setListViewHeightBasedOnChildren(binding.listViewThursday)
        setListViewHeightBasedOnChildren(binding.listViewFriday)
        setListViewHeightBasedOnChildren(binding.listViewSaturday)
        setListViewHeightBasedOnChildren(binding.listViewSunday)
    }

    private fun loadInitialTasks() {
        viewModel.loadTasksForCurrentWeek(homeId)
    }

    private fun navigateToTaskDetail(task: Task, dayName: String) {
        val intent = Intent(requireContext(), DetalleTareaActivity::class.java).apply {
            putExtra("taskId", task.id)
            putExtra("taskName", task.name)
            putExtra("taskDescription", task.description)
            putExtra("homeId", homeId)
            putExtra("completed", task.completed)
            putExtra("dayOfWeek", dayName)

            // Obtener miembros asignados correctamente
            val englishDay = when (dayName) {
                "Lunes" -> "MONDAY"
                "Martes" -> "TUESDAY"
                "Miércoles" -> "WEDNESDAY"
                "Jueves" -> "THURSDAY"
                "Viernes" -> "FRIDAY"
                "Sábado" -> "SATURDAY"
                "Domingo" -> "SUNDAY"
                else -> ""
            }

            // Buscar miembros asignados tanto por fecha como por día recurrente
            val weekDates = viewModel.getWeekDates()
            val dateForDay = weekDates.find { it.second == dayName }?.first ?: ""

            val assignedFromDate = task.schedule[dateForDay]?.assignedTo ?: emptyList()
            val assignedFromDay = task.schedule[englishDay]?.assignedTo ?: emptyList()

            val assignedMembers = (assignedFromDate + assignedFromDay).distinct()

            putStringArrayListExtra("assignedTo", ArrayList(assignedMembers))
            putExtra("isRecurrent", englishDay.isNotEmpty())

            // Añadir estos parámetros esenciales para la edición
            putExtra("creator", task.createdBy)
            putExtra("canEdit", viewModel.canEdit.value ?: false)
        }
        startActivity(intent)
    }

    private fun setListViewHeightBasedOnChildren(listView: ListView) {
        val adapter = listView.adapter ?: return
        var totalHeight = 0
        val desiredWidth = View.MeasureSpec.makeMeasureSpec(
            listView.width,
            View.MeasureSpec.AT_MOST
        )

        for (i in 0 until adapter.count) {
            val listItem = adapter.getView(i, null, listView)
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED)
            totalHeight += listItem.measuredHeight
        }

        val params = listView.layoutParams
        params.height = totalHeight + (listView.dividerHeight * (adapter.count - 1))
        listView.layoutParams = params
        listView.requestLayout()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(homeId: String, homeName: String): SemanalFragment {
            return SemanalFragment().apply {
                arguments = Bundle().apply {
                    putString("HOME_ID", homeId)
                    putString("HOME_NAME", homeName)
                }
            }
        }
    }
}