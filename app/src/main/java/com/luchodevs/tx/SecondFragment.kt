package com.luchodevs.tx

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.luchodevs.tx.adapter.MovimientoAdapter
import com.luchodevs.tx.database.DatabaseClient
import com.luchodevs.tx.entity.Movimiento
import java.util.concurrent.Executors

class SecondFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var movimientosAdapter: MovimientoAdapter
    private lateinit var movimientos: List<Movimiento>

    private var fecha: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_second, container, false)

        fecha = arguments?.getString("fecha")

        recyclerView = root.findViewById(R.id.recyclerViewMovimientos)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val generarArchivoButton = root.findViewById<Button>(R.id.generar_archivo_button)
        generarArchivoButton.setOnClickListener {
            generarArchivoMovimientos()
        }

        val btnVolver = root.findViewById<Button>(R.id.btn_volver)
        btnVolver.setOnClickListener {
            findNavController().popBackStack()
        }

        cargarMovimientos()

        return root
    }

    private fun generarArchivoMovimientos() {
        val fechaSeleccionada = fecha ?: return

        Executors.newSingleThreadExecutor().execute {
            try {
                val movimientos = DatabaseClient.getInstance(requireContext())
                    .appDatabase
                    .movimientosDao()
                    .getMovimientosByFecha(fechaSeleccionada)

                if (movimientos != null && movimientos.isNotEmpty()) {
                    try {
                        // Intentar generar y abrir el archivo
                        com.luchodevs.tx.generador.generarArchivoMovimientos(
                            requireContext(),
                            movimientos,
                            fechaSeleccionada
                        )

                        activity?.runOnUiThread {
                            Toast.makeText(requireContext(), "Archivo generado exitosamente", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: ActivityNotFoundException) {
                        activity?.runOnUiThread {
                            Toast.makeText(
                                requireContext(),
                                "No se encontró una aplicación compatible. Por favor, instala Microsoft Excel o una app para abrir archivos .xlsx.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } else {
                    activity?.runOnUiThread {
                        Toast.makeText(
                            requireContext(),
                            "No hay movimientos para esta fecha.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                activity?.runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        "Error generando archivo: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }


    private fun cargarMovimientos() {
        val fechaSeleccionada = fecha ?: return

        Executors.newSingleThreadExecutor().execute {
            movimientos = DatabaseClient.getInstance(requireContext())
                .appDatabase
                .movimientosDao()
                .getMovimientosByFechaCompletaDesc(fechaSeleccionada)

            activity?.runOnUiThread {
                movimientosAdapter = MovimientoAdapter(
                    movimientos,
                    onEditClick = { movimiento -> mostrarDialogoEditar(movimiento) },
                    onDeleteClick = { movimiento -> eliminarMovimiento(movimiento) }
                )
                recyclerView.adapter = movimientosAdapter
            }
        }
    }

    private fun mostrarDialogoEditar(movimiento: Movimiento) {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_editar_movimiento, null)
        val etNuevoValor = dialogView.findViewById<EditText>(R.id.etNuevoValor)
        val radioGroupMetodo = dialogView.findViewById<RadioGroup>(R.id.radioGroupMetodoPago)
        val radioGroupTipo = dialogView.findViewById<RadioGroup>(R.id.radioGroupTipo)
        etNuevoValor.setText(movimiento.valor.toString())
        when (movimiento.tipo) {
            "Tipo1" -> radioGroupTipo.check(R.id.servicio_tipo1)
            "Tipo2" -> radioGroupTipo.check(R.id.servicio_tipo2)
            "Tipo3" -> radioGroupTipo.check(R.id.servicio_tipo3)
            "Tipo4" -> radioGroupTipo.check(R.id.servicio_tipo4)
            "Tipo5" -> radioGroupTipo.check(R.id.servicio_tipo5)
        }
        when (movimiento.metodoDePago) {
            "Metodo1" -> radioGroupMetodo.check(R.id.rbTarjeta)
            "Abonados" -> radioGroupMetodo.check(R.id.rbAbonados)
            "Metodo2" -> radioGroupMetodo.check(R.id.rbEfectivo)
            "Metodo4" -> radioGroupMetodo.check(R.id.rbpago_metodo4)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Editar Movimiento")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoValor = etNuevoValor.text.toString().toDoubleOrNull()
                val tipoSeleccionado= when(radioGroupTipo.checkedRadioButtonId){
                    R.id.servicio_tipo1 -> "Tipo1"
                    R.id.servicio_tipo2 -> "Tipo2"
                    R.id.servicio_tipo3 -> "Tipo3"
                    R.id.servicio_tipo4 -> "Tipo4"
                    R.id.servicio_tipo5 -> "Tipo5"
                    else -> null
                }
                val metodoPagoSeleccionado = when (radioGroupMetodo.checkedRadioButtonId) {
                    R.id.rbTarjeta -> "Metodo1"
                    R.id.rbAbonados -> "Abonados"
                    R.id.rbEfectivo -> "Metodo2"
                    R.id.rbpago_metodo4 -> "Metodo4"
                    else -> null
                }

                if (nuevoValor != null && metodoPagoSeleccionado != null && tipoSeleccionado != null) {
                    movimiento.valor = nuevoValor
                    movimiento.tipo = tipoSeleccionado
                    movimiento.metodoDePago = metodoPagoSeleccionado

                    Executors.newSingleThreadExecutor().execute {
                        DatabaseClient.getInstance(requireContext())
                            .appDatabase
                            .movimientosDao()
                            .update(movimiento)
                    }
                    Toast.makeText(requireContext(), "Movimiento actualizado", Toast.LENGTH_SHORT)
                        .show()
                    cargarMovimientos()
                } else {

                    Toast.makeText(
                        requireContext(),
                        "Por favor, completa todos los campos",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()
    }

    private fun eliminarMovimiento(movimiento: Movimiento) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar")
            .setMessage("¿Seguro que querés eliminar este movimiento?")
            .setPositiveButton("Sí") { _, _ ->
                Executors.newSingleThreadExecutor().execute {
                    DatabaseClient.getInstance(requireContext())
                        .appDatabase
                        .movimientosDao()
                        .delete(movimiento)

                    activity?.runOnUiThread {
                        cargarMovimientos()
                    }
                }
            }
            .setNegativeButton("No", null)
            .show()
    }
}
