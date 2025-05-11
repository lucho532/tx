package com.tx.dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.tx.entity.Movimiento;

import java.util.List;


@Dao
public interface MovimientoDao {
    @Insert
    void insert(Movimiento movimiento);

    @Query("SELECT * FROM Movimiento WHERE fecha = :fecha ORDER BY id ASC LIMIT 1")
    Movimiento obtenerPrimeroPorFecha(String fecha);

    @Query("SELECT * FROM Movimiento WHERE fecha = :fecha ORDER BY hora DESC LIMIT 1")
    Movimiento obtenerUltimoPorFecha(String fecha);
    @Query("SELECT * FROM Movimiento")
    List<Movimiento> getAllMovimientos();

    @Query("SELECT * FROM movimiento WHERE fecha = :fecha AND tipo = 'Final de Jornada' LIMIT 1")
    Movimiento obtenerFinalDeJornada(String fecha);
    @Query("SELECT SUM(propina) FROM movimiento WHERE fecha = :fecha")
    Double obtenerTotalPropinaPorFecha(String fecha);
    @Query("SELECT * FROM Movimiento WHERE fecha = :fecha")
    List<Movimiento> getMovimientosByFecha(String fecha);

    @Query("SELECT * FROM movimiento WHERE date(substr(fecha, 7, 4) || '-' || substr(fecha, 4, 2) || '-' || substr(fecha, 1, 2)) BETWEEN :inicio AND :fin")
    List<Movimiento> getMovimientosEntreFechas(String inicio, String fin);


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

