package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavArgument
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.databinding.ActivityTareasBinding
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.domain.Home

class TareasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTareasBinding
    private lateinit var homeRef: DatabaseReference
    private lateinit var hogarId: String
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicialización de Firebase
        database = Firebase.database
        homeRef = database.getReference("homes")

        // Configuración del ViewBinding
        binding = ActivityTareasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuración de la toolbar
        setSupportActionBar(binding.toolbar)

        // Obtener el ID del hogar del intent
        hogarId = intent.extras?.getString("HOGAR_ID").orEmpty()

        if (hogarId.isEmpty()) {
            Toast.makeText(this, "Error: No se proporcionó ID de hogar", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Configuración de la barra de navegación inferior
        setupBottomNavigation()

        // Cargar información del hogar
        loadHomeInfo()

        // Configurar el botón flotante para agregar tareas
        setupFloatingActionButton()
    }

    private fun setupBottomNavigation() {
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_tareas)

        // Configuración básica de navegación
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navigation_diario, R.id.navigation_semanal)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Configuración del grafo de navegación con argumentos
        val navGraph = navController.navInflater.inflate(R.navigation.mobile_navigation)

        // Establecer argumentos para todos los destinos que lo necesiten
        navGraph.forEach { dest ->
            if (dest.arguments?.containsKey("HOME_ID") == true) {
                dest.addArgument("HOME_ID", NavArgument.Builder()
                    .setDefaultValue(hogarId)
                    .build())
            }
        }

        navController.setGraph(navGraph, Bundle().apply {
            putString("HOME_ID", hogarId)
        })
    }

    private fun loadHomeInfo() {
        homeRef.child(hogarId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val home = snapshot.getValue(Home::class.java)
                home?.let {
                    binding.toolbar.title = it.name
                } ?: run {
                    showHomeNotFoundError()
                }
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

    private fun setupFloatingActionButton() {
        binding.fab.setOnClickListener {
            val intent = Intent(this, AgregarTareaActivity::class.java).apply {
                putExtra("HOME_ID", hogarId)
            }
            startActivity(intent)
        }
    }

    // Función auxiliar para obtener información del hogar
    fun getHomeById(homeId: String, callback: (Home?) -> Unit) {
        homeRef.child(homeId).get().addOnSuccessListener { snapshot ->
            callback(snapshot.getValue(Home::class.java))
        }.addOnFailureListener {
            callback(null)
        }
    }
}