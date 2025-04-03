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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.DetalleTareaActivity
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.R
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.databinding.FragmentDiarioBinding

class DiarioFragment : Fragment() {

    private var _binding: FragmentDiarioBinding? = null
    private val binding get() = _binding!!
    private lateinit var diarioViewModel: DiarioViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        diarioViewModel = ViewModelProvider(this).get(DiarioViewModel::class.java)
        _binding = FragmentDiarioBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Configuración inicial
        setupViews()
        setupObservers()

        return root
    }

    private fun setupViews() {
        // Configuración adicional de vistas si es necesaria
    }

    private fun setupObservers() {
        diarioViewModel.text.observe(viewLifecycleOwner) {
            binding.texthome.text = it
        }

        diarioViewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            updateTaskList(tasks)
        }
    }

    private fun updateTaskList(tasks: List<Task>) {
        binding.taskContainer.removeAllViews()

        for (task in tasks) {
            // Crear contenedor principal de la tarea
            val taskLayout = LinearLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 16)
                }
                orientation = LinearLayout.VERTICAL
                setPadding(24, 24, 24, 24)
                background = ContextCompat.getDrawable(requireContext(), R.drawable.task_background)
                setOnClickListener {
                    navigateToTaskDetail(task)
                }
            }

            // TextView para el título de la tarea
            val taskTitle = TextView(requireContext()).apply {
                text = task.name
                textSize = 20f
                setTextColor(Color.WHITE)
                setTypeface(null, Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 8)
                }
            }

            // TextView para los asignados
            val taskAssigned = TextView(requireContext()).apply {
                text = "Asignado a: ${task.assignedTo.joinToString(", ")}"
                textSize = 16f
                setTextColor(Color.WHITE)
            }

            // Añadir vistas al layout
            taskLayout.addView(taskTitle)
            taskLayout.addView(taskAssigned)
            binding.taskContainer.addView(taskLayout)
        }
    }

    private fun navigateToTaskDetail(task: Task) {
        val intent = Intent(requireContext(), DetalleTareaActivity::class.java).apply {
            putExtra("taskName", task.name)
            putExtra("assignedTo", task.assignedTo.joinToString(", "))
            putExtra("completed", task.completed)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}