package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.ui.diario

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.DetalleTareaActivity
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.R
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.TareasActivity
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.databinding.FragmentDiarioBinding
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.domain.Task
import java.util.*

class DiarioFragment : Fragment() {
    private var _binding: FragmentDiarioBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: DiarioViewModel
    private lateinit var homeId: String
    private val calendar = Calendar.getInstance()
    private var nombreHogar: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(DiarioViewModel::class.java)
        _binding = FragmentDiarioBinding.inflate(inflater, container, false)

        // Obtener HOME_ID con validación robusta - FORMA CORRECTA
        homeId = arguments?.getString("HOME_ID")?.takeIf { it.isNotEmpty() } ?:
                (activity as? TareasActivity)?.hogarId?.takeIf { it.isNotEmpty() } ?: run {
            Toast.makeText(context, "Error: No se identificó el hogar", Toast.LENGTH_LONG).show()
            requireActivity().finish()
            return binding.root
        }

        nombreHogar = arguments?.getString("HOME_NAME") ?: "Mi Hogar"
        binding.texthome.text = nombreHogar

        Log.d("DiarioFragment", "HOME_ID válido: $homeId")

        configurarUI()
        configurarObservadores()
        cargarTareasIniciales()

        return binding.root

        nombreHogar = arguments?.getString("HOME_NAME") ?: "Mi Hogar"
        binding.texthome.text = nombreHogar

        Log.d("DiarioFragment", "HOME_ID válido: $homeId")

        configurarUI()
        configurarObservadores()
        cargarTareasIniciales()

        return binding.root
    }

    private fun configurarUI() {
        configurarNavegacionDias()
        configurarSelectorFecha()
        configurarBarraProgreso()
    }

    private fun configurarBarraProgreso() {
        binding.progressTasks.apply {
            max = 100
            progress = 0
            progressTintList = ContextCompat.getColorStateList(requireContext(), R.color.black)
        }
    }

    private fun configurarSelectorFecha() {
        binding.btnSelectDate.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    viewModel.actualizarFecha(calendar)
                    cargarTareasParaDia(obtenerNombreDia(calendar))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun obtenerNombreDia(calendar: Calendar): String {
        return listOf("Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")[
            calendar.get(Calendar.DAY_OF_WEEK) - 1
        ]
    }

    private fun configurarNavegacionDias() {
        binding.btnPrevDay.setOnClickListener {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            viewModel.actualizarFecha(calendar)
            cargarTareasParaDia(obtenerNombreDia(calendar))
        }

        binding.btnNextDay.setOnClickListener {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            viewModel.actualizarFecha(calendar)
            cargarTareasParaDia(obtenerNombreDia(calendar))
        }
    }

    private fun cargarTareasParaDia(dia: String) {
        Log.d("DiarioFragment", "Cargando tareas para: $dia, homeId: $homeId")
        viewModel.cargarTareasParaDia(dia, homeId)
    }

    private fun configurarObservadores() {
        viewModel.diaActual.observe(viewLifecycleOwner) { dia ->
            binding.txtDay.text = "${dia}, ${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}"
        }

        viewModel.tareas.observe(viewLifecycleOwner) { tareas ->
            when {
                tareas == null -> mostrarErrorCarga("Error al cargar tareas")
                tareas.isEmpty() -> mostrarMensajeSinTareas()
                else -> mostrarTareas(tareas)
            }
            actualizarBarraProgreso(tareas ?: emptyList())
        }

        viewModel.estaCargando.observe(viewLifecycleOwner) { estaCargando ->
            binding.progressTasks.visibility = if (estaCargando) View.VISIBLE else View.INVISIBLE
            if (estaCargando) binding.taskContainer.removeAllViews()
        }
    }

    private fun cargarTareasIniciales() {
        val diaActual = obtenerNombreDia(calendar)
        viewModel.cargarTareasParaDia(diaActual, homeId)
    }

    private fun mostrarErrorCarga(mensaje: String = "Error al cargar datos") {
        binding.taskContainer.removeAllViews()
        TextView(requireContext()).apply {
            text = mensaje
            textSize = 16f
            setTextColor(Color.RED)
            gravity = Gravity.CENTER
            setPadding(0, 32, 0, 32)
        }.also { binding.taskContainer.addView(it) }
    }

    private fun mostrarMensajeSinTareas() {
        binding.taskContainer.removeAllViews()
        TextView(requireContext()).apply {
            text = "No hay tareas programadas para hoy"
            textSize = 16f
            setTextColor(ContextCompat.getColor(context, R.color.gray))
            gravity = Gravity.CENTER
            setPadding(0, 32, 0, 32)
        }.also { binding.taskContainer.addView(it) }
    }

    private fun mostrarTareas(tareas: List<Task>) {
        binding.taskContainer.removeAllViews()
        Log.d("DiarioFragment", "Mostrando ${tareas.size} tareas")

        tareas.sortedByDescending { it.timestamp }.forEachIndexed { index, tarea ->
            binding.taskContainer.addView(crearVistaTarea(tarea))
            if (index < tareas.size - 1) agregarSeparador()
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
            background = ContextCompat.getDrawable(requireContext(), R.color.light_gray)
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
                text = tarea.description.takeIf { it.isNotEmpty() } ?: "Sin descripción"
                textSize = 14f
                setTextColor(Color.WHITE)
                setPadding(0, 8, 0, 0)
                addView(this)
            }

            // Asignados y estado
            TextView(requireContext()).apply {
                text = buildString {
                    append("Asignado a: ")
                    append(tarea.assignedTo.joinToString(", ").takeIf { it.isNotEmpty() } ?: "Sin asignar")
                    append(" • ")
                    append(if (tarea.completed) "✓ Completada" else "○ Pendiente")
                }
                textSize = 12f
                setTextColor(Color.WHITE)
                setPadding(0, 8, 0, 0)
                addView(this)
            }
        }
    }

    private fun actualizarBarraProgreso(tareas: List<Task>) {
        val progreso = if (tareas.isNotEmpty()) {
            val completadas = tareas.count { it.completed }
            (completadas.toFloat() / tareas.size * 100).toInt()
        } else 0

        binding.progressTasks.apply {
            this.progress = progreso
            progressTintList = ContextCompat.getColorStateList(
                requireContext(),
                if (progreso == 100) R.color.green else R.color.black
            )
        }
    }

    private fun navegarADetalleTarea(tarea: Task) {
        try {
            Intent(requireContext(), DetalleTareaActivity::class.java).apply {
                putExtra("taskId", tarea.id)
                putExtra("taskName", tarea.name)
                putExtra("taskDescription", tarea.description)
                putStringArrayListExtra("assignedTo", ArrayList(tarea.assignedTo))
                putExtra("completed", tarea.completed)
                putExtra("homeId", homeId)
                putExtra("homeName", nombreHogar)
            }.also { startActivity(it) }
        } catch (e: Exception) {
            Log.e("DiarioFragment", "Error al navegar a detalle", e)
            Toast.makeText(context, "Error al abrir la tarea", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}