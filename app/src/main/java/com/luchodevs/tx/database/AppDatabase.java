package com.luchodevs.tx.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.luchodevs.tx.dao.MovimientoDao;
import com.luchodevs.tx.entity.Movimiento;

    @Database(entities = {Movimiento.class}, version = 5)
    public abstract class AppDatabase extends RoomDatabase {
        public abstract MovimientoDao movimientosDao();
    }
