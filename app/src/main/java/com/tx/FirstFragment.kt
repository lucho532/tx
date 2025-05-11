package com.tx

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.tx.database.DatabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors

class FirstFragment : Fragment() {

    private lateinit var tarjetaInfoTaxi: TextView
    private lateinit var efectivoInfoTaxi: TextView
    private lateinit var totalGeneralInfoTaxi: TextView


    private lateinit var tarjetaInfo: TextView
    private lateinit var efectivoInfo: TextView
    private lateinit var totalGeneralInfoRadioTaxi: TextView
    private lateinit var abonadoInfo: TextView
    private lateinit var retornoInfo: TextView


    private lateinit var tarjetaInfoUber: TextView
    private lateinit var efectivoInfoUber: TextView
    private lateinit var totalGeneralInfoUber: TextView


    private lateinit var tarjetaInfoBolt: TextView
    private lateinit var efectivoInfoBolt: TextView
    private lateinit var totalGeneralInfoBolt: TextView


    private lateinit var tarjetaInfoCabify: TextView
    private lateinit var efectivoInfoCabify: TextView
    private lateinit var totalGeneralInfoCabify: TextView

    private lateinit var totalDiaInfo: TextView
    private lateinit var totalSemanalInfo: TextView
    private lateinit var totalMensualInfo: TextView
    private lateinit var totalPropinas: TextView


