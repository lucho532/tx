package com.luchodevs.tx.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class DatabaseClient {

    private Context mCtx;
    private static DatabaseClient mInstance;

    // Base de datos
    private AppDatabase appDatabase;

    private DatabaseClient(Context mCtx) {
        this.mCtx = mCtx;

        // Crear base de datos con Room
        appDatabase = Room.databaseBuilder(mCtx, AppDatabase.class, "mi_base_datos")
                .addMigrations(MIGRATION_4_5)
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




    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Agregar columna tipoNombre (nombre visible del tipo)
            database.execSQL("ALTER TABLE movimiento ADD COLUMN tipoNombre TEXT");

            // Agregar columna metodoNombre (nombre visible del método de pago)
            database.execSQL("ALTER TABLE movimiento ADD COLUMN metodoNombre TEXT");
        }
    };


    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE movimiento ADD COLUMN fechaHoraCompleta TEXT");

        }
    };

}
