package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.ui.diario

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
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
        binding.taskContainer.removeAllViews()

        if (tasks.isEmpty()) {
            val emptyView = TextView(requireContext()).apply {
                text = "No hay tareas para hoy"
                textSize = 16f
                setTextColor(Color.parseColor("#616161"))
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 32, 0, 0)
                }
            }
            binding.taskContainer.addView(emptyView)
            return
        }

        tasks.forEach { task ->
            val taskView = createTaskView(task)
            binding.taskContainer.addView(taskView)

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
            setPadding(32, 16, 32, 16)
            background = ContextCompat.getDrawable(context, R.drawable.ripple_effect)
            setOnClickListener { navigateToTaskDetail(task) }

            // Título de la tarea
            TextView(requireContext()).apply {
                text = task.name
                textSize = 18f
                setTextColor(Color.parseColor("#212121"))
                setTypeface(null, Typeface.BOLD)
                addView(this)
            }

            // Descripción breve (primera línea)
            val shortDescription = task.description?.split("\n")?.firstOrNull() ?: ""
            if (shortDescription.isNotEmpty()) {
                TextView(requireContext()).apply {
                    text = shortDescription
                    textSize = 14f
                    setTextColor(Color.parseColor("#616161"))
                    setPadding(0, 4, 0, 0)
                    maxLines = 1
                    ellipsize = android.text.TextUtils.TruncateAt.END
                    addView(this)
                }
            }

            // Asignados y estado
            TextView(requireContext()).apply {
                text =
                    "Asignado a: ${task.days.values.joinToString(", ")} • ${if (task.completed) "✓ Completada" else "○ Pendiente"}"
                textSize = 12f
                setTextColor(if (task.completed) Color.parseColor("#4CAF50") else Color.parseColor("#FF9800"))
                setPadding(0, 8, 0, 0)
                addView(this)
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