package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar

import android.content.Intent
import android.os.Bundle
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

        binding = ActivityTareasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // Obtener y validar el ID del hogar
        hogarId = intent.extras?.getString("HOGAR_ID")?.trim() ?: ""
        if (hogarId.isEmpty()) {
            Toast.makeText(this, "Error: No se proporcionó ID de hogar", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Configuraciones iniciales
        setupBottomNavigation()
        loadHomeInfo()
        setupFloatingActionButton()
    }

    private fun setupBottomNavigation() {
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_tareas)

        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navigation_diario, R.id.navigation_semanal)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Configurar argumentos para los fragments
        val navGraph = navController.navInflater.inflate(R.navigation.mobile_navigation)

        navGraph.forEach { dest ->
            if (dest.arguments?.containsKey("HOME_ID") == true) {
                dest.addArgument("HOME_ID", NavArgument.Builder()
                    .setDefaultValue(hogarId)
                    .build())
            }

            if (dest.arguments?.containsKey("HOME_NAME") == true) {
                // Puedes pasar también el nombre del hogar si es necesario
                dest.addArgument("HOME_NAME", NavArgument.Builder()
                    .setDefaultValue("")
                    .build())
            }
        }

        // Pasar los argumentos a los fragments
        val args = Bundle().apply {
            putString("HOME_ID", hogarId)
            // Si tienes el nombre del hogar, lo pasas aquí también
        }

        navController.setGraph(navGraph, args)
    }

    private fun loadHomeInfo() {
        homeRef.child(hogarId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val home = snapshot.getValue(Home::class.java)
                home?.let {
                    binding.toolbar.title = it.name
                    // Actualizar el nombre en los fragments si es necesario
                    updateFragmentsWithHomeInfo(it.name)
                } ?: showHomeNotFoundError()
            } else {
                showHomeNotFoundError()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error al cargar información del hogar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateFragmentsWithHomeInfo(homeName: String) {
        // Puedes usar esto para actualizar los fragments con información del hogar
        val navController = findNavController(R.id.nav_host_fragment_activity_tareas)
        val currentDest = navController.currentDestination
        currentDest?.let { dest ->
            val args = navController.getBackStackEntry(dest.id).savedStateHandle
            args.set("HOME_NAME", homeName)
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

    fun getHomeById(homeId: String, callback: (Home?) -> Unit) {
        homeRef.child(homeId).get().addOnSuccessListener { snapshot ->
            callback(snapshot.getValue(Home::class.java))
        }.addOnFailureListener {
            callback(null)
        }
    }
}