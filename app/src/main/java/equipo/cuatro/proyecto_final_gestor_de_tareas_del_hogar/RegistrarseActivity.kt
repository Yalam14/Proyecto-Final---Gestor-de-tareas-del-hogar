package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth


class RegistrarseActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registrarse)

        auth = FirebaseAuth.getInstance()

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
                signIn(mail.text.toString(), password.text.toString())
            }
        }
    }

    fun signIn(email: String, password: String) {
        Log.d("INFO REGISTRO", "email: ${email}, password: ${password}")
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
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

}