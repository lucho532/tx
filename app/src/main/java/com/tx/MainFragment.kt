package com.tx

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.tx.database.DatabaseClient
import com.tx.entity.Movimiento
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors


class MainFragment : Fragment() {
    private var fechaJornada: String? = null
    private lateinit var tvUltimoMovimiento: TextView
    private lateinit var totalGeneralInfo: TextView
    private lateinit var totalPropina: TextView
    private lateinit var initTimeTextView: TextView
    private lateinit var finishmeTextView: TextView
    private lateinit var diferenceTimeTextView: TextView
    private lateinit var cronometroView: TextView
    private lateinit var btnStart: Button
    private lateinit var btnPause: Button
    private lateinit var btnStop: Button

    private var isRunning = false
    private var startTime = 0L
    private var elapsedTime = 0L
    private val handler = android.os.Handler()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_main, container, false)


        // Referencias a los elementos de la vista
        val etFecha = root.findViewById<EditText>(R.id.etFecha)
        val etValor = root.findViewById<EditText>(R.id.etValor)
        val etCobrado = root.findViewById<EditText>(R.id.etCobrado)
        val radioGroupPago = root.findViewById<RadioGroup>(R.id.radioGroupPago)
        val agregarButton = root.findViewById<Button>(R.id.agregar_button)
        val btnNavigate = root.findViewById<Button>(R.id.btnNavigate)
        val btnNavigate2 = root.findViewById<Button>(R.id.btnNavigate2)


        initTimeTextView = root.findViewById(R.id.initTime)
        finishmeTextView = root.findViewById(R.id.finishTime)
        diferenceTimeTextView = root.findViewById(R.id.totaltime)
        tvUltimoMovimiento = root.findViewById(R.id.tvUltimoMovimiento)

        totalGeneralInfo = root.findViewById(R.id.total_general_info)
        totalPropina = root.findViewById(R.id.total_propina)



        cronometroView = root.findViewById(R.id.cronometro)
        btnStart = root.findViewById(R.id.btnStart)
        btnPause = root.findViewById(R.id.btnPause)
        btnStop = root.findViewById(R.id.btnStop)


        btnStart.setOnClickListener {
            if (!isRunning) {
                startTime = System.currentTimeMillis()
                handler.post(runnable)
                isRunning = true

                val horaInicio = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                initTimeTextView.text = "Hora Inicio:\n$horaInicio"

                val fecha = etFecha.text.toString()


                Executors.newSingleThreadExecutor().execute {
                    val ultimo = DatabaseClient.getInstance(requireContext())
                        .appDatabase
                        .movimientosDao()
                        .obtenerUltimoPorFecha(fecha)

                    if (ultimo != null) {
                        ultimo.horaInicio = horaInicio
                        DatabaseClient.getInstance(requireContext())
                            .appDatabase
                            .movimientosDao()
                            .update(ultimo)
                    }
                }
            }
        }


        btnPause.setOnClickListener {
            if (isRunning) {
                elapsedTime += System.currentTimeMillis() - startTime
                handler.removeCallbacks(runnable)
                isRunning = false
            }
        }

        btnStop.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("¿Finalizar jornada?")
                .setMessage("¿Estás seguro que quieres terminar este día?")
                .setPositiveButton("Sí") { dialog, which ->

                    val horaFin = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                    finishmeTextView.text = "Hora Fin:\n$horaFin"
                    val fecha = etFecha.text.toString()

                    handler.removeCallbacks(runnable)
                    isRunning = false
                    startTime = 0L
                    elapsedTime = 0L
                    cronometroView.text = "00:00:00"

                    Executors.newSingleThreadExecutor().execute {
                        val ultimo = DatabaseClient.getInstance(requireContext())
                            .appDatabase
                            .movimientosDao()
                            .obtenerUltimoPorFecha(fecha)
                        if (ultimo != null){
                            val final= calcularDuracion(ultimo.horaInicio, horaFin)
                            diferenceTimeTextView.text = "Hora Fin:\n$final"

                            ultimo.horaFin = horaFin
                            ultimo.horaTotal = final

                            DatabaseClient.getInstance(requireContext())
                                .appDatabase
                                .movimientosDao()
                                .update(ultimo)
                        }
                    }

                    Toast.makeText(requireContext(), "Día finalizado", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("No") { dialog, which -> dialog.dismiss() }
                .show()
        }




        // Establecer la fecha inicial de la jornada
        if (fechaJornada.isNullOrEmpty()) {
            fechaJornada = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        }
        etFecha.setText(fechaJornada)

        fechaJornada?.let {
            mostrarHorasPorFecha(it)
            cargarTotales(it)
        }

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



            val valorCobradoStr = etValor.text.toString().trim()
            val propinaIngresadaStr = etCobrado.text.toString().trim()

            val valorCobrado = valorCobradoStr.toDoubleOrNull() ?: 0.0
            val propinaIngresada = propinaIngresadaStr.toDoubleOrNull() ?: 0.0
            val vlrPropina = propinaIngresada - valorCobrado



            // Crear y guardar el movimiento
            val movimiento = Movimiento().apply {
                this.fecha = fecha
                this.valor = valor
                this.propina = vlrPropina
                this.metodoDePago = metodoDePago
                this.hora = hora
            }

            Executors.newSingleThreadExecutor().execute {
                DatabaseClient.getInstance(requireContext())
                    .appDatabase
                    .movimientosDao()
                    .insert(movimiento)
                cargarTotales(fecha)

            }

            Toast.makeText(requireContext(), "Movimiento guardado", Toast.LENGTH_SHORT).show()
            etValor.text.clear()
            etCobrado.text.clear()
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
        cargarTotales(etFecha.text.toString())




        return root
    }

    override fun onResume() {
        super.onResume()
        val fecha = view?.findViewById<EditText>(R.id.etFecha)?.text.toString()
        if (fecha.isNotEmpty()) {
            mostrarHorasPorFecha(fecha)
        }
    }

    private fun mostrarDatePickerDialog(etFecha: EditText) {
        val calendario = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val fechaSeleccionada = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                etFecha.setText(fechaSeleccionada)
                cargarUltimoMovimiento(etFecha)
                cargarTotales(fechaSeleccionada)
                mostrarHorasPorFecha(fechaSeleccionada)
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
                    Propina: ${ultimo.propina}
                    Pago: ${ultimo.metodoDePago}
                    
                """.trimIndent()

                } else {
                    tvUltimoMovimiento.text = "No hay movimientos para el $fechaSeleccionada."
                }
            }
        }
    }

    private fun cargarTotales(fecha: String) {
        Executors.newSingleThreadExecutor().execute {
            val dao = DatabaseClient.getInstance(requireContext())
                .appDatabase
                .movimientosDao()

            val movimientos = dao.getMovimientosByFecha(fecha)
            val totalPropinaDia = dao.obtenerTotalPropinaPorFecha(fecha) ?: 0.0

            val agrupados = movimientos.groupBy { it.metodoDePago }

            val tarjeta = agrupados["Tarjeta"] ?: emptyList()
            val abonado = agrupados["Abonado"] ?: emptyList()
            val efectivo = agrupados["Efectivo"] ?: emptyList()
            val retorno = agrupados["Retorno"] ?: emptyList()

            val totalTarjeta = tarjeta.sumOf { it.valor }
            val totalAbonado = abonado.sumOf { it.valor }
            val totalEfectivo = efectivo.sumOf { it.valor }
            val totalRetorno = retorno.sumOf { it.valor }


            val totalGeneral = totalTarjeta + totalAbonado + totalEfectivo - totalRetorno



            activity?.runOnUiThread {

                totalGeneralInfo.text = "Total: ${"%.2f".format(totalGeneral ?: 0.0)} €"
                totalPropina.text = "Propina: ${"%.2f".format(totalPropinaDia ?: 0.0)} €"

            }
        }
    }

    private val runnable = object : Runnable {
        override fun run() {
            val currentTime = System.currentTimeMillis()
            val time = currentTime - startTime + elapsedTime
            val seconds = (time / 1000) % 60
            val minutes = (time / (1000 * 60)) % 60
            val hours = (time / (1000 * 60 * 60))

            cronometroView.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            handler.postDelayed(this, 1000)
        }
    }

    private fun calcularDuracion(horaInicio: String?, horaFin: String?): String {
        if (horaInicio.isNullOrEmpty() || horaFin.isNullOrEmpty()) return "00:00:00"
        return try {
            val formato = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val inicio = formato.parse(horaInicio)
            val fin = formato.parse(horaFin)
            val diferencia = fin.time - inicio.time

            val segundos = (diferencia / 1000) % 60
            val minutos = (diferencia / (1000 * 60)) % 60
            val horas = (diferencia / (1000 * 60 * 60))

            String.format("%02d:%02d:%02d", horas, minutos, segundos)
        } catch (e: Exception) {
            "00:00:00"
        }
    }

    private fun mostrarHorasPorFecha(fecha: String) {
        Executors.newSingleThreadExecutor().execute {
            val movimiento = DatabaseClient.getInstance(requireContext())
                .appDatabase
                .movimientosDao()
                .obtenerUltimoPorFecha(fecha)

            activity?.runOnUiThread {
                initTimeTextView.text = "Hora Inicio:\n${movimiento?.horaInicio ?: "00:00:00"}"
                finishmeTextView.text = "Hora Fin:\n${movimiento?.horaFin ?: "00:00:00"}"
                diferenceTimeTextView.text = "Duración:\n${movimiento?.horaTotal ?: "00:00:00"}"
            }
        }
    }


}