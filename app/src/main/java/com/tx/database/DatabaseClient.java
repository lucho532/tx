package com.tx.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.tx.entity.Movimiento;

public class DatabaseClient {

    private Context mCtx;
    private static DatabaseClient mInstance;

    // Base de datos
    private AppDatabase appDatabase;

    private DatabaseClient(Context mCtx) {
        this.mCtx = mCtx;

        // Crear base de datos con Room
        appDatabase = Room.databaseBuilder(mCtx, AppDatabase.class, "mi_base_datos")
                .addMigrations(MIGRATION_1_2)
                .build();

    }

    public static synchronized DatabaseClient getInstance(Context mCtx) {
        if (mInstance == null) {
            mInstance = new DatabaseClient(mCtx);
        }
        return mInstance;
    }

    public AppDatabase getAppDatabase() {
        return appDatabase;
    }


    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE movimiento ADD COLUMN horaInicio TEXT");
            database.execSQL("ALTER TABLE movimiento ADD COLUMN horaFin TEXT");
            database.execSQL("ALTER TABLE movimiento ADD COLUMN horaTotal TEXT");
            database.execSQL("ALTER TABLE movimiento ADD COLUMN propina REAL NOT NULL DEFAULT 0.0");
        }
    };

}
