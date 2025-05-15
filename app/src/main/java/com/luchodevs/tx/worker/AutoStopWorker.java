package com.luchodevs.tx.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;



import com.luchodevs.tx.dao.MovimientoDao;
import com.luchodevs.tx.database.AppDatabase;
import com.luchodevs.tx.database.DatabaseClient;
import com.luchodevs.tx.entity.Movimiento;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class AutoStopWorker extends Worker {

    public AutoStopWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            AppDatabase db = DatabaseClient.getInstance(getApplicationContext()).getAppDatabase();
            MovimientoDao dao = db.movimientosDao();

            String fechaActual = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
            Movimiento ultimo = dao.obtenerUltimoPorFecha(fechaActual);
            Movimiento inicioJornada = dao.obtenerPrimeroPorFecha(fechaActual, "Inicio Jornada");
            if (ultimo != null && ultimo.getHoraFin() == null && ultimo.getHora() != null) {
                String fecha = ultimo.getFecha();
                String hora = ultimo.getHora();

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                Date fechaHoraMovimiento = sdf.parse(fecha + " " + hora);
                Date ahora = new Date();

                long diferencia = ahora.getTime() - fechaHoraMovimiento.getTime();
                long horasPasadas = diferencia / (1000 * 60 * 60);

                if (horasPasadas >= 6) {
                    String horaFin = hora;
                    String tiempoTotal = formatElapsedTime(diferencia);

                    inicioJornada.setHoraFin(horaFin);
                    inicioJornada.setHoraTotal(tiempoTotal);
                    dao.update(inicioJornada);
                }
            }

            return Result.success();
        } catch (Exception e) {
            return Result.failure();
        }
    }

    private String formatElapsedTime(long millis) {
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / (1000 * 60)) % 60;
        long hours = (millis / (1000 * 60 * 60));
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }
}

