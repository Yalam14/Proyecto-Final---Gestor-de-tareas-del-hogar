package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavArgument
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.databinding.ActivityTareasBinding
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.domain.Home

class TareasActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTareasBinding
    private lateinit var homeRef: DatabaseReference
    var hogarId: String = ""
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = Firebase.database
        homeRef = database.getReference("homes")

        binding = ActivityTareasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // Validar y obtener el ID del hogar
        hogarId = intent.getStringExtra("HOGAR_ID")?.trim() ?: run {
            Toast.makeText(this, "Error: No se recibió ID de hogar", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Configurar NavController
        val navController = findNavController(R.id.nav_host_fragment_activity_tareas)
        val navGraph = navController.navInflater.inflate(R.navigation.mobile_navigation)

        // Pasar argumentos a los destinos
        navGraph.apply {
            findNode(R.id.navigation_diario)?.let { node ->
                node.addArgument("HOME_ID", NavArgument.Builder()
                    .setDefaultValue(hogarId)
                    .build())
                node.addArgument("HOME_NAME", NavArgument.Builder()
                    .setDefaultValue(intent.getStringExtra("HOME_NAME") ?: "")
                    .build())
            }

            findNode(R.id.navigation_semanal)?.let { node ->
                node.addArgument("HOME_ID", NavArgument.Builder()
                    .setDefaultValue(hogarId)
                    .build())
                node.addArgument("HOME_NAME", NavArgument.Builder()
                    .setDefaultValue(intent.getStringExtra("HOME_NAME") ?: "")
                    .build())
            }
        }

        navController.graph = navGraph

        // Configurar BottomNavigationView
        val navView: BottomNavigationView = binding.navView
        navView.setupWithNavController(navController)

        // Cargar información del hogar
        loadHomeInfo()

        // Configurar FAB
        binding.fab.setOnClickListener {
            val intent = Intent(this, AgregarTareaActivity::class.java).apply {
                putExtra("HOME_ID", hogarId)
            }
            startActivity(intent)
        }
    }

    private fun loadHomeInfo() {
        homeRef.child(hogarId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val home = snapshot.getValue(Home::class.java)
                home?.let {
                    binding.toolbar.title = it.name
                } ?: showHomeNotFoundError()
            } else {
                showHomeNotFoundError()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error al cargar información del hogar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showHomeNotFoundError() {
        Toast.makeText(this, "Hogar no encontrado", Toast.LENGTH_SHORT).show()
        finish()
    }

    fun getHomeById(homeId: String, callback: (Home?) -> Unit) {
        homeRef.child(homeId).get().addOnSuccessListener { snapshot ->
            callback(snapshot.getValue(Home::class.java))
        }.addOnFailureListener {
            callback(null)
        }
    }
}