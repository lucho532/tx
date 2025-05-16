package com.luchodevs.tx

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.luchodevs.tx.database.DatabaseClient
import com.luchodevs.tx.entity.Movimiento
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors


class MainFragment : Fragment() {
    private var fechaJornada: String? = null
    private lateinit var totalGeneralInfo: TextView
    private lateinit var totalPropina: TextView
    private lateinit var btnOpenChronometer: Button
    private lateinit var btnadmin: Button
    private lateinit var btnGrafico: Button
    private lateinit var tvUltimoMovimiento: TextView

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


        val servicios = listOf("Taxi", "Radio Taxi", "Uber", "Bolt", "Cabify")
        val metodosPago = listOf("Tarjeta", "Efectivo", "Abonado", "Retorno")

        // Solo marcar Taxi (índice 0) y Radio Taxi (índice 1)
        val checksServicios = BooleanArray(servicios.size) { index -> index == 0 || index == 1 }

// Solo marcar Tarjeta (índice 0) y Efectivo (índice 1)
        val checksPagos = BooleanArray(metodosPago.size) { index -> index == 0 || index == 1 }

        val serviciosMap = mapOf(
            0 to R.id.servicio_taxi,
            1 to R.id.servicio_radio_taxi,
            2 to R.id.servicio_uber,
            3 to R.id.servicio_bolt,
            4 to R.id.servicio_cabify
        )

        val pagosMap = mapOf(
            0 to R.id.pago_tarjeta_radio,
            1 to R.id.pago_efectivo_radio,
            2 to R.id.pago_abonado_radio,
            3 to R.id.retorno
        )


        totalGeneralInfo = root.findViewById(R.id.total_general_info)
        totalPropina = root.findViewById(R.id.total_propina)
        tvUltimoMovimiento = root.findViewById(R.id.ultimoAgregado)
        btnadmin = root.findViewById(R.id.btnadmin)
        btnGrafico = root.findViewById(R.id.btnGrafico)

        btnOpenChronometer = root.findViewById(R.id.btnOpenChronometer);
        btnOpenChronometer.setOnClickListener {
            val fragment = FloatingChronometerFragment()
            val bundle = Bundle()
            bundle.putString("fechaSeleccionada", etFecha.text.toString())
            fragment.arguments = bundle
            fragment.show(parentFragmentManager, "ChronometerDialog")
        }

        btnGrafico.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("fechaSeleccionada", etFecha.text.toString())

