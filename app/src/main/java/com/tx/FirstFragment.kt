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

    private lateinit var tarjetaInfo: TextView
    private lateinit var abonadoInfo: TextView
    private lateinit var efectivoInfo: TextView
    private lateinit var retornoInfo: TextView
    private lateinit var totalGeneralInfo: TextView
    private lateinit var porcentajeInfo: TextView
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
        tarjetaInfo = root.findViewById(R.id.tarjeta_info)
        abonadoInfo = root.findViewById(R.id.abonado_info)
        efectivoInfo = root.findViewById(R.id.efectivo_info)
        retornoInfo = root.findViewById(R.id.retorno_info)
        totalGeneralInfo = root.findViewById(R.id.total_general_info)
        porcentajeInfo = root.findViewById(R.id.porcentaje_info)
        totalSemanalInfo = root.findViewById(R.id.total_semanal_info)
        totalMensualInfo = root.findViewById(R.id.total_mensual_info)
        totalPropinas= root.findViewById(R.id.total_propinas)

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

        return root
    }




    private fun cargarTotales(fecha: String) {
        Executors.newSingleThreadExecutor().execute {
            val movimientos = DatabaseClient.getInstance(requireContext())
                .appDatabase
                .movimientosDao()
                .getMovimientosByFecha(fecha)

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
            val porcentaje = totalGeneral *0.45


            activity?.runOnUiThread {
                tarjetaInfo.text = "${"%.2f".format(totalTarjeta)} (${tarjeta.size} V)"
                abonadoInfo.text = "${"%.2f".format(totalAbonado)} (${abonado.size} V)"
                efectivoInfo.text = "${"%.2f".format(totalEfectivo)} (${efectivo.size} V)"
                retornoInfo.text = "${"%.2f".format(totalRetorno)} (${retorno.size} V)"
                totalGeneralInfo.text = "${"%.2f".format(totalGeneral)} (${movimientos.size} V)"
                porcentajeInfo.text = "${"%.2f".format(porcentaje)}"
            }
        }
    }

    private fun cargarTotalesSemanalesYMensuales() {
        Executors.newSingleThreadExecutor().execute {
            val dao = DatabaseClient.getInstance(requireContext())
                .appDatabase
                .movimientosDao()

            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val formatterSalida = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val hoy = java.time.LocalDate.now()

            val inicioSemana = hoy.with(DayOfWeek.MONDAY).format(formatterSalida)
            val finSemana = hoy.with(DayOfWeek.SUNDAY).format(formatterSalida)

            val inicioMes = hoy.withDayOfMonth(1).format(formatter)
            val finMes = hoy.withDayOfMonth(hoy.lengthOfMonth()).format(formatter)

            val semana = dao.getMovimientosEntreFechas2(inicioSemana, finSemana)
            val mes = dao.getMovimientosEntreFechas(inicioMes, finMes)


            val totalSemana = semana.sumOf { it.valor }
            val totalMes = mes.sumOf { it.valor }
            val propinasMes = mes.sumOf { it.propina }

            activity?.runOnUiThread {
                totalSemanalInfo.text = "${"%.2f".format(totalSemana)} (${semana.size} V)"
                totalMensualInfo.text = "${"%.2f".format(totalMes)} (${mes.size} V)"
                totalPropinas.text = "${"%.2f".format(propinasMes)} (${mes.size} V)"
            }
        }
    }
}
