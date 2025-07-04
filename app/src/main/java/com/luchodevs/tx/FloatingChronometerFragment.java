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
import com.luchodevs.tx.database.DatabaseClient;
import com.luchodevs.tx.entity.Movimiento;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

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
            if (!isRunning) {
                return; // No hacer nada si no está corriendo
            }

            long elapsedMillis = System.currentTimeMillis() - startTime;

            int seconds = (int) (elapsedMillis / 1000) % 60;
            int minutes = (int) ((elapsedMillis / (1000 * 60)) % 60);
            int hours = (int) (elapsedMillis / (1000 * 60 * 60));

            String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);

            chronometerView.setText(timeFormatted);

            // Puedes controlar aquí si quieres detener el cronómetro automáticamente al pasar cierto tiempo
            // Por ejemplo, si quieres que pare al llegar a 24 horas:
            if (hours >= 24) {
                finalizarJornada();
                return;
            }

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

        cargarEstadoJornada();

        return view;
    }

    private void cargarEstadoJornada() {
        new Thread(() -> {
            try {
                MovimientoDao dao = DatabaseClient
                        .getInstance(requireContext())
                        .getAppDatabase()
                        .movimientosDao();

                Movimiento existente = dao.obtenerMovimientoConInicio(fechaSeleccionada);
                if (existente != null && existente.getHoraInicio() != null) {
                    Log.d("DEBUG_JORNADA", "fechaHoraCompleta guardada: " + existente.getFechaHoraCompleta());
                    Log.d("DEBUG_JORNADA", "horaInicio guardada: " + existente.getHoraInicio());
                    Log.d("DEBUG_JORNADA", "horaFin guardada: " + existente.getHoraFin());
                    Log.d("DEBUG_JORNADA", "horaTotal guardada: " + existente.getHoraTotal());
                    requireActivity().runOnUiThread(() -> {
                        initTime.setText("Hora Inicio: \n " + existente.getHoraInicio());

                        if (existente.getHoraFin() != null && existente.getHoraTotal() != null) {
                            // Jornada ya finalizada, mostrar datos
                            finishTime.setText("Hora Fin: \n " + existente.getHoraFin());
                            totalTime.setText("Total: \n" + existente.getHoraTotal());
                            chronometerView.setText("Finalizado");
                            isRunning = false;
                        } else {
                            // Jornada en curso, iniciar cronómetro calculando tiempo transcurrido
                            try {
                                // Parsear fechaHoraCompleta con zona horaria local
                                SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                dateTimeFormat.setTimeZone(TimeZone.getDefault()); // Zona local

                                Date inicioCompleto = dateTimeFormat.parse(existente.getFechaHoraCompleta());

                                // Obtener la hora actual también en zona local
                                Calendar now = Calendar.getInstance(TimeZone.getDefault());
                                long nowMillis = now.getTimeInMillis();
                                long inicioMillis = inicioCompleto.getTime();

                                Log.d("DEBUG_JORNADA", "Hora actual (ms): " + nowMillis + " (" + new Date(nowMillis).toString() + ")");
                                Log.d("DEBUG_JORNADA", "Hora inicio (ms): " + inicioMillis + " (" + inicioCompleto.toString() + ")");
                                long tiempoTranscurrido = nowMillis - inicioMillis;
                                Log.d("DEBUG_JORNADA", "Diferencia ms: " + tiempoTranscurrido);

                                if (tiempoTranscurrido < 0) {
                                    Log.w("DEBUG_JORNADA", "Tiempo transcurrido negativo, ajustando sumando 1 día");
                                    tiempoTranscurrido += 24 * 60 * 60 * 1000;
                                }

                                startTime = System.currentTimeMillis() - tiempoTranscurrido;
                                handler.post(runnable);
                                isRunning = true;
                                chronometerView.setText("En curso...");
                            } catch (Exception e) {
                                Log.e("PARSING_TIME", "Error al calcular tiempo transcurrido", e);
                            }


                        }
                    });
                } else {
                    requireActivity().runOnUiThread(() -> {
                        // No hay jornada iniciada
                        initTime.setText("Hora Inicio: \n 00:00:00");
                        finishTime.setText("Hora Fin: \n 00:00:00");
                        totalTime.setText("Total: \n 00:00:00");
                        chronometerView.setText("No iniciado");
                        isRunning = false;
                    });
                }
            } catch (Exception e) {
                Log.e("DB_ERROR", "Error al cargar datos de jornada", e);
            }
        }).start();
    }

    private void reiniciarJornadas() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Reiniciar jornadas")
                .setMessage("¿Estás seguro de que quieres reiniciar esta jornada?")
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
                    chronometerView.setText("No iniciado");
                    Toast.makeText(requireContext(), "Jornadas reiniciadas correctamente", Toast.LENGTH_SHORT).show();
                    isRunning = false;
                    handler.removeCallbacks(runnable);
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

                String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                String fechaHoraCompleta = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                Movimiento existente = dao.obtenerMovimientoConInicio(fechaSeleccionada);

                if (existente != null) {
                    if (existente.getHoraInicio() != null && existente.getFechaHoraCompleta() != null) {
                        // Ya hay jornada iniciada correctamente
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Ya has iniciado una jornada hoy", Toast.LENGTH_SHORT).show()
                        );
                        return;
                    } else {
                        // Jornada incompleta, actualizar registro existente
                        existente.setHoraInicio(currentTime);
                        existente.setFechaHoraCompleta(fechaHoraCompleta);
                        dao.update(existente);
                    }
                } else {
                    // No existe jornada, crear nueva
                    Movimiento movimiento = new Movimiento();
                    movimiento.setFecha(fechaSeleccionada);
                    movimiento.setHoraInicio(currentTime);
                    movimiento.setValor(0.00);
                    movimiento.setTipo("Inicio Jornada");
                    movimiento.setFechaHoraCompleta(fechaHoraCompleta);
                    dao.insert(movimiento);
                }

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

                String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                String fechaHoraCompleta = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                Movimiento movimiento = dao.obtenerMovimientoConInicio(fechaSeleccionada);

                if (movimiento != null) {
                    movimiento.setHoraFin(currentTime);
                    movimiento.setFechaHoraCompleta(fechaHoraCompleta);

                    String tiempoTotal = chronometerView.getText().toString();
                    movimiento.setHoraTotal(tiempoTotal);

                    dao.update(movimiento);

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

    private String formatElapsedTime(long millis) {
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / (1000 * 60)) % 60;
        long hours = (millis / (1000 * 60 * 60));
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }
}
