package com.luchodevs.tx;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.luchodevs.tx.dao.MovimientoDao;
import com.luchodevs.tx.database.AppDatabase;
import com.luchodevs.tx.database.DatabaseClient;
import com.luchodevs.tx.entity.Movimiento;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FloatingChronometerFragment extends DialogFragment {

    private TextView chronometerView, initTime, finishTime, totalTime;
    private Button btnStart, btnStop, reset;
    private String fechaSeleccionada;

    private Handler handler = new Handler();
    private long startTime;
    private boolean isRunning = false;

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            long elapsedTime = System.currentTimeMillis() - startTime;
            chronometerView.setText(formatElapsedTime(elapsedTime));
            verificarFinalizacionDesdeWorker();
            handler.postDelayed(this, 1000);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_floating_chronometer, container, false);

        if (getArguments() != null) {
            fechaSeleccionada = getArguments().getString("fechaSeleccionada");
        }

        chronometerView = view.findViewById(R.id.chronometerView);
        initTime = view.findViewById(R.id.initTime);
        finishTime = view.findViewById(R.id.finishTime);
        totalTime = view.findViewById(R.id.totaltime);

        reset = view.findViewById(R.id.reset);
        btnStart = view.findViewById(R.id.btnStart);
        btnStop = view.findViewById(R.id.btnStop);

        reset.setOnClickListener(v -> reiniciarJornadas());
        btnStart.setOnClickListener(v -> iniciarJornada());
        btnStop.setOnClickListener(v -> finalizarJornada());


        new Thread(() -> {
            try {
                MovimientoDao dao = DatabaseClient
                        .getInstance(requireContext())
                        .getAppDatabase()
                        .movimientosDao();

                String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

                Movimiento existente = dao.obtenerMovimientoConInicio(fechaSeleccionada);
                if (existente != null && existente.getHoraInicio() != null) {
                    requireActivity().runOnUiThread(() -> {
                        initTime.setText("Hora Inicio: \n " + existente.getHoraInicio());

                        if (existente.getHoraFin() != null && existente.getHoraTotal() != null) {
                            // Jornada ya finalizada, mostrar datos
                            finishTime.setText("Hora Fin: \n " + existente.getHoraFin());
                            totalTime.setText("Total: \n" + existente.getHoraTotal());
                            chronometerView.setText("Finalizado");
                        } else {
                            // Jornada en curso, iniciar cronómetro
                            try {
                                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                                Date horaInicioTime = timeFormat.parse(existente.getHoraInicio());

                                Calendar now = Calendar.getInstance();
                                Calendar inicioCal = Calendar.getInstance();
                                inicioCal.setTime(horaInicioTime);
                                inicioCal.set(Calendar.YEAR, now.get(Calendar.YEAR));
                                inicioCal.set(Calendar.MONTH, now.get(Calendar.MONTH));
                                inicioCal.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));

// Si el tiempo de inicio está en el futuro (ej: ayer a las 23:00 y ahora son las 00:30)
                                if (inicioCal.after(now)) {
                                    inicioCal.add(Calendar.DAY_OF_MONTH, -1);
                                }

                                long tiempoTranscurrido = now.getTimeInMillis() - inicioCal.getTimeInMillis();


                                startTime = System.currentTimeMillis() - tiempoTranscurrido;
                                handler.post(runnable);
                                isRunning = true;
                                chronometerView.setText("En curso...");
                            } catch (Exception e) {
                                Log.e("PARSING_TIME", "Error al calcular tiempo transcurrido", e);
                            }
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("DB_ERROR", "Error al cargar datos de jornada", e);
            }
        }).start();


        return view;
    }

    private void reiniciarJornadas() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Reiniciar jornadas")
                .setMessage("¿Estás seguro de que quieres Reiniciar esta Jornada?")
                .setPositiveButton("Sí", (dialog, which) -> ejecutarReinicio())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void ejecutarReinicio() {
        new Thread(() -> {
            try {
                MovimientoDao dao = DatabaseClient
                        .getInstance(requireContext())
                        .getAppDatabase()
                        .movimientosDao();

                dao.eliminarMovimientosConValorCero(fechaSeleccionada);

                requireActivity().runOnUiThread(() -> {
                    initTime.setText("Hora Inicio: \n 00:00:00");
                    finishTime.setText("Hora Fin: \n 00:00:00");
                    totalTime.setText("Total: \n 00:00:00");
                    Toast.makeText(requireContext(), "Jornadas reiniciadas correctamente", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                Log.e("DB_ERROR", "Error al reiniciar jornadas", e);
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Error al reiniciar: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }



    private void iniciarJornada() {

        if (isRunning) {
            Toast.makeText(requireContext(), "Ya estás registrando una jornada", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                MovimientoDao dao = DatabaseClient
                        .getInstance(requireContext())
                        .getAppDatabase()
                        .movimientosDao();

                String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
                String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                String fechaHoraCompleta = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                // Verificar si ya hay jornada iniciada
                Movimiento existente = dao.obtenerMovimientoConInicio(fechaSeleccionada);
                if (existente != null && existente.getHoraInicio() != null) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Ya has iniciado una jornada hoy", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                Movimiento movimiento = new Movimiento();
                movimiento.setFecha(fechaSeleccionada);
                movimiento.setHoraInicio(currentTime);
                movimiento.setValor(0.00);
                movimiento.setTipo("Inicio Jornada");
                movimiento.setFechaHoraCompleta(fechaHoraCompleta);
                dao.insert(movimiento);

                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Jornada iniciada", Toast.LENGTH_SHORT).show();
                    initTime.setText("Hora Inicio: \n " + currentTime);
                    chronometerView.setText("En curso...");

                    startTime = System.currentTimeMillis();
                    handler.post(runnable);
                    isRunning = true;
                });


            } catch (Exception e) {
                Log.e("DB_ERROR", "Error al iniciar jornada", e);
            }
        }).start();
    }

    private void finalizarJornada() {
        if (!isRunning) {
            Toast.makeText(requireContext(), "Aún no has iniciado la jornada", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                MovimientoDao dao = DatabaseClient
                        .getInstance(requireContext())
                        .getAppDatabase()
                        .movimientosDao();

                String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
                String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                String fechaHoraCompleta = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                // Obtener el movimiento con tipo = "Inicio Jornada"
                Movimiento movimiento = dao.obtenerMovimientoConInicio(fechaSeleccionada);

                if (movimiento != null) {
                    movimiento.setHoraFin(currentTime);
                    movimiento.setFechaHoraCompleta(fechaHoraCompleta);

                    String tiempoTotal = chronometerView.getText().toString();
                    movimiento.setHoraTotal(tiempoTotal);

                    dao.update(movimiento);  // Actualizar en lugar de insertar

                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Jornada finalizada", Toast.LENGTH_SHORT).show();
                        finishTime.setText("Hora Fin: \n " + currentTime);
                        totalTime.setText("Total: \n" + tiempoTotal);
                        chronometerView.setText("Finalizado");

                        handler.removeCallbacks(runnable);
                        isRunning = false;
                    });
                } else {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "No se encontró la jornada para finalizar", Toast.LENGTH_SHORT).show()
                    );
                }

            } catch (Exception e) {
                Log.e("DB_ERROR", "Error al finalizar jornada", e);
            }
        }).start();
    }

    private void verificarFinalizacionDesdeWorker() {
        new Thread(() -> {
            try {
                MovimientoDao dao = DatabaseClient
                        .getInstance(requireContext())
                        .getAppDatabase()
                        .movimientosDao();

                Movimiento movimiento = dao.obtenerMovimientoConInicio(fechaSeleccionada);
                List<Movimiento> list = dao.getall();


                for (Movimiento m : list) {
                    Log.d("BD_MOVIMIENTO", m.toString());
                }

                if (movimiento != null && movimiento.getHoraFin() != null) {
                    // Ya fue finalizado por el Worker
                    requireActivity().runOnUiThread(() -> {
                        handler.removeCallbacks(runnable);
                        isRunning = false;

                        finishTime.setText("Hora Fin: \n " + movimiento.getHoraFin());
                        totalTime.setText("Total: \n" + movimiento.getHoraTotal());
                        chronometerView.setText("Finalizado");

                        Toast.makeText(requireContext(), "La jornada fue finalizada automáticamente", Toast.LENGTH_LONG).show();
                    });
                }
            } catch (Exception e) {
                Log.e("CRONOMETRO_CHECK", "Error al verificar finalización en la base de datos", e);
            }
        }).start();

    }




    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(runnable);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        }
    }

    private String getCurrentTimeString() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    private String formatElapsedTime(long millis) {
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / (1000 * 60)) % 60;
        long hours = (millis / (1000 * 60 * 60));
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }
}
