package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.ui.diario

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.DetalleTareaActivity
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

        diarioViewModel.text.observe(viewLifecycleOwner) {
            binding.texthome.text = it
        }

        diarioViewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            updateTaskList(tasks)
        }

        return root
    }

    private fun updateTaskList(tasks: List<Task>) {
        binding.taskContainer.removeAllViews()

        for ((index, task) in tasks.withIndex()) {
            val taskLayout = LinearLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.HORIZONTAL
                setPadding(16, 16, 16, 16)
                setBackgroundResource(android.R.drawable.dialog_holo_light_frame)
                setOnClickListener {
                    val intent = Intent(requireContext(), DetalleTareaActivity::class.java).apply {
                        putExtra("taskName", task.name)
                        putExtra("assignedTo", task.assignedTo.joinToString(", "))
                        putExtra("completed", task.completed)
                    }
                    startActivity(intent)
                }
            }

            val checkBox = CheckBox(requireContext()).apply {
                isChecked = task.completed
                setOnCheckedChangeListener { _, _ ->
                    diarioViewModel.toggleTaskCompletion(index)
                }
            }

            val taskDetails = LinearLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.VERTICAL
            }

            val taskTitle = TextView(requireContext()).apply {
                text = task.name
                textSize = 24f
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            val taskAssigned = TextView(requireContext()).apply {
                text = task.assignedTo.joinToString(", ")
                textSize = 18f
            }

            taskDetails.addView(taskTitle)
            taskDetails.addView(taskAssigned)
            taskLayout.addView(checkBox)
            taskLayout.addView(taskDetails)
            binding.taskContainer.addView(taskLayout)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
