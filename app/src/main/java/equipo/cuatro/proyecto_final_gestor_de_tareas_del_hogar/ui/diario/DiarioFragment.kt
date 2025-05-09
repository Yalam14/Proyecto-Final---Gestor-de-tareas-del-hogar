package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.ui.diario

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

        taskAdapter = TaskAdapter(
            requireContext(),
            emptyList(),
            { task -> navigateToTaskDetail(task) }
        )
        binding.taskContainer.adapter = taskAdapter

        setupEmptyView()
        setupArguments()
        setupClickListeners()
        setupObservers()
        loadInitialTasks()

        viewModel.loadCanEdit(homeId)
        viewModel.loadCreator(homeId)

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

        viewModel.homeCode.observe(viewLifecycleOwner) { code ->
            Log.d("HomeCode", "Código actualizado: $code")
        }
    }

    private fun updateTaskList(tasks: List<Task>) {
        val currentDay = binding.txtDay.text.toString()
        Log.d("DiarioFragment", "Mostrando tareas para $currentDay - Total: ${tasks.size}")
        taskAdapter.updateTareas(tasks)

        // Mostrar mensaje si no hay tareas
        if (tasks.isEmpty()) {
            Toast.makeText(context, "No hay tareas programadas para $currentDay", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadInitialTasks() {
        if (homeId.isNotEmpty()) {
            viewModel.loadTasksForCurrentDay(homeId)
        } else {
            Log.e("DiarioFragment", "Home ID vacío, no se pueden cargar tareas")
        }
    }

    private fun navigateToTaskDetail(task: Task) {
        val intent = Intent(requireContext(), DetalleTareaActivity::class.java).apply {
            putExtra("taskId", task.id)
            putExtra("taskName", task.name)
            putExtra("taskDescription", task.description)
            putExtra("homeId", homeId)
            putExtra("completed", task.completed)
            val currentDate = viewModel.currentDay.value ?: ""
            val dayName = binding.txtDay.text.toString()
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

            val asignados = task.schedule[englishDay]?.assignedTo ?: emptyList()

            putStringArrayListExtra("assignedTo", ArrayList(asignados))
            putExtra("completed", task.completed)
            putExtra("canEdit", viewModel.canEdit.value)
            putExtra("creator", viewModel.creator.value)

            val assignedMembers = when {
                currentDate.isNotEmpty() && task.schedule.containsKey(currentDate) -> {
                    task.schedule[currentDate]?.assignedTo ?: emptyList()
                }
                englishDay.isNotEmpty() && task.schedule.containsKey(englishDay) -> {
                    task.schedule[englishDay]?.assignedTo ?: emptyList()
                }
                else -> {
                    task.schedule.values.flatMap { it.assignedTo }.distinct()
                }
            }
            putStringArrayListExtra("assignedTo", ArrayList(assignedMembers))
            putExtra("isRecurrent", englishDay.isNotEmpty() && task.schedule.containsKey(englishDay))
        }
        startActivity(intent)
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