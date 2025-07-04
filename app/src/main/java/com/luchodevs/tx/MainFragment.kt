package com.luchodevs.tx

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
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
import com.luchodevs.tx.utils.AppConstants
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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
    private lateinit var promedioHoraView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_main, container, false)
        val prefs = requireContext().getSharedPreferences("preferencias_visibilidad", 0)


        Thread {
            try {
                val context = requireContext()
                val db = DatabaseClient.getInstance(context).appDatabase
                val movimientosDao = db.movimientosDao()

                val nombresPrefs = context.getSharedPreferences("nombres_editables", Context.MODE_PRIVATE)
                val todosMovimientos = movimientosDao.getAllMovimientos()

                todosMovimientos.forEach { movimiento ->

                    val tipoActual = movimiento.tipo ?: ""
                    val metodoDePagoActual = movimiento.metodoDePago ?: ""

                    val nombreEsperado = nombresPrefs.getString(
                        tipoActual,
                        AppConstants.NOMBRES_SERVICIOS_DEFAULT[tipoActual]
                    ) ?: AppConstants.NOMBRES_SERVICIOS_DEFAULT[tipoActual].orEmpty()

                    val metodoEsperado = nombresPrefs.getString(
                        metodoDePagoActual,
                        AppConstants.NOMBRES_METODOS_DEFAULT[metodoDePagoActual]
                    ) ?: AppConstants.NOMBRES_METODOS_DEFAULT[metodoDePagoActual].orEmpty()

                    val nuevoTipo = when (tipoActual?.lowercase() ?: "") {
                        "taxi" -> "Tipo1"
                        "radio taxi" -> "Tipo2"
                        "uber" -> "Tipo3"
                        "bolt" -> "Tipo4"
                        "cabify" -> "Tipo5"
                        else -> tipoActual ?: ""
                    }

                    val nuevoMetodo = when (metodoDePagoActual?.lowercase() ?: "") {
                        "tarjeta" -> "Metodo1"
                        "efectivo" -> "Metodo2"
                        "abonado" -> "Metodo3"
                        "retorno" -> "Metodo4"
                        else -> metodoDePagoActual ?: ""
                    }

                    movimiento.tipo = nuevoTipo
                    movimiento.metodoDePago = nuevoMetodo

                    var actualizar = false
                    if (movimiento.tipoNombre.isNullOrBlank() ||
                        movimiento.tipoNombre == "NombrePorDefecto" ||
                        movimiento.tipoNombre != nombreEsperado) {
                        movimiento.tipoNombre = nombreEsperado
                        actualizar = true
                    }

                    if (movimiento.metodoNombre.isNullOrBlank() ||
                        movimiento.metodoNombre != metodoEsperado) {
                        movimiento.metodoNombre = metodoEsperado
                        actualizar = true
                    }

                    if (actualizar) {
                        movimientosDao.update(movimiento)
                    }
                }

                movimientosDao.getAllMovimientos().forEach {
                    Log.d("GetTodos", it.toString())
                }

            } catch (e: Exception) {
                Log.e("ERROR_THREAD_MOV", "Error en la migración de movimientos", e)
            }
        }.start()



        // Referencias a los elementos de la vista
        val etFecha = root.findViewById<EditText>(R.id.etFecha)
        val etValor = root.findViewById<EditText>(R.id.etValor)
        val etCobrado = root.findViewById<EditText>(R.id.etCobrado)
        val radioGroupPago = root.findViewById<RadioGroup>(R.id.radioGroupPago)
        val radioGroupTipo = root.findViewById<RadioGroup>(R.id.radioGroupTipo)
        val agregarButton = root.findViewById<Button>(R.id.agregar_button)
        val btnNavigate = root.findViewById<Button>(R.id.btnNavigate)
        val btnNavigate2 = root.findViewById<Button>(R.id.btnNavigate2)


        hacerEditableYPersistente(requireContext(), root.findViewById(R.id.servicio_tipo1), "Tipo1")
        hacerEditableYPersistente(requireContext(), root.findViewById(R.id.servicio_tipo2), "Tipo2")
        hacerEditableYPersistente(requireContext(), root.findViewById(R.id.servicio_tipo3), "Tipo3")
        hacerEditableYPersistente(requireContext(), root.findViewById(R.id.servicio_tipo4), "Tipo4")
        hacerEditableYPersistente(requireContext(), root.findViewById(R.id.servicio_tipo5), "Tipo5")

        hacerEditableYPersistente(requireContext(), root.findViewById(R.id.pago_metodo1), "Metodo1")
        hacerEditableYPersistente(requireContext(), root.findViewById(R.id.pago_metodo2), "Metodo2")
        hacerEditableYPersistente(requireContext(), root.findViewById(R.id.pago_metodo3), "Metodo3")
        hacerEditableYPersistente(requireContext(), root.findViewById(R.id.pago_metodo4), "Metodo4")


        val servicios = AppConstants.SERVICIOS
        val metodosPago = AppConstants.METODOS_PAGO

        // Solo marcar Taxi (índice 0) y Radio Taxi (índice 1)
        val checksServicios = BooleanArray(servicios.size) { index -> index == 0 || index == 1 }

        // Solo marcar Tarjeta (índice 0) y Efectivo (índice 1)
        val checksPagos = BooleanArray(metodosPago.size) { index -> index == 0 || index == 1 }

        val serviciosMap = mapOf(
            0 to R.id.servicio_tipo1,
            1 to R.id.servicio_tipo2,
            2 to R.id.servicio_tipo3,
            3 to R.id.servicio_tipo4,
            4 to R.id.servicio_tipo5
        )

        val pagosMap = mapOf(
            0 to R.id.pago_metodo1,
            1 to R.id.pago_metodo2,
            2 to R.id.pago_metodo3,
            3 to R.id.pago_metodo4
        )

        for ((i, id) in serviciosMap) {
            val visible = prefs.getBoolean(
                "servicio_$i",
                i == 0 || i == 1
            ) // Por defecto visibles los dos primeros
            root.findViewById<RadioButton>(id).visibility = if (visible) View.VISIBLE else View.GONE
            checksServicios[i] = visible
        }

        for ((i, id) in pagosMap) {
            val visible = prefs.getBoolean(
                "pago_$i",
                i == 0 || i == 1
            ) // Por defecto visibles los dos primeros
            root.findViewById<RadioButton>(id).visibility = if (visible) View.VISIBLE else View.GONE
            checksPagos[i] = visible
        }
        totalGeneralInfo = root.findViewById(R.id.total_general_info)
        totalPropina = root.findViewById(R.id.total_propina)
        tvUltimoMovimiento = root.findViewById(R.id.ultimoAgregado)
        btnadmin = root.findViewById(R.id.btnadmin)
        btnGrafico = root.findViewById(R.id.btnGrafico)
        promedioHoraView = root.findViewById(R.id.promedio_general_info)

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
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val valor = valorStr.toDoubleOrNull()
            if (valor == null) {
                Toast.makeText(requireContext(), "Valor no válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedIdTipo = radioGroupTipo.checkedRadioButtonId
            if (selectedIdTipo == -1) {
                Toast.makeText(
                    requireContext(),
                    "Selecciona el tipo de servicio",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val selectedId = radioGroupPago.checkedRadioButtonId
            if (selectedId == -1) {
                Toast.makeText(requireContext(), "Selecciona un método de pago", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val selectedRadioTipo = root.findViewById<RadioButton>(selectedIdTipo)
            val tipoNombre = selectedRadioTipo.text.toString()

            // Aquí asumo que tienes una forma de mapear la posición o el ID del radio button a una clave fija:
            // Ejemplo: si el radio button tiene id R.id.servicio_tipo1 -> clave "Tipo1"
            val tipoClave = when (selectedIdTipo) {
                R.id.servicio_tipo1 -> "Tipo1"
                R.id.servicio_tipo2 -> "Tipo2"
                R.id.servicio_tipo3 -> "Tipo3"
                R.id.servicio_tipo4 -> "Tipo4"
                R.id.servicio_tipo5 -> "Tipo5"
                else -> "TipoX"  // por defecto o error
            }

            val selectedRadio = root.findViewById<RadioButton>(selectedId)
            val metodoNombre = selectedRadio.text.toString()

            // Igual con método de pago, mapea id a clave fija:
            val metodoClave = when (selectedId) {
                R.id.pago_metodo1 -> "Metodo1"
                R.id.pago_metodo2 -> "Metodo2"
                R.id.pago_metodo3 -> "Metodo3"
                R.id.pago_metodo4 -> "Metodo4"
                else -> "MetodoX"
            }

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

                        val btnChronometer =
                            requireView().findViewById<Button>(R.id.btnOpenChronometer)
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
                    this.tipo = tipoClave         // guardo clave para cálculos
                    this.tipoNombre = tipoNombre  // guardo nombre para mostrar
                    this.propina = vlrPropina
                    this.metodoDePago = metodoClave
                    this.metodoNombre = metodoNombre
                    this.hora = hora
                }

                dao.insert(movimiento)
                Log.d("BD_MOVIMIENTO", movimiento.toString())
                val btnChronometer = requireView().findViewById<Button>(R.id.btnOpenChronometer)
                btnChronometer.setBackgroundColor(Color.parseColor("#DDDDDD"))

                requireActivity().runOnUiThread {
                    cargarTotales(fecha)
                    cargarUltimoMovimiento(etFecha.text.toString())
                    Toast.makeText(requireContext(), "Movimiento guardado", Toast.LENGTH_SHORT)
                        .show()
                    etValor.text.clear()
                    etCobrado.text.clear()
                    radioGroupPago.clearCheck()
                    radioGroupTipo.clearCheck()

                    // Seleccionar Taxi y Tarjeta por defecto
                    val radioTaxi = view?.findViewById<RadioButton>(R.id.servicio_tipo1)
                    val radioTarjeta = view?.findViewById<RadioButton>(R.id.pago_metodo1)
                    radioTaxi?.isChecked = true
                    radioTarjeta?.isChecked = true
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
            val ctx = requireContext()

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
                    val nombre = getNombreServicio(ctx, servicio)
                    Log.d("AdminDialog", "Servicio key: $servicio, nombre leído: $nombre")
                    val cb = CheckBox(ctx).apply {
                        text = getNombreServicio(ctx, servicio)
                        isChecked = checksServicios[i]
                        setOnCheckedChangeListener { _, isChecked ->
                            checksServicios[i] = isChecked
                        }
                    }
                    addView(cb)
                }

                addView(TextView(ctx).apply {
                    text = "Métodos de pago"
                    textSize = 18f
                    setPadding(0, 24, 0, 6)
                })

                metodosPago.forEachIndexed { i, metodo ->
                    val cb = CheckBox(ctx).apply {
                        text = getNombreMetodoPago(ctx, metodo)
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
                    val prefs = requireContext().getSharedPreferences("preferencias_visibilidad", 0)
                    val editor = prefs.edit()

                    for ((i, id) in serviciosMap) {
                        val visible = checksServicios[i]
                        view?.findViewById<RadioButton>(id)?.visibility =
                            if (visible) View.VISIBLE else View.GONE
                        editor.putBoolean("servicio_$i", visible) // Guardar preferencia
                    }

                    view?.findViewById<RadioGroup>(R.id.radioGroupTipo)?.visibility =
                        if (checksServicios.any { it }) View.VISIBLE else View.GONE

                    for ((i, id) in pagosMap) {
                        val visible = checksPagos[i]
                        view?.findViewById<RadioButton>(id)?.visibility =
                            if (visible) View.VISIBLE else View.GONE
                        editor.putBoolean("pago_$i", visible) // Guardar preferencia
                    }

                    view?.findViewById<RadioGroup>(R.id.radioGroupPago)?.visibility =
                        if (checksPagos.any { it }) View.VISIBLE else View.GONE

                    editor.apply() // 👉 guardar los cambios
                }

                .setNegativeButton("Cancelar", null)
                .show()
        }


        cargarUltimoMovimiento(etFecha.text.toString())
        cargarTotales(etFecha.text.toString())
        return root
    }

    fun getNombreServicio(context: Context, key: String): String {
        val prefs = context.getSharedPreferences("nombres_editables", Context.MODE_PRIVATE)
        return prefs.getString(key, AppConstants.NOMBRES_SERVICIOS_DEFAULT[key])
            ?: AppConstants.NOMBRES_SERVICIOS_DEFAULT[key].orEmpty()
    }

    fun getNombreMetodoPago(context: Context, key: String): String {
        val prefs = context.getSharedPreferences("nombres_editables", Context.MODE_PRIVATE)
        return prefs.getString(key, AppConstants.NOMBRES_METODOS_DEFAULT[key])
            ?: AppConstants.NOMBRES_METODOS_DEFAULT[key].orEmpty()
    }

    private fun hacerEditableYPersistente(context: Context, radioButton: RadioButton, key: String) {
        val prefs = context.getSharedPreferences("nombres_editables", Context.MODE_PRIVATE)

        // Recuperar el valor guardado (si existe)
        val textoGuardado = prefs.getString(key, null)
        if (textoGuardado != null) {
            radioButton.text = textoGuardado
        }

        // Activar edición solo si se mantiene presionado
        radioButton.setOnLongClickListener {
            val parent = radioButton.parent as ViewGroup
            val index = parent.indexOfChild(radioButton)

            // Crear LinearLayout horizontal para EditText + Botón
            val container = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = radioButton.layoutParams
            }

            // Crear EditText
            val editText = EditText(context).apply {
                setText(radioButton.text)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }

            // Crear botón de guardar
            val btnGuardar = Button(context).apply {
                text = "✔"
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            // Añadir EditText y botón al contenedor
            container.addView(editText)
            container.addView(btnGuardar)

            // Reemplazar el RadioButton por el contenedor
            parent.removeViewAt(index)
            parent.addView(container, index)

            // Acción al pulsar el botón "Guardar"
            btnGuardar.setOnClickListener {
                val nuevoTexto = editText.text.toString()
                radioButton.text = nuevoTexto
                prefs.edit().putString(key, nuevoTexto).apply()
                Log.d("Prefs", "Guardando $key -> $nuevoTexto")

                parent.removeViewAt(index)
                parent.addView(radioButton, index)
            }

            true // indicar que se ha manejado el long click
        }
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
        val prefs = requireContext().getSharedPreferences("nombres_editables", Context.MODE_PRIVATE)


        Executors.newSingleThreadExecutor().execute {
            val dao = DatabaseClient.getInstance(requireContext()).appDatabase.movimientosDao()

            val ultimo = dao.obtenerUltimoMovimientoFechaCompleta(fechaSeleccionada)
            val inicioJornada = dao.obtenerMovimientoConInicio(fechaSeleccionada)

            activity?.runOnUiThread {
                if (ultimo != null) {
                    var horaMostrada = ultimo.hora
                    if (ultimo.fechaHoraCompleta != null && inicioJornada.fechaHoraCompleta != null) {
                        if (ultimo.fechaHoraCompleta < inicioJornada.fechaHoraCompleta) {
                            horaMostrada = "+1 ${ultimo.hora}"
                        }
                    }

                    // Obtener los valores literales guardados en SharedPreferences
                    val pagoLiteral = prefs.getString(ultimo.metodoNombre, ultimo.metodoNombre)
                        ?: ultimo.metodoNombre
                    val tipoLiteral =
                        prefs.getString(ultimo.tipoNombre, ultimo.tipoNombre) ?: ultimo.tipoNombre

                    tvUltimoMovimiento.text = """
                    Fecha: ${ultimo.fechaHoraCompleta}
                    Valor: ${ultimo.valor}
                    Propina: ${"%.2f".format(ultimo.propina)}
                    tipo: $tipoLiteral
                    Pago: $pagoLiteral
                """.trimIndent()

                } else {
                    tvUltimoMovimiento.text = "No hay movimientos para el $fechaSeleccionada."
                }
            }
        }
    }

    private fun calcularHorasTrabajadasDesdePrimerServicio(inicio: String?): Double {
        return try {
            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
            val horaInicio = LocalTime.parse(inicio, formatter)
            val horaActual = LocalTime.now()

            val duration = Duration.between(horaInicio, horaActual)
            duration.toMinutes().toDouble() / 60
        } catch (e: Exception) {
            0.0
        }
    }


    private fun cargarTotales(fecha: String) {
        Executors.newSingleThreadExecutor().execute {
            val dao = DatabaseClient.getInstance(requireContext())
                .appDatabase
                .movimientosDao()

            val movimientos = dao.getMovimientosByFecha(fecha)
            movimientos.forEach {
                Log.d(
                    "bbdd",
                    "Movimiento metodoDePago: ${it.metodoDePago}, valor: ${it.valor}, NombreTipo: ${it.tipoNombre}"
                )
            }


            val totalPropinaDia = dao.obtenerTotalPropinaPorFecha(fecha) ?: 0.0

            val agrupados = movimientos.groupBy { it.metodoDePago }
            val tarjeta = agrupados["Metodo1"] ?: emptyList()
            val abonado = agrupados["Metodo3"] ?: emptyList()
            val efectivo = agrupados["Metodo2"] ?: emptyList()
            val pago_metodo4 = agrupados["Metodo4"] ?: emptyList()

            val totalTarjeta = tarjeta.sumOf { it.valor }
            val totalAbonado = abonado.sumOf { it.valor }
            val totalEfectivo = efectivo.sumOf { it.valor }
            val totalpago_metodo4 = pago_metodo4.sumOf { it.valor }


            val totalGeneral = totalTarjeta + totalAbonado + totalEfectivo - totalpago_metodo4

            // Cálculo de horas trabajadas
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val primerMovimiento = movimientos.firstOrNull { !it.fechaHoraCompleta.isNullOrEmpty() }

            val horasTrabajadas = primerMovimiento?.fechaHoraCompleta?.let { inicio ->
                try {
                    val horaInicio = LocalDateTime.parse(inicio, formatter)
                    val ahora = LocalDateTime.now(ZoneId.of("Europe/Madrid"))
                    val minutosTrabajados = Duration.between(horaInicio, ahora).toMinutes()
                    minutosTrabajados / 60.0
                } catch (e: Exception) {
                    Log.e("HorasTrabajadas", "Error al parsear fechaHora: $inicio", e)
                    0.0
                }
            } ?: 0.0

            val promedio = if (horasTrabajadas > 1.0) totalGeneral / horasTrabajadas else 0.0



            activity?.runOnUiThread {

                totalGeneralInfo.text = "Total: ${"%.2f".format(totalGeneral ?: 0.0)} €"
                totalPropina.text = "Propina: ${"%.2f".format(totalPropinaDia ?: 0.0)} €"
                promedioHoraView.text = "Promedio: ${"%.2f".format(promedio)} €/h"
            }
        }
    }
}

