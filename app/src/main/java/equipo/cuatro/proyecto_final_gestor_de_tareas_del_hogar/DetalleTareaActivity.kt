package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar

import android.content.Context
import android.graphics.Color
import android.hardware.input.InputManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.databinding.ActivityDetalleTareaBinding

class DetalleTareaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleTareaBinding
    private val database = FirebaseDatabase.getInstance().reference
    private var isEditing = false
    private lateinit var originalName: String
    private lateinit var originalDescription: String
    private lateinit var taskId: String
    private lateinit var homeId: String
    private var isCompleted = false
    private lateinit var creatorId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleTareaBinding.inflate(layoutInflater)
        setContentView(binding.root)



        // Obtener datos del intent
        taskId = intent.getStringExtra("taskId") ?: run {
            Toast.makeText(this, "Error: No se recibió ID de tarea", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Configurar vistas
        setupViews()

        // Configurar listeners
        setupListeners()

        // Debug de visibilidad
        Log.d("DebugView", "Visibilidad btnEditar: ${binding.btnEditar.visibility}")
        Log.d("DebugView", "¿BtnEditar es visible?: ${binding.btnEditar.isVisible}")
    }

    private fun setupViews() {
        // Obtener datos del intent
        originalName = intent.getStringExtra("taskName") ?: ""
        originalDescription = intent.getStringExtra("taskDescription") ?: ""
        homeId = intent.getStringExtra("homeId") ?: ""
        isCompleted = intent.getBooleanExtra("completed", false)
        creatorId = intent.getStringExtra("creator") ?: ""
        val assignedTo = intent.getStringArrayListExtra("assignedTo") ?: emptyList()

        // Configurar vistas con los datos
        binding.etTitulo.setText(originalName)
        binding.etDescripcion.setText(originalDescription)
        setupAssignedMembers(assignedTo)
        updateCompleteButton(isCompleted)

        // Verificar permisos de edición
        checkEditPermissions()
    }

    private fun checkEditPermissions() {
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val canEdit = intent.getBooleanExtra("canEdit", false)

        // Debug de permisos
        Log.d("DebugPermissions", "CurrentUser: $currentUser")
        Log.d("DebugPermissions", "CreatorId: $creatorId")
        Log.d("DebugPermissions", "CanEdit: $canEdit")

        if (currentUser == creatorId || canEdit) {
            binding.btnEditar.visibility = View.VISIBLE
            Log.d("DebugPermissions", "Mostrando botón de edición")
        } else {
            binding.btnEditar.visibility = View.GONE
            Log.d("DebugPermissions", "Ocultando botón de edición")
        }
    }

    private fun setupListeners() {
        binding.btnEditar.setOnClickListener {
            toggleEditMode(true)
        }

        binding.btnGuardar.setOnClickListener {
            saveChanges()
        }

        binding.btnCancelar.setOnClickListener {
            toggleEditMode(false)
            resetFields()
        }

        binding.btnTerminar.setOnClickListener {
            toggleTaskStatus()
        }

        binding.btnEliminar.setOnClickListener {
            confirmDeleteTask()
        }
    }

    private fun toggleEditMode(enable: Boolean) {
        isEditing = enable

        with(binding) {
            etTitulo.isEnabled = enable
            etDescripcion.isEnabled = enable
            btnEditar.visibility = if (enable) View.GONE else View.VISIBLE
            btnGuardar.visibility = if (enable) View.VISIBLE else View.GONE
            btnCancelar.visibility = if (enable) View.VISIBLE else View.GONE
            btnTerminar.visibility = if (enable) View.GONE else View.VISIBLE
            btnEliminar.visibility = if (enable) View.GONE else View.VISIBLE

            if (enable) {
                etTitulo.requestFocus()
                showKeyboard()
            } else {
                hideKeyboard()
            }
        }
    }

    private fun showKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etTitulo, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etTitulo.windowToken, 0)
    }

    private fun resetFields() {
        binding.etTitulo.setText(originalName)
        binding.etDescripcion.setText(originalDescription)
    }

    private fun saveChanges() {
        val newName = binding.etTitulo.text.toString().trim()
        val newDescription = binding.etDescripcion.text.toString().trim()

        if (newName.isEmpty()) {
            Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
            return
        }

        val updates = hashMapOf<String, Any>()
        if (newName != originalName) updates["name"] = newName
        if (newDescription != originalDescription) updates["description"] = newDescription

        if (updates.isNotEmpty()) {
            showProgress()
            database.child("tasks").child(taskId).updateChildren(updates)
                .addOnSuccessListener {
                    originalName = newName
                    originalDescription = newDescription
                    toggleEditMode(false)
                    hideProgress()
                    Toast.makeText(this, "Cambios guardados", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    hideProgress()
                    Toast.makeText(this, "Error al guardar cambios", Toast.LENGTH_SHORT).show()
                }
        } else {
            toggleEditMode(false)
        }
    }

    private fun toggleTaskStatus() {
        val newStatus = !isCompleted
        database.child("tasks").child(taskId).child("completed")
            .setValue(newStatus)
            .addOnSuccessListener {
                isCompleted = newStatus
                updateCompleteButton(newStatus)
                Toast.makeText(this, "Estado actualizado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al actualizar estado", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateCompleteButton(completed: Boolean) {
        binding.btnTerminar.text = if (completed) {
            "Marcar como pendiente"
        } else {
            "Marcar como completada"
        }
        binding.btnTerminar.backgroundTintList = ContextCompat.getColorStateList(
            this,
            if (completed) R.color.orange else R.color.green
        )
    }

    private fun confirmDeleteTask() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar tarea")
            .setMessage("¿Estás seguro de que deseas eliminar esta tarea?")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteTask()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteTask() {
        database.child("tasks").child(taskId).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Tarea eliminada", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al eliminar tarea", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupAssignedMembers(members: List<String>) {
        val memberViews = listOf(
            binding.miembro1 to binding.miembro1Nombre,
            binding.miembro2 to binding.miembro2Nombre,
            binding.miembro3 to binding.miembro3Nombre,
            binding.miembro4 to binding.miembro4Nombre
        )

        memberViews.forEach { it.first.visibility = View.GONE }

        members.take(4).forEachIndexed { index, member ->
            memberViews[index].first.visibility = View.VISIBLE
            memberViews[index].second.text = member
        }
    }

    private fun showProgress() {
        // Implementar diálogo de progreso
    }

    private fun hideProgress() {
        // Ocultar diálogo de progreso
    }
}