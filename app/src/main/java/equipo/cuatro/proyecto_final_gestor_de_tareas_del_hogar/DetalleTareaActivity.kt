package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.databinding.ActivityDetalleTareaBinding

class DetalleTareaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetalleTareaBinding
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleTareaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener datos del intent
        val taskId = intent.getStringExtra("taskId") ?: ""
        val taskName = intent.getStringExtra("taskName") ?: ""
        val taskDescription = intent.getStringExtra("taskDescription") ?: ""
        val homeId = intent.getStringExtra("homeId") ?: ""
        val assignedTo = intent.getStringArrayListExtra("assignedTo") ?: emptyList()
        val isCompleted = intent.getBooleanExtra("completed", false)
        val canEdit = intent.getBooleanExtra("canEdit", false)
        val creator = intent.getStringExtra("creator") ?: ""

        // Configurar vistas con los datos básicos
        binding.tvTitulo.text = taskName
        binding.etDescripcion.setText(taskDescription)

        if (creator == FirebaseAuth.getInstance().currentUser?.uid.toString()) {
            binding.btnEditar.visibility = View.VISIBLE
        }else if (canEdit) {
            binding.btnEditar.visibility = View.VISIBLE
        } else {
            binding.btnEditar.visibility = View.GONE
        }

        // Configurar estado de completado
        if (isCompleted) {
            binding.btnTerminar.text = "Marcar como pendiente"
            binding.btnTerminar.backgroundTintList =
                ContextCompat.getColorStateList(this, R.color.orange)
        } else {
            binding.btnTerminar.text = "Marcar como completada"
            binding.btnTerminar.backgroundTintList =
                ContextCompat.getColorStateList(this, R.color.green)
        }

        // Mostrar miembros asignados directamente desde el intent
        setupAssignedMembers(assignedTo)

        // OPCIÓN ALTERNATIVA: Si prefieres cargar los miembros desde Firebase
        // if (taskId.isNotEmpty() && homeId.isNotEmpty()) {
        //     loadAssignedMembers(taskId, homeId)
        // }

        // Configurar botones
        binding.btnTerminar.setOnClickListener {
            updateTaskStatus(taskId, !isCompleted)
            finish()
        }

        binding.btnEliminar.setOnClickListener {
            deleteTask(taskId)
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
                    binding.miembro1Checkbox.isChecked = false
                }
                1 -> {
                    binding.miembro2.visibility = View.VISIBLE
                    binding.miembro2Nombre.text = memberName
                    binding.miembro2Checkbox.isChecked = false
                }
                2 -> {
                    binding.miembro3.visibility = View.VISIBLE
                    binding.miembro3Nombre.text = memberName
                    binding.miembro3Checkbox.isChecked = false
                }
                3 -> {
                    binding.miembro4.visibility = View.VISIBLE
                    binding.miembro4Nombre.text = memberName
                    binding.miembro4Checkbox.isChecked = false
                }
            }
        }
    }

    private fun updateTaskStatus(taskId: String, isCompleted: Boolean) {
        if (taskId.isNotEmpty()) {
            database.child("tasks").child(taskId).child("completed")
                .setValue(isCompleted)
                .addOnSuccessListener {
                    Toast.makeText(this, "Estado de tarea actualizado", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al actualizar tarea", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun deleteTask(taskId: String) {
        if (taskId.isNotEmpty()) {
            database.child("tasks").child(taskId).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Tarea eliminada", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al eliminar tarea", Toast.LENGTH_SHORT).show()
                }
        }
    }
}