package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.databinding.ActivityTareasBinding
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.domain.Home

class TareasActivity : AppCompatActivity() {
    private lateinit var homeRef: DatabaseReference
    private lateinit var binding: ActivityTareasBinding
    private lateinit var hogarId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        homeRef = FirebaseDatabase.getInstance().getReference("homes")

        binding = ActivityTareasBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbar))

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_tareas)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_diario, R.id.navigation_semanal
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val bundle = intent.extras

        if (bundle != null) {
            hogarId = bundle.getString("HOGAR_ID").toString()
        }

        getHomeById(hogarId) { home ->
            if (home != null) {
                findViewById<TextView>(R.id.texthome).text = home.name
            } else {
                Toast.makeText(this, "Hogar no encontrado", Toast.LENGTH_SHORT).show()
            }
        }

        val agregarTarea: FloatingActionButton = findViewById(R.id.fab)

        agregarTarea.setOnClickListener {
            val intent = Intent(this, AgregarTareaActivity::class.java)
            startActivity(intent)
        }
    }

    fun getHomeById(homeId: String, callback: (Home?) -> Unit) {
        homeRef.child(homeId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val home = snapshot.getValue(Home::class.java)
                callback(home)
            } else {
                callback(null)
            }
        }.addOnFailureListener {
            callback(null)
        }
    }
}