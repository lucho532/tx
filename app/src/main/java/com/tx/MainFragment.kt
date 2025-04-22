package com.tx

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.tx.database.DatabaseClient
import com.tx.entity.Movimiento
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors


class MainFragment : Fragment() {

    private var fechaJornada: String? = null
    private lateinit var tvUltimoMovimiento: TextView
    private lateinit var totalGeneralInfo: TextView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_main, container, false)


        // Referencias a los elementos de la vista
        val etFecha = root.findViewById<EditText>(R.id.etFecha)
        val etValor = root.findViewById<EditText>(R.id.etValor)
        val radioGroupPago = root.findViewById<RadioGroup>(R.id.radioGroupPago)
        val agregarButton = root.findViewById<Button>(R.id.agregar_button)
        val btnNavigate = root.findViewById<Button>(R.id.btnNavigate)
        val btnNavigate2 = root.findViewById<Button>(R.id.btnNavigate2)

        tvUltimoMovimiento = root.findViewById(R.id.tvUltimoMovimiento)
/*
        totalGeneralInfo = root.findViewById(R.id.total_general_info)
*/


        // Establecer la fecha inicial de la jornada
        if (fechaJornada.isNullOrEmpty()) {
            fechaJornada = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        }
        etFecha.setText(fechaJornada)

        // Mostrar DatePickerDialog al hacer clic en el campo
        etFecha.setOnClickListener {
            mostrarDatePickerDialog(etFecha)
            cargarUltimoMovimiento(etFecha)
        }

        // Acción botón "Agregar Movimiento"
        agregarButton.setOnClickListener {
            val fecha = etFecha.text.toString().trim()
            val valorStr = etValor.text.toString().trim()

            if (fecha.isEmpty() || valorStr.isEmpty()) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val valor = valorStr.toDoubleOrNull()
            if (valor == null) {
                Toast.makeText(requireContext(), "Valor no válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedId = radioGroupPago.checkedRadioButtonId
            if (selectedId == -1) {
                Toast.makeText(requireContext(), "Selecciona un método de pago", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedRadio = root.findViewById<RadioButton>(selectedId)
            val metodoDePago = selectedRadio.text.toString()
            val hora = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

            // Crear y guardar el movimiento
            val movimiento = Movimiento().apply {
                this.fecha = fecha
                this.valor = valor
                this.metodoDePago = metodoDePago
                this.hora = hora
            }

            Executors.newSingleThreadExecutor().execute {
                DatabaseClient.getInstance(requireContext())
                    .appDatabase
                    .movimientosDao()
                    .insert(movimiento)
            }

            Toast.makeText(requireContext(), "Movimiento guardado", Toast.LENGTH_SHORT).show()
            etValor.text.clear()
            radioGroupPago.clearCheck()
            cargarUltimoMovimiento(etFecha)
        }

        btnNavigate.setOnClickListener{
            val fecha = etFecha.text.toString().trim()
            if (fecha.isEmpty()) {
                Toast.makeText(requireContext(), "Selecciona una fecha", Toast.LENGTH_SHORT).show()
            } else {
                val bundle = Bundle().apply {
                    putString("fecha", fecha)
                }
                findNavController().navigate(R.id.action_mainFragment_to_firstFragment, bundle)
            }
        }
        btnNavigate2.setOnClickListener{
            val fecha = etFecha.text.toString().trim()
            if (fecha.isEmpty()) {
                Toast.makeText(requireContext(), "Selecciona una fecha", Toast.LENGTH_SHORT).show()
            } else {
                val bundle = Bundle().apply {
                    putString("fecha", fecha)
                }
                findNavController().navigate(R.id.action_mainFragment_to_secondFragment2, bundle)
            }
        }
        cargarUltimoMovimiento(etFecha)

        return root
    }

    private fun mostrarDatePickerDialog(etFecha: EditText) {
        val calendario = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val fechaSeleccionada = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                etFecha.setText(fechaSeleccionada)
                cargarUltimoMovimiento(etFecha)
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    // Función para mostrar el último movimiento
    fun cargarUltimoMovimiento(etFecha: EditText) {
        val fechaSeleccionada = etFecha.text.toString()
        Executors.newSingleThreadExecutor().execute {
            val ultimo = DatabaseClient.getInstance(requireContext())
                .appDatabase
                .movimientosDao()
                .obtenerUltimoPorFecha(fechaSeleccionada)

            activity?.runOnUiThread {
                if (ultimo != null) {
                    tvUltimoMovimiento.text = """
                    Último movimiento:
                    Fecha: ${ultimo.fecha}
                    Hora: ${ultimo.hora}
                    Valor: ${ultimo.valor}
                    Pago: ${ultimo.metodoDePago}
                    
                """.trimIndent()

                } else {
                    tvUltimoMovimiento.text = "No hay movimientos para el $fechaSeleccionada."
                }
            }
        }
    }
}