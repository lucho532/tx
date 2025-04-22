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
    @Query("SELECT * FROM Movimiento WHERE fecha = :fecha ORDER BY hora DESC LIMIT 1")
    Movimiento obtenerUltimoPorFecha(String fecha);
    @Query("SELECT * FROM Movimiento ORDER BY fecha DESC, hora DESC")
    List<Movimiento> getAllMovimientos();

    @Query("SELECT * FROM Movimiento WHERE fecha = :fecha")
    List<Movimiento> getMovimientosByFecha(String fecha);

    @Query("SELECT * FROM movimiento WHERE fecha BETWEEN :inicio AND :fin")
    List<Movimiento> getMovimientosEntreFechas(String inicio, String fin);
    @Update
    void update(Movimiento movimiento);

    @Delete
    void delete(Movimiento movimiento);

    @Query("DELETE FROM Movimiento")
    void deleteAll();

    @Query("DELETE FROM Movimiento WHERE id = :id")
    void deleteById(int id);
}

