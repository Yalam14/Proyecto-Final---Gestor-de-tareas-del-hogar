package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.domain.Home
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.domain.User

class HogaresExistentesActivity : AppCompatActivity() {
    private lateinit var homeRef: DatabaseReference
    private lateinit var containerHogares: LinearLayout

    var name: String = ""

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_hogares_existentes)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val fabMenu: FloatingActionButton = findViewById(R.id.fabMenu)
        fabMenu.setOnClickListener {
            val intent = Intent(this, HogarActivity::class.java)
            startActivity(intent)
        }

        homeRef = FirebaseDatabase.getInstance().getReference("homes")
        containerHogares = findViewById(R.id.container_hogares)
        cargarHogares()
    }

    private fun cargarHogares() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        homeRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                containerHogares.removeAllViews()

                for (hogarSnapshot in snapshot.children) {
                    val hogar = hogarSnapshot.getValue(Home::class.java)
                    hogar?.let { home ->
                        if (home.participants.containsKey(userId) == true) {
                            agregarHogarALista(home, hogarSnapshot.key)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@HogaresExistentesActivity,
                    "Error al cargar hogares: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun agregarHogarALista(hogar: Home, hogarId: String?) {
        val hogarLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            setPadding(
                resources.getDimensionPixelSize(R.dimen.activity_horizontal_margin_home),
                resources.getDimensionPixelSize(R.dimen.activity_vertical_margin_home),
                resources.getDimensionPixelSize(R.dimen.activity_horizontal_margin_home),
                resources.getDimensionPixelSize(R.dimen.activity_vertical_margin_home)
            )
            setOnClickListener {
                abrirHogar(hogarId)
            }
        }

        val icono = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                resources.getDimensionPixelSize(R.dimen.icon_size),
                resources.getDimensionPixelSize(R.dimen.icon_size)
            )
            setImageResource(obtenerDrawable(hogar.icon))
            setPadding(
                resources.getDimensionPixelSize(R.dimen.icon_padding),
                resources.getDimensionPixelSize(R.dimen.icon_padding),
                resources.getDimensionPixelSize(R.dimen.icon_padding),
                resources.getDimensionPixelSize(R.dimen.icon_padding)
            )
        }

        val nombre = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = resources.getDimensionPixelSize(R.dimen.text_margin)
                gravity = Gravity.CENTER_VERTICAL
            }
            text = hogar.name
            textSize = 16f
        }

        hogarLayout.addView(icono)
        hogarLayout.addView(nombre)

        val separador = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                resources.getDimensionPixelSize(R.dimen.separator_height)
            ).apply {
                topMargin = resources.getDimensionPixelSize(R.dimen.separator_margin)
                bottomMargin = resources.getDimensionPixelSize(R.dimen.separator_margin)
            }
            background =
                ContextCompat.getDrawable(this@HogaresExistentesActivity, R.color.lightGray)
        }

        containerHogares.addView(hogarLayout)
        containerHogares.addView(separador)
    }

    private fun obtenerDrawable(icono: String): Int {
        return when (icono) {
            "baseline_account_balance_24" -> R.drawable.baseline_account_balance_24
            "baseline_add_business_24" -> R.drawable.baseline_add_business_24
            "baseline_add_home_24" -> R.drawable.baseline_add_home_24
            "baseline_bedroom_baby_24" -> R.drawable.baseline_bedroom_baby_24
            "bar" -> R.drawable.bar
            "dormitorio" -> R.drawable.dormitorio
            "fabrica" -> R.drawable.fabrica
            "factory" -> R.drawable.factory
            "mosque" -> R.drawable.mosque
            "templo" -> R.drawable.templo
            else -> R.drawable.baseline_add_home_24
        }.also {
            Log.d("IconDebug", "Icono buscado: $icono, ResID encontrado: $it")
        }
    }

    private fun abrirHogar(hogarId: String?) {
        hogarId?.let {
            getHomeName(hogarId) { homename ->
                val intent = Intent(this, TareasActivity::class.java).apply {
                    putExtra("HOGAR_ID", it)
                    putExtra("HOME_NAME", homename)
                }
                startActivity(intent)
            }
        } ?: run {
            Toast.makeText(this, "Error: ID de hogar inválido", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getHomeName(hogarId: String, callback: (String) -> Unit) {
        homeRef.child(hogarId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val home = snapshot.getValue(Home::class.java)
                callback(home?.name ?: "Nombre no disponible")
            } else {
                callback("Hogar no encontrado")
            }
        }.addOnFailureListener {
            callback("Error al obtener nombre")
        }
    }

}