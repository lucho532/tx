package com.luchodevs.tx

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.luchodevs.tx.database.DatabaseClient
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors

class FirstFragment : Fragment() {


    private lateinit var taxiTarjetaSemana: TextView
    private lateinit var taxiEfectivoSemana: TextView
    private lateinit var taxiTotalSemana: TextView
    private lateinit var taxiTarjetaMes: TextView
    private lateinit var taxiEfectivoMes: TextView
    private lateinit var taxiTotalMes: TextView

    private lateinit var tipo1diario: TextView
    private lateinit var tipo1semanal: TextView
    private lateinit var tipo1mensual: TextView


    private lateinit var radioTaxiTarjetaSemana: TextView
    private lateinit var radioTaxiEfectivoSemana: TextView
    private lateinit var radioTaxiAbonadoSemana: TextView
    private lateinit var radioTaxiTotalSemana: TextView

    private lateinit var radioTaxiTarjetaMes: TextView
    private lateinit var radioTaxiEfectivoMes: TextView
    private lateinit var radioTaxiAbonadoMes: TextView
    private lateinit var radioTaxiTotalMes: TextView


    private lateinit var uberTarjetaSemana: TextView
    private lateinit var uberEfectivoSemana: TextView
    private lateinit var uberTotalSemana: TextView
    private lateinit var uberTarjetaMes: TextView
    private lateinit var uberEfectivoMes: TextView
    private lateinit var uberTotalMes: TextView
    private lateinit var tipo3diario: TextView
    private lateinit var tipo3semanal: TextView
    private lateinit var tipo3mensual: TextView

    private lateinit var boltTarjetaSemana: TextView
    private lateinit var boltEfectivoSemana: TextView
    private lateinit var boltTotalSemana: TextView
    private lateinit var boltTarjetaMes: TextView
    private lateinit var boltEfectivoMes: TextView
    private lateinit var boltTotalMes: TextView
    private lateinit var tipo4diario : TextView
    private lateinit var tipo4semanal : TextView
    private lateinit var tipo4mensual : TextView

    private lateinit var cabifyTarjetaSemana: TextView
    private lateinit var cabifyEfectivoSemana: TextView
    private lateinit var cabifyTotalSemana: TextView
    private lateinit var cabifyTarjetaMes: TextView
    private lateinit var cabifyEfectivoMes: TextView
    private lateinit var cabifyTotalMes: TextView
    private lateinit var tipo5diario : TextView
    private lateinit var tipo5semanal : TextView
    private lateinit var tipo5mensual : TextView
    private lateinit var tarjetaInfoTaxi: TextView
    private lateinit var efectivoInfoTaxi: TextView
    private lateinit var totalGeneralInfoTaxi: TextView


    private lateinit var tarjetaInfo: TextView
    private lateinit var efectivoInfo: TextView
    private lateinit var totalGeneralInfoRadioTaxi: TextView
    private lateinit var abonadoInfo: TextView


    private lateinit var tarjetaInfoUber: TextView
    private lateinit var efectivoInfoUber: TextView
    private lateinit var totalGeneralInfoUber: TextView


    private lateinit var tarjetaInfoBolt: TextView
    private lateinit var efectivoInfoBolt: TextView
    private lateinit var totalGeneralInfoBolt: TextView


    private lateinit var tarjetaInfoCabify: TextView
    private lateinit var efectivoInfoCabify: TextView
    private lateinit var totalGeneralInfoCabify: TextView


    private lateinit var resumentarjetadiario: TextView
    private lateinit var resumenefectivodiario: TextView
    private lateinit var resumentotaldiario: TextView

    private lateinit var resumentarjetasemana: TextView
    private lateinit var resumenefectivosemana: TextView
    private lateinit var resumentotalsemana: TextView

    private lateinit var resumentarjetamensual: TextView
    private lateinit var resumenefectivomensual: TextView
    private lateinit var resumentotalmensual: TextView

    private lateinit var resumenAbonadoDiario: TextView
    private lateinit var resumenAbonadoSemana: TextView
    private lateinit var resumenAbonadomensual: TextView


