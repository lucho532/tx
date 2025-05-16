package com.luchodevs.tx.dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.luchodevs.tx.entity.Movimiento;

import java.util.List;


@Dao
public interface MovimientoDao {
    @Insert
    void insert(Movimiento movimiento);

    @Query("SELECT * FROM Movimiento WHERE fecha = :fecha AND tipo = :tipo ORDER BY id ASC LIMIT 1")
    Movimiento obtenerPrimeroPorFecha(String fecha, String tipo);

    @Query("SELECT * FROM Movimiento WHERE fecha = :fecha AND horaInicio IS NOT NULL LIMIT 1")
    Movimiento obtenerMovimientoConInicio(String fecha);
    @Query("SELECT * FROM Movimiento WHERE fecha = :fecha AND valor > 0.00 ORDER BY hora DESC LIMIT 1")
    Movimiento obtenerUltimoPorFecha(String fecha);
    @Query("SELECT * FROM Movimiento")
    List<Movimiento> getAllMovimientos();
    @Query("DELETE FROM Movimiento WHERE  valor = 0.00 AND fecha = :fecha")
    void eliminarMovimientosConValorCero(String fecha);
    @Query("SELECT * FROM movimiento WHERE fecha = :fecha AND valor > 0.00 ORDER BY fechaHoraCompleta DESC LIMIT 1")
    Movimiento obtenerUltimoMovimientoFechaCompleta(String fecha);

    @Query("SELECT * FROM movimiento WHERE fecha = :fecha AND tipo = 'Final de Jornada' LIMIT 1")
    Movimiento obtenerFinalDeJornada(String fecha);
    @Query("SELECT SUM(propina) FROM movimiento WHERE fecha = :fecha")
    Double obtenerTotalPropinaPorFecha(String fecha);
    @Query("SELECT * FROM Movimiento WHERE fecha = :fecha AND valor > 0.00")
    List<Movimiento> getMovimientosByFecha(String fecha);

    @Query("SELECT * FROM movimiento WHERE date(substr(fecha, 7, 4) || '-' || substr(fecha, 4, 2) || '-' || substr(fecha, 1, 2)) BETWEEN :inicio AND :fin")
    List<Movimiento> getMovimientosEntreFechas(String inicio, String fin);

    @Query("SELECT * FROM movimiento")
    List<Movimiento> getall();


    @Query("SELECT * FROM movimiento WHERE strftime('%Y-%m-%d', substr(fecha, 7, 4) || '-' || substr(fecha, 4, 2) || '-' || substr(fecha, 1, 2)) BETWEEN :inicio AND :fin")
    List<Movimiento> getMovimientosEntreFechas2(String inicio, String fin);
    @Query("SELECT EXISTS(SELECT 1 FROM movimiento WHERE fecha = :fecha LIMIT 1)")
    Boolean existeMovimientoEnFecha(String fecha);
    @Query("SELECT EXISTS(SELECT 1 FROM movimiento WHERE fecha = :fecha AND tipo = 'Final')")
    Boolean existeFinalEnFecha(String fecha);

    @Update
    void update(Movimiento movimiento);

    @Delete
    void delete(Movimiento movimiento);

}

