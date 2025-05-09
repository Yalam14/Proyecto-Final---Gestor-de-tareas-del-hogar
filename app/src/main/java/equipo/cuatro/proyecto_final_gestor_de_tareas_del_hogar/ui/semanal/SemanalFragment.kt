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
        homeName = arguments?.getString("HOME_NAME") ?: ""
        binding.texthome.text = homeName

        initAdapters()

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
        setListViewHeightBasedOnChildren(binding.listViewMonday)
        setListViewHeightBasedOnChildren(binding.listViewTuesday)
        setListViewHeightBasedOnChildren(binding.listViewWednesday)
        setListViewHeightBasedOnChildren(binding.listViewThursday)
        setListViewHeightBasedOnChildren(binding.listViewFriday)
        setListViewHeightBasedOnChildren(binding.listViewSaturday)
        setListViewHeightBasedOnChildren(binding.listViewSunday)
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

        viewModel.homeCode.observe(viewLifecycleOwner) { code ->
            Log.d("HomeCode", "Código actualizado: $code")
        }
    }

    private fun loadInitialTasks() {
        viewModel.loadTasksForCurrentWeek(homeId)
    }

    private fun updateTaskViews(tasksByDay: Map<String, List<Task>>) {
        mondayAdapter.updateTareas(tasksByDay["Lunes"] ?: emptyList())
        tuesdayAdapter.updateTareas(tasksByDay["Martes"] ?: emptyList())
        wednesdayAdapter.updateTareas(tasksByDay["Miércoles"] ?: emptyList())
        thursdayAdapter.updateTareas(tasksByDay["Jueves"] ?: emptyList())
        fridayAdapter.updateTareas(tasksByDay["Viernes"] ?: emptyList())
        saturdayAdapter.updateTareas(tasksByDay["Sábado"] ?: emptyList())
        sundayAdapter.updateTareas(tasksByDay["Domingo"] ?: emptyList())
        mondayAdapter.notifyDataSetChanged()
        tuesdayAdapter.notifyDataSetChanged()
        wednesdayAdapter.notifyDataSetChanged()
        thursdayAdapter.notifyDataSetChanged()
        fridayAdapter.notifyDataSetChanged()
        saturdayAdapter.notifyDataSetChanged()
        sundayAdapter.notifyDataSetChanged()
    }

    private fun navigateToTaskDetail(task: Task, dayName: String) {
        val intent = Intent(requireContext(), DetalleTareaActivity::class.java).apply {
            putExtra("taskId", task.id)
            putExtra("taskName", task.name)
            putExtra("taskDescription", task.description)
            putExtra("homeId", homeId)
            putExtra("completed", task.completed)
            putExtra("dayOfWeek", dayName)
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
            val assignedMembers = if (englishDay.isNotEmpty()) {
                task.schedule[englishDay]?.assignedTo ?: emptyList()
            } else {
                val weekDates = viewModel.getWeekDates()
                val dateForDay = weekDates.find { it.second == dayName }?.first ?: ""
                task.schedule[dateForDay]?.assignedTo ?: emptyList()
            }
            val finalAssignedMembers = if (assignedMembers.isEmpty()) {
                task.schedule.values.flatMap { it.assignedTo }.distinct()
            } else {
                assignedMembers
            }
            putStringArrayListExtra("assignedTo", ArrayList(finalAssignedMembers))
            putExtra("isRecurrent", englishDay.isNotEmpty())
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