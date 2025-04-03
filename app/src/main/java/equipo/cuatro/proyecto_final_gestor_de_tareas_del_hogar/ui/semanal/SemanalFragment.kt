package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.ui.semanal



import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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
            // Botón anterior
            buttonPreviousWeek.setOnClickListener {
                // Lógica para ir a la semana anterior
                updateWeekNumber(false)
            }

            // Botón siguiente
            buttonNextWeek.setOnClickListener {
                // Lógica para ir a la semana siguiente
                updateWeekNumber(true)
            }
        }
    }

    private fun updateWeekNumber(isNext: Boolean) {
        // Aquí iría la lógica para actualizar el número de semana
        // Por ahora solo es un ejemplo
        val currentWeek = binding.textWeekNumber.text.toString()
            .replace("Semana ", "").toIntOrNull() ?: 1

        val newWeek = if (isNext) currentWeek + 1 else currentWeek - 1
        if (newWeek > 0) {
            binding.textWeekNumber.text = "Semana $newWeek"
        }

        // También deberías actualizar las tareas según la semana seleccionada
        // updateTasksForWeek(newWeek)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}