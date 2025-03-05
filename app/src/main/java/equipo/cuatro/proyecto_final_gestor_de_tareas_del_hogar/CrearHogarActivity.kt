package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar.databinding.CrearHogarBinding

class CrearHogarActivity : AppCompatActivity() {


    private var iconoSeleccionado: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = CrearHogarBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val editTextNombreHogar = binding.editTextNombreHogar
        val icon1 = binding.icon1
        val icon2 = binding.icon2
        val icon3 = binding.icon3
        val icon4 = binding.icon4
        val botonGuardar = binding.botonConfigurar

        val icons = listOf(icon1, icon2, icon3, icon4)
        val iconIds = listOf(
            R.drawable.baseline_account_balance_24,
            R.drawable.baseline_add_business_24,
            R.drawable.baseline_add_home_24,
            R.drawable.baseline_bedroom_baby_24
        )


        fun seleccionarIcono(icon: ImageView, index: Int) {
            icons.forEach { it.isSelected = false }
            icon.isSelected = true
            iconoSeleccionado = iconIds[index]
        }


        icons.forEachIndexed { index, icon ->
            icon.setOnClickListener {
                seleccionarIcono(icon, index)
            }
        }


        botonGuardar.setOnClickListener {
            val nombreHogar = editTextNombreHogar.text.toString().trim()

            if (nombreHogar.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa el nombre del hogar.", Toast.LENGTH_SHORT).show()
            } else if (iconoSeleccionado == -1) {
                Toast.makeText(this, "Por favor, selecciona un icono.", Toast.LENGTH_SHORT).show()
            } else {

                Toast.makeText(this, "Hogar guardado: $nombreHogar", Toast.LENGTH_SHORT).show()

                editTextNombreHogar.text.clear()
                iconoSeleccionado = -1
                icons.forEach { it.isSelected = false }
            }
        }
    }
}
