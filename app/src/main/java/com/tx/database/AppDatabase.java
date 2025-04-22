package com.tx.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.tx.dao.MovimientoDao;
import com.tx.entity.Movimiento;

    @Database(entities = {Movimiento.class}, version = 1)
    public abstract class AppDatabase extends RoomDatabase {
        public abstract MovimientoDao movimientosDao();
    }

