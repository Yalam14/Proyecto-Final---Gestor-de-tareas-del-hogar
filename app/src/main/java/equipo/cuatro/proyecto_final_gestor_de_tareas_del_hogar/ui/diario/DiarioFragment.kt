package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.ui.diario

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
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
import java.util.*

class DiarioFragment : Fragment() {

    private var _binding: FragmentDiarioBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: DiarioViewModel
    private lateinit var homeId: String
    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(DiarioViewModel::class.java)
        _binding = FragmentDiarioBinding.inflate(inflater, container, false)

        homeId = arguments?.getString("HOME_ID") ?: run {
            Log.e("DiarioFragment", "No se recibió HOME_ID en los argumentos")
            requireActivity().finish()
            return binding.root
        }

        val nombreHogar = arguments?.getString("HOME_NAME") ?: ""
        binding.texthome.text = nombreHogar

        configurarNavegacionDias()
        configurarSelectorFecha()
        configurarObservadores()
        cargarTareasIniciales()

        return binding.root
    }

    private fun configurarSelectorFecha() {
        binding.btnSelectDate.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    viewModel.actualizarFecha(calendar)
                    viewModel.cargarTareasParaDia(obtenerNombreDia(calendar), homeId)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun obtenerNombreDia(calendar: Calendar): String {
        val dias = listOf("Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
        return dias[calendar.get(Calendar.DAY_OF_WEEK) - 1]
    }

    private fun configurarNavegacionDias() {
        binding.btnPrevDay.setOnClickListener {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            viewModel.actualizarFecha(calendar)
            viewModel.cargarTareasDiaAnterior(homeId)
        }

        binding.btnNextDay.setOnClickListener {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            viewModel.actualizarFecha(calendar)
            viewModel.cargarTareasDiaSiguiente(homeId)
        }
    }

    private fun configurarObservadores() {
        viewModel.diaActual.observe(viewLifecycleOwner) { dia ->
            binding.txtDay.text = "${dia}, ${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}"
        }

        viewModel.tareas.observe(viewLifecycleOwner) { tareas ->
            when {
                tareas == null -> mostrarErrorCarga()
                tareas.isEmpty() -> mostrarMensajeSinTareas()
                else -> mostrarTareas(tareas)
            }
        }

        viewModel.estaCargando.observe(viewLifecycleOwner) { estaCargando ->
            binding.progressTasks.visibility = if (estaCargando) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun cargarTareasIniciales() {
        val diaActual = obtenerNombreDia(calendar)
        Log.d("DiarioFragment", "Cargando tareas para día: $diaActual, homeId: $homeId")
        viewModel.cargarTareasParaDia(diaActual, homeId)
    }

    private fun mostrarErrorCarga() {
        binding.taskContainer.removeAllViews()
        TextView(requireContext()).apply {
            text = "Error al cargar tareas"
            textSize = 16f
            setTextColor(Color.RED)
            gravity = View.TEXT_ALIGNMENT_CENTER
        }.also { binding.taskContainer.addView(it) }
    }

    private fun mostrarMensajeSinTareas() {
        binding.taskContainer.removeAllViews()
        TextView(requireContext()).apply {
            text = "No hay tareas para este día"
            textSize = 16f
            setTextColor(Color.BLACK)
            gravity = View.TEXT_ALIGNMENT_CENTER
            setPadding(0, 32, 0, 32)
        }.also { binding.taskContainer.addView(it) }
    }

    private fun mostrarTareas(tareas: List<Task>) {
        binding.taskContainer.removeAllViews()
        Log.d("DiarioFragment", "Mostrando ${tareas.size} tareas")

        tareas.forEachIndexed { index, tarea ->
            binding.taskContainer.addView(crearVistaTarea(tarea))

            if (index < tareas.size - 1) {
                agregarSeparador()
            }
        }
    }

    private fun agregarSeparador() {
        View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
            ).apply {
                setMargins(16, 8, 16, 8)
            }
            setBackgroundColor(Color.parseColor("#EEEEEE"))
        }.also { binding.taskContainer.addView(it) }
    }

    private fun crearVistaTarea(tarea: Task): View {
        return LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 8, 16, 8)
            }

            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            background = ContextCompat.getDrawable(requireContext(), R.drawable.task_background)
            setOnClickListener { navegarADetalleTarea(tarea) }

            // Nombre de la tarea
            TextView(requireContext()).apply {
                text = tarea.name
                textSize = 18f
                setTextColor(Color.WHITE)
                setTypeface(null, Typeface.BOLD)
                addView(this)
            }

            // Descripción de la tarea
            TextView(requireContext()).apply {
                text = tarea.description
                textSize = 14f
                setTextColor(Color.WHITE)
                setPadding(0, 8, 0, 0)
                addView(this)
            }

            // Asignados y estado
            TextView(requireContext()).apply {
                text = "Asignado a: ${tarea.assignedTo.joinToString(", ")} • ${if(tarea.completed) "✓ Completada" else "○ Pendiente"}"
                textSize = 12f
                setTextColor(Color.WHITE)
                setPadding(0, 8, 0, 0)
                addView(this)
            }
        }
    }

    private fun actualizarBarraProgreso(tareas: List<Task>) {
        binding.progressTasks.progress = if (tareas.isNotEmpty()) {
            val tareasCompletadas = tareas.count { it.completed }
            (tareasCompletadas.toFloat() / tareas.size * 100).toInt().also { progreso ->
                binding.progressTasks.progressTintList = ContextCompat.getColorStateList(
                    requireContext(),
                    if (progreso == 100) R.color.green else R.color.black
                )
            }
        } else {
            0
        }
    }

    private fun navegarADetalleTarea(tarea: Task) {
        Intent(requireContext(), DetalleTareaActivity::class.java).apply {
            putExtra("taskId", tarea.id)
            putExtra("taskName", tarea.name)
            putExtra("taskDescription", tarea.description)
            putStringArrayListExtra("assignedTo", ArrayList(tarea.assignedTo))
            putExtra("completed", tarea.completed)
        }.also { startActivity(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}