            val dialog = productividad() // ← tu DialogFragment
            dialog.arguments = bundle
            dialog.show(parentFragmentManager, "ProductividadDialog")
        }


        // Establecer la fecha inicial de la jornada
        if (fechaJornada.isNullOrEmpty()) {
            fechaJornada = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        }
        etFecha.setText(fechaJornada)

        fechaJornada?.let {
            cargarTotales(etFecha.text.toString())
            cargarUltimoMovimiento(etFecha.text.toString())
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

            val selectedId = radioGroupPago.checkedRadioButtonId
            if (selectedId == -1) {
                Toast.makeText(requireContext(), "Selecciona un método de pago", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedRadioTipo = root.findViewById<RadioButton>(selectedIdTipo)
            val tipoServicio = selectedRadioTipo.text.toString()
            val selectedRadio = root.findViewById<RadioButton>(selectedId)
            val metodoDePago = selectedRadio.text.toString()
            val hora = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

            val valorCobradoStr = etValor.text.toString().trim()
            val propinaIngresadaStr = etCobrado.text.toString().trim()

            val valorCobrado = valorCobradoStr.toDoubleOrNull() ?: 0.0
            val propinaIngresada = propinaIngresadaStr.toDoubleOrNull() ?: 0.0
            val vlrPropina = if (propinaIngresadaStr.isNotEmpty()) {
                propinaIngresada - valorCobrado
            } else {
                0.0
            }

            Executors.newSingleThreadExecutor().execute {
                val dao = DatabaseClient.getInstance(requireContext()).appDatabase.movimientosDao()

                // 👉 Validar si existe jornada
                val jornada: Movimiento? = dao.obtenerPrimeroPorFecha(fecha, "Inicio Jornada")
                if (jornada == null) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(
                            requireContext(),
                            "Debes iniciar la jornada antes de registrar servicios",
                            Toast.LENGTH_LONG
                        ).show()

                        val btnChronometer = requireView().findViewById<Button>(R.id.btnOpenChronometer)
                        btnChronometer.setBackgroundColor(Color.RED)
                    }
                    return@execute
                }
                val isoFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val fechaHoraInicio = isoFormat.format(Date())
                // ✅ Si hay jornada, insertar
                val movimiento = Movimiento().apply {
                    this.fechaHoraCompleta = fechaHoraInicio
                    this.fecha = fecha
                    this.valor = valor
                    this.tipo = tipoServicio
                    this.propina = vlrPropina
                    this.metodoDePago = metodoDePago
                    this.hora = hora
                }

                dao.insert(movimiento)
                Log.d("BD_MOVIMIENTO", movimiento.toString())
                val btnChronometer = requireView().findViewById<Button>(R.id.btnOpenChronometer)
                btnChronometer.setBackgroundColor(Color.parseColor("#DDDDDD"))

                requireActivity().runOnUiThread {
                    cargarTotales(fecha)
                    cargarUltimoMovimiento(etFecha.text.toString())
                    Toast.makeText(requireContext(), "Movimiento guardado", Toast.LENGTH_SHORT).show()
                    etValor.text.clear()
                    etCobrado.text.clear()
                    radioGroupPago.clearCheck()
                    radioGroupTipo.clearCheck()
                }
            }
        }


        btnNavigate.setOnClickListener {
            val fecha = etFecha.text.toString().trim()
            if (fecha.isEmpty()) {
                Toast.makeText(requireContext(), "Selecciona una fecha", Toast.LENGTH_SHORT)
                    .show()
            } else {
                val bundle = Bundle().apply {
                    putString("fecha", fecha)
                }
                findNavController().navigate(R.id.action_mainFragment_to_firstFragment, bundle)
            }
        }
        btnNavigate2.setOnClickListener {
            val fecha = etFecha.text.toString().trim()
            if (fecha.isEmpty()) {
                Toast.makeText(requireContext(), "Selecciona una fecha", Toast.LENGTH_SHORT)
                    .show()
            } else {
                val bundle = Bundle().apply {
                    putString("fecha", fecha)
                }
                findNavController().navigate(
                    R.id.action_mainFragment_to_secondFragment2,
                    bundle
                )
            }
        }
        btnadmin.setOnClickListener {
            val layout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 16, 16, 16)

                // Servicios
                addView(TextView(context).apply {
                    text = "Servicios"
                    textSize = 18f
                    setPadding(0, 12, 0, 6)
                })
                servicios.forEachIndexed { i, servicio ->
                    val cb = CheckBox(context).apply {
                        text = servicio
                        isChecked = checksServicios[i]
                        setOnCheckedChangeListener { _, isChecked ->
                            checksServicios[i] = isChecked
                        }
                    }
                    addView(cb)
                }

                // Métodos de pago
                addView(TextView(context).apply {
                    text = "Métodos de pago"
                    textSize = 18f
                    setPadding(0, 24, 0, 6)
                })
                metodosPago.forEachIndexed { i, metodo ->
                    val cb = CheckBox(context).apply {
                        text = metodo
                        isChecked = checksPagos[i]
                        setOnCheckedChangeListener { _, isChecked ->
                            checksPagos[i] = isChecked
                        }
                    }
                    addView(cb)
                }
            }

            AlertDialog.Builder(requireContext()) // ✅ CORREGIDO
                .setTitle("Configurar visibilidad")
                .setView(layout)
                .setPositiveButton("Aceptar") { _, _ ->
                    for ((i, id) in serviciosMap) {
                        view?.findViewById<RadioButton>(id)?.visibility =
                            if (checksServicios[i]) View.VISIBLE else View.GONE
                    }

                    view?.findViewById<RadioGroup>(R.id.radioGroupTipo)?.visibility =
                        if (checksServicios.any { it }) View.VISIBLE else View.GONE

                    for ((i, id) in pagosMap) {
                        view?.findViewById<RadioButton>(id)?.visibility =
                            if (checksPagos[i]) View.VISIBLE else View.GONE
                    }

                    view?.findViewById<RadioGroup>(R.id.radioGroupPago)?.visibility =
                        if (checksPagos.any { it }) View.VISIBLE else View.GONE
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }


        cargarUltimoMovimiento(etFecha.text.toString())
        cargarTotales(etFecha.text.toString())
        return root
    }


    private fun mostrarDatePickerDialog(etFecha: EditText) {
        val calendario = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val fechaSeleccionada =
                    String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                etFecha.setText(fechaSeleccionada)
                cargarUltimoMovimiento(etFecha.text.toString())
                cargarTotales(fechaSeleccionada)
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
            val dao = DatabaseClient.getInstance(requireContext()).appDatabase.movimientosDao()

            val ultimo = dao.obtenerUltimoMovimientoFechaCompleta(fechaSeleccionada)
            val inicioJornada = dao.obtenerMovimientoConInicio(fechaSeleccionada)

            activity?.runOnUiThread {
                if (ultimo != null) {
                    var horaMostrada = ultimo.hora
                    if (ultimo.fechaHoraCompleta != null && inicioJornada.fechaHoraCompleta != null){
                        if (ultimo.fechaHoraCompleta < inicioJornada.fechaHoraCompleta){
                            horaMostrada = "+1 ${ultimo.hora}"
                        }
                    }

                    tvUltimoMovimiento.text = """
                    Último movimiento:
                    Fecha: ${ultimo.fecha}
                    Hora: $horaMostrada
                    Valor: ${ultimo.valor}
                    Propina: ${"%.2f".format(ultimo.propina)}
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
}

