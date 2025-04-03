package equipo.cuatro.proyecto_final_gestor_de_tareas_del_hogar

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class AgregarTareaActivity : AppCompatActivity() {

    private var diaSeleccionado: CheckBox? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_tarea)

        // Configurar listeners para todos los días
        setupDayCheckBox(R.id.cb_lunes, R.id.layout_lunes_miembros)
        setupDayCheckBox(R.id.cb_martes, R.id.layout_martes_miembros)
        setupDayCheckBox(R.id.cb_miercoles, R.id.layout_miercoles_miembros)
        setupDayCheckBox(R.id.cb_jueves, R.id.layout_jueves_miembros)
        setupDayCheckBox(R.id.cb_viernes, R.id.layout_viernes_miembros)
        setupDayCheckBox(R.id.cb_sabado, R.id.layout_sabado_miembros)
        setupDayCheckBox(R.id.cb_domingo, R.id.layout_domingo_miembros)

        val btnAgregar: Button = findViewById(R.id.btn_agregar)
        btnAgregar.setOnClickListener {
            val intent = Intent(this, TareasActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupDayCheckBox(checkBoxId: Int, layoutId: Int) {
        val checkBox: CheckBox = findViewById(checkBoxId)
        val layout: LinearLayout = findViewById(layoutId)

        // Estilo inicial
        checkBox.buttonTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.checkbox_unselected))

        checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                // Deseleccionar el día anterior
                diaSeleccionado?.isChecked = false
                diaSeleccionado = buttonView as CheckBox

                // Cambiar color a negro
                buttonView.buttonTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.checkbox_selected))

                // Mostrar miembros
                layout.visibility = LinearLayout.VISIBLE
            } else {
                // Cambiar color a gris
                buttonView.buttonTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.checkbox_unselected))

                // Ocultar miembros
                layout.visibility = LinearLayout.GONE

                // Si era el día seleccionado, limpiar referencia
                if (buttonView == diaSeleccionado) {
                    diaSeleccionado = null
                }
            }
        }
    }
}