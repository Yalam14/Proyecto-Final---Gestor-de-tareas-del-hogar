package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.HogaresExistentesActivity
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.domain.Home
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.domain.User

class UnirseActivity : AppCompatActivity() {
    private lateinit var homeRef: DatabaseReference
    private lateinit var userRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_unirse)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        homeRef = FirebaseDatabase.getInstance().getReference("homes")
        userRef = FirebaseDatabase.getInstance().getReference("users")

        val etCodigo: EditText = findViewById(R.id.et_codigo)
        val btnUnirse: Button = findViewById(R.id.btn_unirse)

        btnUnirse.setOnClickListener {
            val codigo = etCodigo.text.toString()

            if (codigo.isNotEmpty()) {
                buscarYUnirseACodigo(codigo)
            } else {
                Toast.makeText(this, "Ingresa un código válido", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun buscarYUnirseACodigo(codigo: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Toast.makeText(this@UnirseActivity, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        getNameUser(userId) { username ->
            if (username.isEmpty()) {
                Toast.makeText(this@UnirseActivity, "Error al obtener usuario", Toast.LENGTH_SHORT).show()
                return@getNameUser
            }

            homeRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var codigoEncontrado = false

                    for (hogarSnapshot in snapshot.children) {
                        val hogar = hogarSnapshot.getValue(Home::class.java)
                        if (hogar?.code == codigo) {
                            codigoEncontrado = true

                            if (hogar.participants.containsKey(userId)) {
                                Toast.makeText(
                                    this@UnirseActivity,
                                    "Ya eres parte de este hogar",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                hogarSnapshot.ref.child("participants/$userId")
                                    .setValue(username)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            this@UnirseActivity,
                                            "¡Te has unido al hogar!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        startActivity(
                                            Intent(
                                                this@UnirseActivity,
                                                HogaresExistentesActivity::class.java
                                            )
                                        )
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            this@UnirseActivity,
                                            "Error al unirse: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                            break
                        }
                    }

                    if (!codigoEncontrado) {
                        Toast.makeText(this@UnirseActivity, "Código no encontrado", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@UnirseActivity, "Error: ${error.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
        }
    }


    private fun getNameUser(userId: String, callback: (String) -> Unit): String {
        var username = ""

        userRef.orderByChild("id").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (userSnap in snapshot.children) {
                            val user = userSnap.getValue(User::class.java)
                            username = user?.user.toString()
                            callback(username)
                        }
                    }
                    callback("")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error en la consulta: ${error.message}")
                    callback("")
                }
            })

        return username
    }
}