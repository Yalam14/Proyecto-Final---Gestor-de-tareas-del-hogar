package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.databinding.CrearHogarBinding
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.domain.Home
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.domain.User



class CrearHogarActivity : AppCompatActivity() {
    private lateinit var homeRef: DatabaseReference
    private lateinit var userRef: DatabaseReference
    private var iconoDatabase: String = ""
    private var iconoSeleccionado: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = CrearHogarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        homeRef = FirebaseDatabase.getInstance().getReference("homes")
        userRef = FirebaseDatabase.getInstance().getReference("users")

        val editTextNombreHogar = binding.editTextNombreHogar
        val botonGuardar = binding.botonConfigurar

        val icons = listOf(
            binding.icon1, binding.icon2, binding.icon3, binding.icon4,
            binding.icon5, binding.icon6, binding.icon7, binding.icon8,
            binding.icon9, binding.icon10
        )

        val iconIds = listOf(
            R.drawable.baseline_account_balance_24,
            R.drawable.baseline_add_business_24,
            R.drawable.baseline_add_home_24,
            R.drawable.baseline_bedroom_baby_24,
            R.drawable.bar,
            R.drawable.dormitorio,
            R.drawable.fabrica,
            R.drawable.factory,
            R.drawable.mosque,
            R.drawable.templo
        )

        val iconStrs = listOf(
            "baseline_account_balance_24",
            "baseline_add_business_24",
            "baseline_add_home_24",
            "baseline_bedroom_baby_24",
            "bar",
            "dormitorio",
            "fabrica",
            "factory",
            "mosque",
            "templo"
        )

        fun seleccionarIcono(icon: ImageView, index: Int) {
            icons.forEach { it.isSelected = false }
            icon.isSelected = true
            iconoSeleccionado = iconIds[index]
            iconoDatabase = iconStrs[index]
            Log.d("IconoSeleccionado", "Icono seleccionado: $iconoDatabase")
        }

        icons.forEachIndexed { index, icon ->
            icon.setOnClickListener {
                seleccionarIcono(icon, index)
            }
        }

        botonGuardar.setOnClickListener {
            val nombreHogar = editTextNombreHogar.text.toString().trim()
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

            if (nombreHogar.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa el nombre del hogar.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (iconoSeleccionado == -1) {
                Toast.makeText(this, "Por favor, selecciona un icono.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            getNameUser(userId) { username ->
                if (username.isEmpty()) {
                    Toast.makeText(this, "Error al obtener información del usuario", Toast.LENGTH_SHORT).show()
                    return@getNameUser
                }

                val hogar = Home(
                    name = nombreHogar,
                    icon = iconoDatabase,
                    code = generarCódigo(),
                    createdBy = userId,
                    participants = mapOf(userId to username)
                )

                Log.d("HomeObject", "Home a guardar: $hogar")

                saveHomeToDatabase(hogar) {
                    editTextNombreHogar.text.clear()
                    iconoSeleccionado = -1
                    icons.forEach { it.isSelected = false }

                    val intent = Intent(this, HogaresExistentesActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    private fun saveHomeToDatabase(home: Home, onComplete: () -> Unit) {
        homeRef.push().setValue(home)
            .addOnSuccessListener {
                Log.d("Firebase", "Hogar guardado correctamente")
                Toast.makeText(baseContext, "Hogar registrado correctamente", Toast.LENGTH_SHORT).show()
                onComplete()
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error al guardar hogar", e)
                Toast.makeText(baseContext, "Error al guardar hogar", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getNameUser(userId: String, callback: (String) -> Unit) {
        userRef.orderByChild("id").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (userSnap in snapshot.children) {
                            val user = userSnap.getValue(User::class.java)
                            user?.user?.let { username ->
                                callback(username)
                                return
                            }
                        }
                    }
                    callback("")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error en la consulta: ${error.message}")
                    callback("")
                }
            })
    }

    private fun generarCódigo(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..5).map { chars.random() }.joinToString("")
    }
}