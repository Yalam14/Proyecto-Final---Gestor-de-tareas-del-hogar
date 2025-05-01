package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.domain.Home
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.domain.User


class RegistrarseActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var userRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrarse)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        userRef = database.getReference("users")

        val user: EditText = findViewById(R.id.editTextUsuario)
        val mail: EditText = findViewById(R.id.editTextMail)
        val password: EditText = findViewById(R.id.editTextContrasenia)
        val confirmPassword: EditText = findViewById(R.id.editTextConfirmarContrasena)

        val registar: Button = findViewById(R.id.buttonRegistrarse)

        registar.setOnClickListener {
            if (user.text.isEmpty() || mail.text.isEmpty() || password.text.isEmpty() || confirmPassword.text.isEmpty()) {
                Toast.makeText(baseContext, "Todos los campos deben llenarse", Toast.LENGTH_SHORT)
                    .show()
            } else if (!password.text.toString().equals(confirmPassword.text.toString())) {
                Toast.makeText(baseContext, "Las contraseñas no coinciden", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(baseContext, "Registro realizado correctamente", Toast.LENGTH_SHORT)
                    .show()
                signIn(user.text.toString(), mail.text.toString(), password.text.toString())
            }
        }
    }

    private fun signIn(user: String, email: String, password: String) {
        Log.d("INFO REGISTRO", "email: ${email}, password: ${password}")
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                saveUser(User(auth.currentUser?.uid?: "", user, email))
                Log.d("INFO REGISTRO", "signInWithEmail:success")
                val user = auth.currentUser
                val intent = Intent(this, HogarActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } else {
                Log.w("ERROR REGISTRO", "signInWithEmail:failure", task.exception)
                Toast.makeText(baseContext, "El registro falló", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUser(user: User) {
        userRef.push().setValue(user)
    }

}