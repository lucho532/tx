// MovimientoAdapter.kt
package com.luchodevs.tx.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.luchodevs.tx.R
import com.luchodevs.tx.entity.Movimiento

class MovimientoAdapter(
    private val movimientos: List<Movimiento>,
    private val onEditClick: (Movimiento) -> Unit,
    private val onDeleteClick: (Movimiento) -> Unit
) : RecyclerView.Adapter<MovimientoAdapter.MovimientoViewHolder>() {

    inner class MovimientoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFecha: TextView = view.findViewById(R.id.tvFecha)
        val tvFechaServicio: TextView = view.findViewById(R.id.tvFechaServicio)
        val tvValor: TextView = view.findViewById(R.id.tvValor)
        val tipo: TextView = view.findViewById(R.id.tipo)
        val propina: TextView = view.findViewById(R.id.propina)
        val tvMetodoDePago: TextView = view.findViewById(R.id.tvMetodoDePago)
        val btnEditar: Button = view.findViewById(R.id.btn_editar)
        val btnEliminar: Button = view.findViewById(R.id.btn_eliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovimientoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_movimiento, parent, false)
        return MovimientoViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovimientoViewHolder, position: Int) {
        val movimiento = movimientos[position]
        holder.tvFecha.text = "Fecha Jornada: ${movimiento.fecha}"
        holder.tvFechaServicio.text = "Fecha Servicio: ${movimiento.fechaHoraCompleta}"
        holder.tvValor.text = "Valor: ${movimiento.valor}"
        holder.propina.text = "Propina: ${"%.2f".format(movimiento.propina)}"
        holder.tipo.text= "Servicio de: ${movimiento.tipo}"
        holder.tvMetodoDePago.text = "Método de Pago: ${movimiento.metodoDePago}"


        holder.btnEditar.setOnClickListener {
            onEditClick(movimiento)
        }

        holder.btnEliminar.setOnClickListener {
            onDeleteClick(movimiento)
        }
    }

    override fun getItemCount(): Int = movimientos.size
}


