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
    private lateinit var totalGeneralInfo: TextView
    private lateinit var totalPropina: TextView
    private lateinit var initTimeTextView: TextView
    private lateinit var finishmeTextView: TextView
    private lateinit var diferenceTimeTextView: TextView
    private lateinit var cronometroView: TextView
    private lateinit var btnStart: Button
    private lateinit var tvUltimoMovimiento: TextView
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
        val radioGroupTipo = root.findViewById<RadioGroup>(R.id.radioGroupTipo)
        val agregarButton = root.findViewById<Button>(R.id.agregar_button)
        val btnNavigate = root.findViewById<Button>(R.id.btnNavigate)
        val btnNavigate2 = root.findViewById<Button>(R.id.btnNavigate2)


        initTimeTextView = root.findViewById(R.id.initTime)
        finishmeTextView = root.findViewById(R.id.finishTime)
        diferenceTimeTextView = root.findViewById(R.id.totaltime)

        totalGeneralInfo = root.findViewById(R.id.total_general_info)
        totalPropina = root.findViewById(R.id.total_propina)



        cronometroView = root.findViewById(R.id.cronometro)
        btnStart = root.findViewById(R.id.btnStart)
        tvUltimoMovimiento = root.findViewById(R.id.ultimoAgregado)
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
                    val movimientosDao = DatabaseClient.getInstance(requireContext())
                        .appDatabase
                        .movimientosDao()

                    // Verificar si ya existe un registro para esta fecha
                    val yaExiste = movimientosDao.existeMovimientoEnFecha(fecha)

                    if (!yaExiste) {
                        // Insertar nuevo registro de jornada con horaInicio
                        val movimientoInicio = Movimiento().apply {
                            this.fecha = fecha
                            this.horaInicio = horaInicio
                            this.valor = 0.0
                            this.propina = 0.0
                            this.metodoDePago = "Inicio Jornada"
                            this.hora = horaInicio
                        }

                        movimientosDao.insert(movimientoInicio)
                    }
                }
            }
        }
        btnStop.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("¿Finalizar jornada?")
                .setMessage("¿Estás seguro que quieres terminar este día?")
                .setPositiveButton("Sí") { dialog, _ ->

                    val fecha = etFecha.text.toString().trim()
                    val horaFin = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

                    Executors.newSingleThreadExecutor().execute {
                        val dao = DatabaseClient.getInstance(requireContext()).appDatabase.movimientosDao()

                        val inicioExiste = dao.existeMovimientoEnFecha(fecha)
                        val finalYaExiste = dao.existeFinalEnFecha(fecha)

                        activity?.runOnUiThread {
                            if (!inicioExiste) {
                                Toast.makeText(requireContext(), "No puedes finalizar una jornada que no ha sido iniciada.", Toast.LENGTH_SHORT).show()
                                return@runOnUiThread
                            }
                            if (finalYaExiste) {
                                Toast.makeText(requireContext(), "Esta jornada ya ha sido finalizada.", Toast.LENGTH_SHORT).show()
                                return@runOnUiThread
                            }

                            // Detener cronómetro
                            handler.removeCallbacks(runnable)
                            isRunning = false
                            startTime = 0L
                            elapsedTime = 0L
                            cronometroView.text = "00:00:00"
                            finishmeTextView.text = "Hora Fin:\n$horaFin"

                            // Obtener primera hora (inicio)
                            Executors.newSingleThreadExecutor().execute {
                                val primero = dao.obtenerPrimeroPorFecha(fecha)
                                val horaInicio = primero?.hora ?: "00:00:00"
                                val duracion = calcularDuracion(horaInicio, horaFin)
                                val existente = dao.obtenerFinalDeJornada(fecha)

                                if (existente != null) {
                                    existente.hora=horaFin
                                    existente.horaFin = horaFin
                                    existente.horaTotal = duracion
                                    dao.update(existente)
                                    Log.d("BBDD", "Actualizado Final de Jornada: ${existente}")
                                }else{
                                    val finalMovimiento = Movimiento().apply {
                                        this.fecha = fecha
                                        this.valor = 0.0
                                        this.tipo = "Final de Jornada"
                                        this.propina = 0.0
                                        this.metodoDePago = "N/A"
                                        this.horaFin = horaFin
                                        this.horaInicio = horaInicio
                                        this.horaTotal = duracion
                                        this.hora = horaFin // Guarda también en el campo principal
                                    }
                                    dao.insert(finalMovimiento)

                                }
                                activity?.runOnUiThread {
                                    diferenceTimeTextView.text = "Duración:\n$duracion"
                                    cargarUltimoMovimiento(fecha)
                                    Toast.makeText(requireContext(), "Jornada finalizada correctamente.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
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
            cargarUltimoMovimiento(it)
        }

        // Mostrar DatePickerDialog al hacer clic en el campo
            etFecha.setOnClickListener {
            mostrarDatePickerDialog(etFecha)
            cargarUltimoMovimiento(etFecha.text.toString())
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

            val selectedIdTipo = radioGroupTipo.checkedRadioButtonId
            if (selectedIdTipo == -1) {
                Toast.makeText(requireContext(), "Selecciona el tipo de servicio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedRadioTipo = root.findViewById<RadioButton>(selectedIdTipo)
            val tipoServicio = selectedRadioTipo.text.toString()

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
            val vlrPropina = if (propinaIngresadaStr.isNotEmpty()) {
                propinaIngresada - valorCobrado
            } else {
                0.0
            }

            // Validación robusta: verificar si existe un inicio de jornada para la fecha
            Executors.newSingleThreadExecutor().execute {
                val dao = DatabaseClient.getInstance(requireContext()).appDatabase.movimientosDao()
                val inicioExiste = dao.existeMovimientoEnFecha(fecha) // asegúrate que este método esté implementado

                activity?.runOnUiThread {
                    if (!inicioExiste) {
                        Toast.makeText(requireContext(), "Debes iniciar la jornada con el cronómetro antes de agregar movimientos.", Toast.LENGTH_SHORT).show()
                        return@runOnUiThread
                    }

                    // Si pasó la validación, se crea el movimiento
                    val movimiento = Movimiento().apply {
                        this.fecha = fecha
                        this.valor = valor
                        this.tipo = tipoServicio
                        this.propina = vlrPropina
                        this.metodoDePago = metodoDePago
                        this.hora = hora
                    }

                    Executors.newSingleThreadExecutor().execute {
                        dao.insert(movimiento)
                        cargarTotales(fecha)
                        cargarUltimoMovimiento(fecha);
                    }

                    Toast.makeText(requireContext(), "Movimiento guardado", Toast.LENGTH_SHORT).show()
                    etValor.text.clear()
                    etCobrado.text.clear()
                    radioGroupPago.clearCheck()
                    radioGroupTipo.clearCheck()
                }
            }
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
        //cargarUltimoMovimiento(etFecha)
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
                //cargarUltimoMovimiento(etFecha)
                cargarTotales(fechaSeleccionada)
                mostrarHorasPorFecha(fechaSeleccionada)
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        )

        datePicker.show()

    }

     //Función para mostrar el último movimiento
    fun cargarUltimoMovimiento(etFecha: String) {
        val fechaSeleccionada = etFecha
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
                    tipo: ${ultimo.tipo}

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
            var fin = formato.parse(horaFin)

            if (fin.before(inicio)) {
                // Fin es al día siguiente
                val calendar = Calendar.getInstance()
                calendar.time = fin
                calendar.add(Calendar.DATE, 1)
                fin = calendar.time
            }

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
            val db = DatabaseClient.getInstance(requireContext()).appDatabase.movimientosDao()

            val primerMovimiento = db.obtenerPrimeroPorFecha(fecha)
            val ultimoMovimiento = db.obtenerUltimoPorFecha(fecha)
            Log.d("BBDD", "Movimientos para primer $fecha: $primerMovimiento")
            Log.d("BBDD", "Movimientos para ultimo $fecha: $ultimoMovimiento")
            activity?.runOnUiThread {
                val horaInicio = primerMovimiento?.horaInicio
                val horaFin = ultimoMovimiento?.horaFin
                val horaTotal = ultimoMovimiento?.horaTotal

                initTimeTextView.text = "Hora Inicio:\n${horaInicio ?: "00:00:00"}"
                finishmeTextView.text = "Hora Fin:\n${horaFin ?: "00:00:00"}"
                diferenceTimeTextView.text = "Duración:\n${horaTotal ?: "00:00:00"}"

                if (!horaInicio.isNullOrEmpty() &&
                    (horaFin.isNullOrEmpty() || horaFin == "00:00:00") &&
                    (horaTotal.isNullOrEmpty() || horaTotal == "00:00:00")
                ) {
                    try {
                        val formato = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                        val horaInicioDate = formato.parse(horaInicio)

                        val calendarInicio = Calendar.getInstance()
                        calendarInicio.time = Date() // Fecha actual

                        val temp = Calendar.getInstance()
                        temp.time = horaInicioDate!!

                        calendarInicio.set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY))
                        calendarInicio.set(Calendar.MINUTE, temp.get(Calendar.MINUTE))
                        calendarInicio.set(Calendar.SECOND, temp.get(Calendar.SECOND))
                        calendarInicio.set(Calendar.MILLISECOND, 0)

                        val ahora = Calendar.getInstance()

                        if (calendarInicio.after(ahora)) {
                            calendarInicio.add(Calendar.DAY_OF_YEAR, -1)
                        }

                        val diferencia = ahora.timeInMillis - calendarInicio.timeInMillis

                        startTime = System.currentTimeMillis() - diferencia
                        elapsedTime = 0L
                        handler.post(runnable)
                        isRunning = true
                    } catch (e: Exception) {
                        Log.e("MainFragment", "Error al calcular cronómetro desde horaInicio", e)
                    }
                }
            }
        }
    }





}