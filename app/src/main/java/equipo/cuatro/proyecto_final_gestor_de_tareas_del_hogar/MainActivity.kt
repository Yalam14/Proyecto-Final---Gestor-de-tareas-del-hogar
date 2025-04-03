package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class MainActivity : AppCompatActivity() {
    private lateinit var homeRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)

        auth = FirebaseAuth.getInstance()

        val email: EditText = findViewById(R.id.editTextUsuario)
        val password: EditText = findViewById(R.id.editTextContrasena)

        // Botón para ir a la pantalla de Registrarse
        val buttonRegistrarse = findViewById<Button>(R.id.buttonRegistrarse)
        buttonRegistrarse.setOnClickListener {
            val intent = Intent(this@MainActivity, RegistrarseActivity::class.java)
            startActivity(intent)
        }

        val buttonIniciarSesion = findViewById<Button>(R.id.buttonIniciarSesion)
        buttonIniciarSesion.setOnClickListener {
            login(email.text.toString(), password.text.toString())
        }

    }

    fun goToMain(user: FirebaseUser) {
        val intent = Intent(this, HogarActivity::class.java)
        intent.putExtra("user", user.email)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    fun goToMainExistentes(user: FirebaseUser) {
        val intent = Intent(this, HogaresExistentesActivity::class.java)
        intent.putExtra("user", user.email)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()

        homeRef = FirebaseDatabase.getInstance().getReference("homes")

        val currentUser = auth.currentUser
        if (currentUser != null) {
            hayHogares(homeRef) { vacia ->
                if (vacia) {
                    goToMain(currentUser)
                }else {
                    goToMainExistentes(currentUser)
                }
            }
        }
    }

    fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                goToMain(user!!)
            } else {
                Toast.makeText(baseContext, "Email y/o contraseña incorrectos", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    fun hayHogares(homeRef: DatabaseReference, callback: (Boolean) -> Unit) {
        homeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback(!snapshot.exists() || !snapshot.hasChildren())
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false)
            }
        })
    }
}