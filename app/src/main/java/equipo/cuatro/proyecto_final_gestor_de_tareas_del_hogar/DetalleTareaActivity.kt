package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.databinding.ActivityDetalleTareaBinding

class DetalleTareaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetalleTareaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleTareaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener datos del intent
        val taskId = intent.getStringExtra("taskId") ?: ""
        val taskName = intent.getStringExtra("taskName") ?: ""
        val taskDescription = intent.getStringExtra("taskDescription") ?: ""
        val assignedTo = intent.getStringArrayListExtra("assignedTo") ?: emptyList()
        val isCompleted = intent.getBooleanExtra("completed", false)

        // Configurar vistas con los datos
        binding.tvTitulo.text = taskName
        binding.etDescripcion.setText(taskDescription)

        // Configurar estado de completado
        if (isCompleted) {
            binding.btnTerminar.text = "Marcar como pendiente"
            binding.btnTerminar.backgroundTintList = ContextCompat.getColorStateList(this, R.color.orange)
        } else {
            binding.btnTerminar.text = "Marcar como completada"
            binding.btnTerminar.backgroundTintList = ContextCompat.getColorStateList(this, R.color.green)
        }

        // Configurar miembros
        setupAssignedMembers(assignedTo)

        // Configurar botones
        binding.btnTerminar.setOnClickListener {
            // Lógica para actualizar estado en Firebase
            Toast.makeText(this, "Estado de tarea actualizado", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.btnEliminar.setOnClickListener {
            // Lógica para eliminar tarea de Firebase
            Toast.makeText(this, "Tarea eliminada", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.btnEditar.setOnClickListener {
            // Lógica para editar tarea
            Toast.makeText(this, "Editar tarea", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupAssignedMembers(members: List<String>) {
        // Ocultar todos los miembros primero
        listOf(
            binding.miembro1, binding.miembro2,
            binding.miembro3, binding.miembro4
        ).forEach { it.visibility = View.GONE }

        // Mostrar solo los miembros asignados
        members.forEachIndexed { index, memberName ->
            when (index) {
                0 -> {
                    binding.miembro1.visibility = View.VISIBLE
                    binding.miembro1Nombre.text = memberName
                    // Configurar checkbox si es necesario
                    // binding.miembro1Checkbox.isChecked = ...
                }
                1 -> {
                    binding.miembro2.visibility = View.VISIBLE
                    binding.miembro2Nombre.text = memberName
                }
                2 -> {
                    binding.miembro3.visibility = View.VISIBLE
                    binding.miembro3Nombre.text = memberName
                }
                3 -> {
                    binding.miembro4.visibility = View.VISIBLE
                    binding.miembro4Nombre.text = memberName
                }
            }
        }
    }
}