    private var fecha: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_first, container, false)

        // Referencias UI
        tarjetaInfoTaxi = root.findViewById(R.id.taxi_tarjeta_info_diario)
        efectivoInfoTaxi = root.findViewById(R.id.taxi_efectivo_info_diario)
        totalGeneralInfoTaxi = root.findViewById(R.id.taxi_total_general_info_diario)

        tarjetaInfo = root.findViewById(R.id.radiotaxi_tarjeta_info_diario)
        abonadoInfo = root.findViewById(R.id.radiotaxi_abonado_info_diario)
        efectivoInfo = root.findViewById(R.id.radiotaxi_efectivo_info_diario)
        retornoInfo = root.findViewById(R.id.radiotaxi_retorno_info_diario)
        totalGeneralInfoRadioTaxi = root.findViewById(R.id.radiotaxi_total_info_diario)

        tarjetaInfoUber = root.findViewById(R.id.uber_tarjeta_info_diario)
        efectivoInfoUber = root.findViewById(R.id.uber_efectivo_info_diario)
        totalGeneralInfoUber = root.findViewById(R.id.uber_total_info_diario)

        tarjetaInfoBolt = root.findViewById(R.id.bolt_tarjeta_info_diario)
        efectivoInfoBolt = root.findViewById(R.id.bolt_efectivo_info_diario)
        totalGeneralInfoBolt = root.findViewById(R.id.bolt_total_info_diario)

        tarjetaInfoCabify = root.findViewById(R.id.cabify_tarjeta_info_diario)
        efectivoInfoCabify = root.findViewById(R.id.cabify_efectivo_info_diario)
        totalGeneralInfoCabify = root.findViewById(R.id.cabify_total_info_diario)

        totalDiaInfo = root.findViewById(R.id.total_dia_info)
        totalSemanalInfo = root.findViewById(R.id.total_semanal_info)
        totalMensualInfo = root.findViewById(R.id.total_mensual_info)
        totalPropinas = root.findViewById(R.id.total_propinas)

        // Botón volver
        val back = root.findViewById<Button>(R.id.btn_volver)
        back.setOnClickListener {
            findNavController().popBackStack()
        }

        // Obtener la fecha desde argumentos
        fecha = arguments?.getString("fecha")

        if (fecha != null) {
            cargarTotales(fecha!!)
            cargarTotalesSemanalesYMensuales()
        }

        // Expansión/colapso de la sección Taxi
        val btnToggleTaxi = root.findViewById<Button>(R.id.btnExpandCollapseTaxi)
        val taxiInfoContainer = root.findViewById<View>(R.id.taxiInfoContainer)

        btnToggleTaxi.setOnClickListener {
            if (taxiInfoContainer.visibility == View.VISIBLE) {
                taxiInfoContainer.visibility = View.GONE
                btnToggleTaxi.text = "Taxi +"
            } else {
                taxiInfoContainer.visibility = View.VISIBLE
                btnToggleTaxi.text = "Taxi -"
            }
        }

        // Expansión/colapso de la sección RadioTaxi
        val btnToggleRadioTaxi = root.findViewById<Button>(R.id.btnExpandCollapseRadioTaxi)
        val radioTaxiInfoContainer = root.findViewById<View>(R.id.RadiotaxiInfoContainer)

        btnToggleRadioTaxi.setOnClickListener {
            if (radioTaxiInfoContainer.visibility == View.VISIBLE) {
                radioTaxiInfoContainer.visibility = View.GONE
                btnToggleRadioTaxi.text = "Radio Taxi +"
            } else {
                radioTaxiInfoContainer.visibility = View.VISIBLE
                btnToggleRadioTaxi.text = "Radio Taxi -"
            }
        }

        // Expansión/colapso de la sección Uber
        val btnToggleUber = root.findViewById<Button>(R.id.btnExpandCollapseuber)
        val uberInfoContainer = root.findViewById<View>(R.id.uberiInfoContainer)

        btnToggleUber.setOnClickListener {
            if (uberInfoContainer.visibility == View.VISIBLE) {
                uberInfoContainer.visibility = View.GONE
                btnToggleUber.text = "Uber +"
            } else {
                uberInfoContainer.visibility = View.VISIBLE
                btnToggleUber.text = "Uber -"
            }
        }

        // Expansión/colapso de la sección Uber
        val btnToggleBolt = root.findViewById<Button>(R.id.btnExpandCollapseRadiobolt)
        val boltInfoContainer = root.findViewById<View>(R.id.boltiInfoContainer)

        btnToggleBolt.setOnClickListener {
            if (boltInfoContainer.visibility == View.VISIBLE) {
                boltInfoContainer.visibility = View.GONE
                btnToggleBolt.text = "Bolt +"
            } else {
                boltInfoContainer.visibility = View.VISIBLE
                btnToggleBolt.text = "Bolt -"
            }
        }

        // Expansión/colapso de la sección Uber
        val btnToggleCabify = root.findViewById<Button>(R.id.btnExpandCollapsecabify)
        val cabifyInfoContainer = root.findViewById<View>(R.id.cabifyiInfoContainer)

        btnToggleCabify.setOnClickListener {
            if (cabifyInfoContainer.visibility == View.VISIBLE) {
                cabifyInfoContainer.visibility = View.GONE
                btnToggleCabify.text = "Cabify +"
            } else {
                cabifyInfoContainer.visibility = View.VISIBLE
                btnToggleCabify.text = "Cabify -"
            }
        }

        return root
    }


    private fun cargarTotales(fecha: String) {
        Executors.newSingleThreadExecutor().execute {
            val movimientos = DatabaseClient.getInstance(requireContext())
                .appDatabase
                .movimientosDao()
                .getMovimientosByFecha(fecha)

            // Agrupar los movimientos por tipo
            val agrupadoPorTipo = movimientos.groupBy { it.tipo ?: "Desconocido" }
            val resultadosPorTipo = mutableMapOf<String, Map<String, Double>>()

            for ((tipo, listaPorTipo) in agrupadoPorTipo) {
                // Agrupar por método de pago
                val agrupadoPorMetodo = listaPorTipo.groupBy { it.metodoDePago }
                val totalesPorMetodo = agrupadoPorMetodo.mapValues { (_, lista) ->
                    lista.sumOf { it.valor } // Sumar los valores por método de pago
                }
                resultadosPorTipo[tipo] = totalesPorMetodo
            }

            // Actualizar los TextViews en la UI en el hilo principal
            activity?.runOnUiThread {
                // Taxi
                val taxi = resultadosPorTipo["Taxi"] ?: emptyMap()
                tarjetaInfoTaxi.text = "${"%.2f".format(taxi["Tarjeta"] ?: 0.0)}"
                efectivoInfoTaxi.text = "${"%.2f".format(taxi["Efectivo"] ?: 0.0)}"
                totalGeneralInfoTaxi.text = "${"%.2f".format(taxi.values.sum())}"

                // Radio Taxi
                val radiotaxi = resultadosPorTipo["Radio Taxi"] ?: emptyMap()
                tarjetaInfo.text = "${"%.2f".format(radiotaxi["Tarjeta"] ?: 0.0)}"
                abonadoInfo.text = "${"%.2f".format(radiotaxi["Abonado"] ?: 0.0)}"
                efectivoInfo.text = "${"%.2f".format(radiotaxi["Efectivo"] ?: 0.0)}"
                retornoInfo.text = "${"%.2f".format(radiotaxi["Retorno"] ?: 0.0)}"
                totalGeneralInfoRadioTaxi.text = "${"%.2f".format(radiotaxi.values.sum())}"

                // Uber
                val uber = resultadosPorTipo["Uber"] ?: emptyMap()
                tarjetaInfoUber.text = "${"%.2f".format(uber["Tarjeta"] ?: 0.0)}"
                efectivoInfoUber.text = "${"%.2f".format(uber["Efectivo"] ?: 0.0)}"
                totalGeneralInfoUber.text = "${"%.2f".format(uber.values.sum())}"

                // Bolt
                val bolt = resultadosPorTipo["Bolt"] ?: emptyMap()
                tarjetaInfoBolt.text = "${"%.2f".format(bolt["Tarjeta"] ?: 0.0)}"
                efectivoInfoBolt.text = "${"%.2f".format(bolt["Efectivo"] ?: 0.0)}"
                totalGeneralInfoBolt.text = "${"%.2f".format(bolt.values.sum())}"

                // Cabify
                val cabify = resultadosPorTipo["Cabify"] ?: emptyMap()
                tarjetaInfoCabify.text = "${"%.2f".format(cabify["Tarjeta"] ?: 0.0)}"
                efectivoInfoCabify.text = "${"%.2f".format(cabify["Efectivo"] ?: 0.0)}"
                totalGeneralInfoCabify.text = "${"%.2f".format(cabify.values.sum())}"

                // Imprimir todos los resultados por tipo y método de pago en consola (si quieres verlo)
                resultadosPorTipo.forEach { (tipo, totales) ->
                    println("Tipo: $tipo")
                    totales.forEach { (metodo, total) ->
                        println("  $metodo: ${"%.2f".format(total)}")
                    }
                }

            }
        }
    }


    private fun cargarTotalesSemanalesYMensuales() {
        fecha?.let { fechaStr ->
            Executors.newSingleThreadExecutor().execute {
                val dao = DatabaseClient.getInstance(requireContext())
                    .appDatabase
                    .movimientosDao()

                val formatterEntrada = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val formatterSalida = DateTimeFormatter.ofPattern("yyyy-MM-dd")

                try {
                    val fechaSeleccionada = java.time.LocalDate.parse(fechaStr, formatterEntrada)

                    val inicioSemana =
                        fechaSeleccionada.with(DayOfWeek.MONDAY).format(formatterSalida)
                    val finSemana = fechaSeleccionada.with(DayOfWeek.SUNDAY).format(formatterSalida)

                    val inicioMes = fechaSeleccionada.withDayOfMonth(1).format(formatterSalida)
                    val finMes = fechaSeleccionada.withDayOfMonth(fechaSeleccionada.lengthOfMonth())
                        .format(formatterSalida)
                    val dia = dao.getMovimientosByFecha(fechaStr)
                    val semana = dao.getMovimientosEntreFechas2(inicioSemana, finSemana)
                    val mes = dao.getMovimientosEntreFechas(inicioMes, finMes)

                    val totalDia = dia.sumOf{ it.valor }
                    val totalSemana = semana.sumOf { it.valor }
                    val totalMes = mes.sumOf { it.valor }
                    val propinasMes = mes.sumOf { it.propina }

                    activity?.runOnUiThread {
                        totalDiaInfo.text = "${"%.2f".format(totalDia)}"
                        totalSemanalInfo.text = "${"%.2f".format(totalSemana)}"
                        totalMensualInfo.text = "${"%.2f".format(totalMes)}"
                        totalPropinas.text = "${"%.2f".format(propinasMes)}"
                    }
                } catch (e: Exception) {
                    Log.e("FirstFragment", "Error al parsear la fecha del argumento", e)
                }
            }
        }
    }

}
