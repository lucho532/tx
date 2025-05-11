package com.tx.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "movimiento")
public class Movimiento {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private double valor;
    private String tipo;
    private String metodoDePago;
    private String fecha;
    private String hora;
    private String horaInicio;
    private String horaFin;
    private String horaTotal;
    private double propina;


    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }

    public String getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(String horaFin) {
        this.horaFin = horaFin;
    }

    public String getHoraTotal() {
        return horaTotal;
    }

    public void setHoraTotal(String horaTotal) {
        this.horaTotal = horaTotal;
    }

    public double getPropina() {
        return propina;
    }

    public void setPropina(double propina) {
        this.propina = propina;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public String getMetodoDePago() {
        return metodoDePago;
    }

    public void setMetodoDePago(String metodoDePago) {
        this.metodoDePago = metodoDePago;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }


    @Override
    public String toString() {
        return "Movimiento{" +
                "id=" + id +
                ", valor=" + valor +
                ", tipo='" + tipo + '\'' +
                ", metodoDePago='" + metodoDePago + '\'' +
                ", fecha='" + fecha + '\'' +
                ", hora='" + hora + '\'' +
                ", horaInicio='" + horaInicio + '\'' +
                ", horaFin='" + horaFin + '\'' +
                ", horaTotal='" + horaTotal + '\'' +
                ", propina=" + propina +
                '}';
    }

}
