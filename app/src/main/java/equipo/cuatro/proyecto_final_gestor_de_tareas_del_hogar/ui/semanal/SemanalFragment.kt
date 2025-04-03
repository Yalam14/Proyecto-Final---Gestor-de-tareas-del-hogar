package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.ui.semanal

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.DetalleTareaActivity
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.databinding.FragmentSemanalBinding

class SemanalFragment : Fragment() {

    private var _binding: FragmentSemanalBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSemanalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar los botones de navegación de semana
        binding.apply {
            buttonPreviousWeek.setOnClickListener { updateWeekNumber(false) }
            buttonNextWeek.setOnClickListener { updateWeekNumber(true) }
        }

        // Configurar listeners solo para las tareas del lunes
        setupMondayTaskClickListeners()
    }

    private fun setupMondayTaskClickListeners() {
        // Asignar listeners solo a las tareas del lunes
        binding.taskLunes1.setOnClickListener {
            openTaskDetail("Limpiar cocina")
        }

        binding.taskLunes2.setOnClickListener {
            openTaskDetail("Lavar porche")
        }

        binding.taskLunes3.setOnClickListener {
            openTaskDetail("Bañar perro")
        }
    }

    private fun openTaskDetail(taskName: String) {
        val intent = Intent(activity, DetalleTareaActivity::class.java).apply {
            putExtra("TASK_NAME", taskName)
        }
        startActivity(intent)
    }

    private fun updateWeekNumber(isNext: Boolean) {
        val currentWeek = binding.textWeekNumber.text.toString()
            .replace("Semana ", "").toIntOrNull() ?: 1

        val newWeek = if (isNext) currentWeek + 1 else currentWeek - 1
        if (newWeek > 0) {
            binding.textWeekNumber.text = "Semana $newWeek"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}