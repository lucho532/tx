package com.tx

import android.app.AlertDialog
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
import com.tx.adapter.MovimientoAdapter
import com.tx.database.DatabaseClient
import com.tx.entity.Movimiento
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
                    com.tx.generador.generarArchivoMovimientos(
                        requireContext(),
                        movimientos,
                        fechaSeleccionada
                    )
                            Toast.makeText(requireContext(), "Archivo generado", Toast.LENGTH_SHORT).show()
                } else {
                            Toast.makeText(requireContext(), "No hay movimientos para esta fecha.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                        Toast.makeText(requireContext(), "Error generando archivo: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun cargarMovimientos() {
        val fechaSeleccionada = fecha ?: return

        Executors.newSingleThreadExecutor().execute {
            movimientos = DatabaseClient.getInstance(requireContext())
                .appDatabase
                .movimientosDao()
                .getMovimientosByFecha(fechaSeleccionada)

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
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_editar_movimiento, null)
        val etNuevoValor = dialogView.findViewById<EditText>(R.id.etNuevoValor)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radioGroupMetodoPago)

        etNuevoValor.setText(movimiento.valor.toString())

        when (movimiento.metodoDePago) {
            "Tarjeta" -> radioGroup.check(R.id.rbTarjeta)
            "Abonados" -> radioGroup.check(R.id.rbAbonados)
            "Efectivo" -> radioGroup.check(R.id.rbEfectivo)
            "Retorno" -> radioGroup.check(R.id.rbRetorno)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Editar Movimiento")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoValor = etNuevoValor.text.toString().toDoubleOrNull()
                val metodoPagoSeleccionado = when (radioGroup.checkedRadioButtonId) {
                    R.id.rbTarjeta -> "Tarjeta"
                    R.id.rbAbonados -> "Abonados"
                    R.id.rbEfectivo -> "Efectivo"
                    R.id.rbRetorno -> "Retorno"
                    else -> null
                }

                if (nuevoValor != null && metodoPagoSeleccionado != null) {
                    movimiento.valor = nuevoValor
                    movimiento.metodoDePago = metodoPagoSeleccionado

                    Executors.newSingleThreadExecutor().execute {
                        DatabaseClient.getInstance(requireContext())
                            .appDatabase
                            .movimientosDao()
                            .update(movimiento)
                    }
                        Toast.makeText(requireContext(), "Movimiento actualizado", Toast.LENGTH_SHORT).show()
                        cargarMovimientos()
                } else {

                        Toast.makeText(requireContext(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()

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