    private var fecha: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_first, container, false)
        val prefs = requireContext().getSharedPreferences("nombres_editables", Context.MODE_PRIVATE)


        val containerTipo1 = root.findViewById<TextView>(R.id.containerTipo1)
        val nombreTipo1 = prefs.getString("Tipo1", "Taxi") ?: "Taxi"
        containerTipo1.text = nombreTipo1

        val containerTipo2 = root.findViewById<TextView>(R.id.containerTipo2)
        val nombreTipo2 = prefs.getString("Tipo2", "Emisora") ?: "Emisora"
        containerTipo2.text = nombreTipo2

        val containerTipo3 = root.findViewById<TextView>(R.id.containerTipo3)
        val nombreTipo3 = prefs.getString("Tipo3", "Uber") ?: "Uber"
        containerTipo3.text = nombreTipo3

        val containerTipo4 = root.findViewById<TextView>(R.id.containerTipo4)
        val nombreTipo4 = prefs.getString("Tipo4", "Bolt") ?: "Bolt"
        containerTipo4.text = nombreTipo4

        val containerTipo5 = root.findViewById<TextView>(R.id.containerTipo5)
        val nombreTipo5 = prefs.getString("Tipo5", "Cabify") ?: "Cabify"
        containerTipo5.text = nombreTipo5


        val nombreTaxi = prefs.getString("Tipo1", "Taxi") ?: "Taxi"
        val nombreRadioTaxi = prefs.getString("Tipo2", "Emisora") ?: "Emisora"
        val nombreUber = prefs.getString("Tipo3", "Uber") ?: "Uber"
        val nombreBolt = prefs.getString("Tipo4", "Bolt") ?: "Bolt"
        val nombreCabify = prefs.getString("Tipo5", "Cabify") ?: "Cabify"


        // Referencias UI

        // Totales Diarios
        tarjetaInfoTaxi = root.findViewById(R.id.taxi_tarjeta_info_diario)
        efectivoInfoTaxi = root.findViewById(R.id.taxi_efectivo_info_diario)
        totalGeneralInfoTaxi = root.findViewById(R.id.taxi_total_general_info_diario)

        tipo1diario = root.findViewById(R.id.tipo1diario)
        tipo1semanal = root.findViewById(R.id.tipo1semanal)
        tipo1mensual = root.findViewById(R.id.tipo1mensual)

        tipo3diario = root.findViewById(R.id.tipo3diario)
        tipo3semanal = root.findViewById(R.id.tipo3semanal)
        tipo3mensual = root.findViewById(R.id.tipo3mensual)

        tipo4diario = root.findViewById(R.id.tipo4diario)
        tipo4semanal = root.findViewById(R.id.tipo4semanal)
        tipo4mensual = root.findViewById(R.id.tipo4mensual)

        tipo5diario = root.findViewById(R.id.tipo5diario)
        tipo5semanal = root.findViewById(R.id.tipo5semanal)
        tipo5mensual = root.findViewById(R.id.tipo5mensual)

        tarjetaInfo = root.findViewById(R.id.radiotaxi_tarjeta_info_diario)
        abonadoInfo = root.findViewById(R.id.radiotaxi_abonado_info_diario)
        efectivoInfo = root.findViewById(R.id.radiotaxi_efectivo_info_diario)
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

        //Totales Semanales
        taxiTarjetaSemana = root.findViewById(R.id.taxi_tarjeta_info_semanal)
        taxiEfectivoSemana = root.findViewById(R.id.taxi_efectivo_info_semanal)
        taxiTotalSemana = root.findViewById(R.id.taxi_total_general_info_semanal)
        taxiTarjetaMes = root.findViewById(R.id.taxi_tarjeta_info_mensual)
        taxiEfectivoMes = root.findViewById(R.id.taxi_efectivo_info_mensual)
        taxiTotalMes = root.findViewById(R.id.taxi_total_general_info_mensual)


        radioTaxiTarjetaSemana = root.findViewById(R.id.radiotaxi_tarjeta_info_semanal)
        radioTaxiEfectivoSemana = root.findViewById(R.id.radiotaxi_efectivo_info_semanal)
        radioTaxiAbonadoSemana = root.findViewById(R.id.radiotaxi_abonado_info_semanal)
        radioTaxiTotalSemana = root.findViewById(R.id.radiotaxi_total_info_semanal)

        radioTaxiTarjetaMes = root.findViewById(R.id.radiotaxi_tarjeta_info_mensual)
        radioTaxiEfectivoMes = root.findViewById(R.id.radiotaxi_efectivo_info_mensual)
        radioTaxiAbonadoMes = root.findViewById(R.id.radiotaxi_abonado_info_mensual)
        radioTaxiTotalMes = root.findViewById(R.id.radiotaxi_total_info_mensual)


        uberTarjetaSemana = root.findViewById(R.id.uber_tarjeta_info_semanal)
        uberEfectivoSemana = root.findViewById(R.id.uber_efectivo_info_semanal)
        uberTotalSemana = root.findViewById(R.id.uber_total_info_semanal)
        uberTarjetaMes = root.findViewById(R.id.uber_tarjeta_info_mensual)
        uberEfectivoMes = root.findViewById(R.id.uber_efectivo_info_mensual)
        uberTotalMes = root.findViewById(R.id.uber_total_info_mensual)


        boltTarjetaSemana = root.findViewById(R.id.bolt_tarjeta_info_semanal)
        boltEfectivoSemana = root.findViewById(R.id.bolt_efectivo_info_semanal)
        boltTotalSemana = root.findViewById(R.id.bolt_total_info_semanal)
        boltTarjetaMes = root.findViewById(R.id.bolt_tarjeta_info_mensual)
        boltEfectivoMes = root.findViewById(R.id.bolt_efectivo_info_mensual)
        boltTotalMes = root.findViewById(R.id.bolt_total_info_mensual)

        cabifyTarjetaSemana = root.findViewById(R.id.cabify_tarjeta_info_semanal)
        cabifyEfectivoSemana = root.findViewById(R.id.cabify_efectivo_info_semanal)
        cabifyTotalSemana = root.findViewById(R.id.cabify_total_info_semanal)
        cabifyTarjetaMes = root.findViewById(R.id.cabify_tarjeta_info_mensual)
        cabifyEfectivoMes = root.findViewById(R.id.cabify_efectivo_info_mensual)
        cabifyTotalMes = root.findViewById(R.id.cabify_total_info_mensual)


        // Totales Generales
        resumentarjetadiario = root.findViewById(R.id.resumen_tarjeta_diario)
        resumenefectivodiario = root.findViewById(R.id.resumen_efectivo_info_diario)
        resumentotaldiario = root.findViewById(R.id.resumen_total_info_diario)

        resumentarjetasemana = root.findViewById(R.id.resumen_tarjeta_semanal)
        resumenefectivosemana = root.findViewById(R.id.resumen_efectivo_info_semanal)
        resumentotalsemana = root.findViewById(R.id.resumen_total_info_semanal)

        resumentarjetamensual = root.findViewById(R.id.resumen_tarjeta_mensual)
        resumenefectivomensual = root.findViewById(R.id.resumen_efectivo_info_mensual)
        resumentotalmensual = root.findViewById(R.id.resumen_total_info_mensual)

        resumenAbonadoDiario = root.findViewById(R.id.resume_abonado_info_diario)
        resumenAbonadoSemana = root.findViewById(R.id.resume_abonado_info_semanal)
        resumenAbonadomensual = root.findViewById(R.id.resume_abonado_info_mensual)


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

        btnToggleTaxi.text = "$nombreTaxi +"

        btnToggleTaxi.setOnClickListener {
            if (taxiInfoContainer.visibility == View.VISIBLE) {
                taxiInfoContainer.visibility = View.GONE
                btnToggleTaxi.text = "$nombreTaxi +"
            } else {
                taxiInfoContainer.visibility = View.VISIBLE
                btnToggleTaxi.text = "$nombreTaxi -"
            }
        }


        // Expansión/colapso de la sección RadioTaxi
        val btnToggleRadioTaxi = root.findViewById<Button>(R.id.btnExpandCollapseRadioTaxi)
        val radioTaxiInfoContainer = root.findViewById<View>(R.id.RadiotaxiInfoContainer)

        btnToggleRadioTaxi.text = "$nombreRadioTaxi +"
        btnToggleRadioTaxi.setOnClickListener {
            if (radioTaxiInfoContainer.visibility == View.VISIBLE) {
                radioTaxiInfoContainer.visibility = View.GONE
                btnToggleRadioTaxi.text = "$nombreRadioTaxi +"
            } else {
                radioTaxiInfoContainer.visibility = View.VISIBLE
                btnToggleRadioTaxi.text = "$nombreRadioTaxi -"
            }
        }

        // Expansión/colapso de la sección Uber
        val btnToggleUber = root.findViewById<Button>(R.id.btnExpandCollapseuber)
        val uberInfoContainer = root.findViewById<View>(R.id.uberiInfoContainer)

        btnToggleUber.text = "$nombreUber +"
        btnToggleUber.setOnClickListener {
            if (uberInfoContainer.visibility == View.VISIBLE) {
                uberInfoContainer.visibility = View.GONE
                btnToggleUber.text = "$nombreUber +"
            } else {
                uberInfoContainer.visibility = View.VISIBLE
                btnToggleUber.text = "$nombreUber -"
            }
        }

        // Expansión/colapso de la sección Uber
        val btnToggleBolt = root.findViewById<Button>(R.id.btnExpandCollapseRadiobolt)
        val boltInfoContainer = root.findViewById<View>(R.id.boltiInfoContainer)

        btnToggleBolt.text = "$nombreBolt +"
        btnToggleBolt.setOnClickListener {
            if (boltInfoContainer.visibility == View.VISIBLE) {
                boltInfoContainer.visibility = View.GONE
                btnToggleBolt.text = "$nombreBolt +"
            } else {
                boltInfoContainer.visibility = View.VISIBLE
                btnToggleBolt.text = "$nombreBolt -"
            }
        }

        // Expansión/colapso de la sección Uber
        val btnToggleCabify = root.findViewById<Button>(R.id.btnExpandCollapsecabify)
        val cabifyInfoContainer = root.findViewById<View>(R.id.cabifyiInfoContainer)

        btnToggleCabify.text = "$nombreCabify +"
        btnToggleCabify.setOnClickListener {
            if (cabifyInfoContainer.visibility == View.VISIBLE) {
                cabifyInfoContainer.visibility = View.GONE
                btnToggleCabify.text = "$nombreCabify +"
            } else {
                cabifyInfoContainer.visibility = View.VISIBLE
                btnToggleCabify.text = "$nombreCabify -"
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
                val taxi = resultadosPorTipo["Tipo1"] ?: emptyMap()
                tarjetaInfoTaxi.text = "${"%.2f".format(taxi["Metodo1"] ?: 0.0)}"
                efectivoInfoTaxi.text = "${"%.2f".format(taxi["Metodo2"] ?: 0.0)}"
                tipo1diario.text = "${"%.2f".format(taxi["Metodo3"] ?: 0.0)}"
                totalGeneralInfoTaxi.text = "${"%.2f".format(taxi.values.sum())}"

                // Radio Taxi
                val radiotaxi = resultadosPorTipo["Tipo2"] ?: emptyMap()
                tarjetaInfo.text = "${"%.2f".format(radiotaxi["Metodo1"] ?: 0.0)}"
                abonadoInfo.text = "${"%.2f".format(radiotaxi["Metodo3"] ?: 0.0)}"
                efectivoInfo.text = "${"%.2f".format(radiotaxi["Metodo2"] ?: 0.0)}"
                totalGeneralInfoRadioTaxi.text = "${"%.2f".format(radiotaxi.values.sum())}"

                // Uber
                val uber = resultadosPorTipo["Tipo3"] ?: emptyMap()
                tarjetaInfoUber.text = "${"%.2f".format(uber["Metodo1"] ?: 0.0)}"
                efectivoInfoUber.text = "${"%.2f".format(uber["Metodo2"] ?: 0.0)}"
                tipo3diario.text = "${"%.2f".format(uber["Metodo3"] ?: 0.0)}"
                totalGeneralInfoUber.text = "${"%.2f".format(uber.values.sum())}"

                // Bolt
                val bolt = resultadosPorTipo["Tipo4"] ?: emptyMap()
                tarjetaInfoBolt.text = "${"%.2f".format(bolt["Metodo1"] ?: 0.0)}"
                efectivoInfoBolt.text = "${"%.2f".format(bolt["Metodo2"] ?: 0.0)}"
                tipo4diario.text = "${"%.2f".format(bolt["Metodo3"] ?: 0.0)}"
                totalGeneralInfoBolt.text = "${"%.2f".format(bolt.values.sum())}"

                // Cabify
                val cabify = resultadosPorTipo["Tipo5"] ?: emptyMap()
                tarjetaInfoCabify.text = "${"%.2f".format(cabify["Metodo1"] ?: 0.0)}"
                efectivoInfoCabify.text = "${"%.2f".format(cabify["Metodo2"] ?: 0.0)}"
                tipo5diario.text = "${"%.2f".format(cabify["Metodo3"] ?: 0.0)}"
                totalGeneralInfoCabify.text = "${"%.2f".format(cabify.values.sum())}"

                // Cálculo del resumen general del día
                val totalTarjeta = resultadosPorTipo.values.sumOf { it["Metodo1"] ?: 0.0 }
                val totalEfectivo = resultadosPorTipo.values.sumOf { it["Metodo2"] ?: 0.0 }
                val totalAbonado = resultadosPorTipo.values.sumOf { it["Metodo3"] ?: 0.0 }
                val totalGeneral = resultadosPorTipo.values.sumOf { it.values.sum() }

                // Setear los TextViews de resumen diario
                resumentarjetadiario.text = "%.2f".format(totalTarjeta)
                resumenefectivodiario.text = "%.2f".format(totalEfectivo)
                resumenAbonadoDiario.text = "%.2f".format(totalAbonado)
                resumentotaldiario.text = "%.2f".format(totalGeneral)


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

                    // Totales generales
                    val totalDia = dia.sumOf { it.valor }
                    val totalSemana = semana.sumOf { it.valor }
                    val totalMes = mes.sumOf { it.valor }

                    val totalAbonadoMes =
                        mes.filter { it.metodoDePago == "Metodo3" }.sumOf { it.valor }
                    val efectivoMensual =
                        mes.filter { it.metodoDePago == "Metodo2" }.sumOf { it.valor }
                    val tarjetaMensual =
                        mes.filter { it.metodoDePago == "Metodo1" }.sumOf { it.valor }

                    val propinasMes = mes.sumOf { it.propina }

                    // Agrupaciones por tipo y método de pago
                    val resumenPorTipoSemana =
                        semana.groupBy { it.tipo ?: "Desconocido" }.mapValues { (_, lista) ->
                            lista.groupBy { it.metodoDePago }
                                .mapValues { it.value.sumOf { m -> m.valor } }
                        }

                    val resumenPorTipoMes =
                        mes.groupBy { it.tipo ?: "Desconocido" }.mapValues { (_, lista) ->
                            lista.groupBy { it.metodoDePago }
                                .mapValues { it.value.sumOf { m -> m.valor } }
                        }

                    // Totales generales por método
                    val totalTarjetaSemanal =
                        resumenPorTipoSemana.values.sumOf { it["Metodo1"] ?: 0.0 }
                    val totalEfectivoSemanal =
                        resumenPorTipoSemana.values.sumOf { it["Metodo2"] ?: 0.0 }
                    val totalAbonadoSemanal =
                        resumenPorTipoSemana.values.sumOf { it["Metodo3"] ?: 0.0 }
                    val totalSemanal =
                        totalTarjetaSemanal + totalEfectivoSemanal + totalAbonadoSemanal

                    val totalMensual = tarjetaMensual + efectivoMensual + totalAbonadoMes

                    val tiposServicios = listOf("Tipo1", "Tipo2", "Tipo3", "Tipo4", "Tipo5")

                    activity?.runOnUiThread {
                        resumentarjetasemana.text = "%.2f".format(totalTarjetaSemanal)
                        resumenefectivosemana.text = "%.2f".format(totalEfectivoSemanal)
                        resumenAbonadoSemana.text = "%.2f".format(totalAbonadoSemanal)
                        resumentotalsemana.text = "%.2f".format(totalSemanal)

                        resumentarjetamensual.text = "%.2f".format(tarjetaMensual)
                        resumenefectivomensual.text = "%.2f".format(efectivoMensual)
                        resumenAbonadomensual.text = "%.2f".format(totalAbonadoMes)
                        resumentotalmensual.text = "%.2f".format(totalMensual)

                        // Totales por tipo de servicio
                        for (tipo in tiposServicios) {
                            val semanaTipo = resumenPorTipoSemana[tipo] ?: emptyMap()
                            val mesTipo = resumenPorTipoMes[tipo] ?: emptyMap()

                            val tarjetaSemana = semanaTipo["Metodo1"] ?: 0.0
                            val efectivoSemana = semanaTipo["Metodo2"] ?: 0.0
                            val abonadoSemana = semanaTipo["Metodo3"] ?: 0.0
                            val totalSemanaTipo = tarjetaSemana + efectivoSemana + abonadoSemana

                            val tarjetaMes = mesTipo["Metodo1"] ?: 0.0
                            val efectivoMes = mesTipo["Metodo2"] ?: 0.0
                            val abonadoMes = mesTipo["Metodo3"] ?: 0.0
                            val totalMesTipo = tarjetaMes + efectivoMes + abonadoMes

                            // Asegúrate de tener estos IDs definidos en tu layout
                            when (tipo) {
                                "Tipo1" -> {
                                    taxiTarjetaSemana.text = "%.2f".format(tarjetaSemana)
                                    taxiEfectivoSemana.text = "%.2f".format(efectivoSemana)
                                    tipo1semanal.text = "%.2f".format(abonadoSemana)
                                    taxiTotalSemana.text = "%.2f".format(totalSemanaTipo)
                                    taxiTarjetaMes.text = "%.2f".format(tarjetaMes)
                                    taxiEfectivoMes.text = "%.2f".format(efectivoMes)
                                    tipo1mensual.text = "%.2f".format(abonadoMes)
                                    taxiTotalMes.text = "%.2f".format(totalMesTipo)
                                }

                                "Tipo2" -> {
                                    radioTaxiTarjetaSemana.text = "%.2f".format(tarjetaSemana)
                                    radioTaxiEfectivoSemana.text = "%.2f".format(efectivoSemana)
                                    radioTaxiAbonadoSemana.text = "%.2f".format(abonadoSemana)
                                    radioTaxiTotalSemana.text = "%.2f".format(totalSemanaTipo)

                                    radioTaxiTarjetaMes.text = "%.2f".format(tarjetaMes)
                                    radioTaxiEfectivoMes.text = "%.2f".format(efectivoMes)
                                    radioTaxiAbonadoMes.text = "%.2f".format(abonadoMes)
                                    radioTaxiTotalMes.text = "%.2f".format(totalMesTipo)
                                }

                                "Tipo3" -> {
                                    uberTarjetaSemana.text = "%.2f".format(tarjetaSemana)
                                    uberEfectivoSemana.text = "%.2f".format(efectivoSemana)
                                    tipo3semanal.text = "%.2f".format(abonadoSemana)
                                    uberTotalSemana.text = "%.2f".format(totalSemanaTipo)
                                    uberTarjetaMes.text = "%.2f".format(tarjetaMes)
                                    uberEfectivoMes.text = "%.2f".format(efectivoMes)
                                    tipo3mensual.text = "%.2f".format(abonadoMes)
                                    uberTotalMes.text = "%.2f".format(totalMesTipo)
                                }

                                "Tipo4" -> {
                                    boltTarjetaSemana.text = "%.2f".format(tarjetaSemana)
                                    boltEfectivoSemana.text = "%.2f".format(efectivoSemana)
                                    tipo4semanal.text = "%.2f".format(abonadoSemana)
                                    boltTotalSemana.text = "%.2f".format(totalSemanaTipo)
                                    boltTarjetaMes.text = "%.2f".format(tarjetaMes)
                                    boltEfectivoMes.text = "%.2f".format(efectivoMes)
                                    tipo4mensual.text = "%.2f".format(abonadoSemana)
                                    boltTotalMes.text = "%.2f".format(totalMesTipo)
                                }

                                "Tipo5" -> {
                                    cabifyTarjetaSemana.text = "%.2f".format(tarjetaSemana)
                                    cabifyEfectivoSemana.text = "%.2f".format(efectivoSemana)
                                    tipo5semanal.text = "%.2f".format(abonadoSemana)
                                    cabifyTotalSemana.text = "%.2f".format(totalSemanaTipo)
                                    cabifyTarjetaMes.text = "%.2f".format(tarjetaMes)
                                    cabifyEfectivoMes.text = "%.2f".format(efectivoMes)
                                    tipo5mensual.text = "%.2f".format(abonadoMes)
                                    cabifyTotalMes.text = "%.2f".format(totalMesTipo)
                                }
                            }
                        }
                    }

                } catch (e: Exception) {
                    Log.e("FirstFragment", "Error al parsear la fecha del argumento", e)
                }
            }
        }
    }


}
