// MovimientoAdapter.kt
package com.tx.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tx.R
import com.tx.entity.Movimiento

class MovimientoAdapter(
    private val movimientos: List<Movimiento>,
    private val onEditClick: (Movimiento) -> Unit,
    private val onDeleteClick: (Movimiento) -> Unit
) : RecyclerView.Adapter<MovimientoAdapter.MovimientoViewHolder>() {

    inner class MovimientoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFecha: TextView = view.findViewById(R.id.tvFecha)
        val tvValor: TextView = view.findViewById(R.id.tvValor)
        val tipo: TextView = view.findViewById(R.id.tipo)
        val propina: TextView = view.findViewById(R.id.propina)
        val tvMetodoDePago: TextView = view.findViewById(R.id.tvMetodoDePago)
        val tvHora: TextView = view.findViewById(R.id.tvHora)
        val btnEditar: Button = view.findViewById(R.id.btn_editar)
        val btnEliminar: Button = view.findViewById(R.id.btn_eliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovimientoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_movimiento, parent, false)
        return MovimientoViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovimientoViewHolder, position: Int) {
        val movimiento = movimientos[position]
        holder.tvFecha.text = "Fecha: ${movimiento.fecha}"
        holder.tvValor.text = "Valor: ${movimiento.valor}"
        holder.propina.text = "Propina: ${"%.2f".format(movimiento.propina)}"
        holder.tipo.text= "Servicio de: ${movimiento.tipo}"
        holder.tvMetodoDePago.text = "Método de Pago: ${movimiento.metodoDePago}"
        holder.tvHora.text = "Hora: ${movimiento.hora}"

        holder.btnEditar.setOnClickListener {
            onEditClick(movimiento)
        }

        holder.btnEliminar.setOnClickListener {
            onDeleteClick(movimiento)
        }
    }

    override fun getItemCount(): Int = movimientos.size
}


