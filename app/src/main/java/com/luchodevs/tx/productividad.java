package com.luchodevs.tx;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.luchodevs.tx.database.DatabaseClient;
import com.luchodevs.tx.entity.Movimiento;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class productividad extends DialogFragment {
    private BarChart barChart;
    private String fechaSeleccionada; // fecha que viene desde el bundle

    @SuppressLint("WrongViewCast")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_productividad, container, false);
        barChart = view.findViewById(R.id.barChart);

        if (getArguments() != null) {
            fechaSeleccionada = getArguments().getString("fechaSeleccionada", "");
        }

        TextView tvFechaGrafico = view.findViewById(R.id.tvFechaGrafico);
        tvFechaGrafico.setText("Fecha: " + fechaSeleccionada);

        cargarYMostrarDatos();
        return view;
    }


    private void cargarYMostrarDatos() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Movimiento> movimientos = DatabaseClient
                    .getInstance(requireContext())
                    .getAppDatabase()
                    .movimientosDao()
                    .getMovimientosByFecha(fechaSeleccionada); // Asegúrate de tener este método implementado

            requireActivity().runOnUiThread(() -> mostrarGraficoProduccionPorHora(movimientos));
        });
    }

    private void mostrarGraficoProduccionPorHora(List<Movimiento> movimientos) {
        int[] produccionPorHora = new int[24];

        for (Movimiento mov : movimientos) {
            if (mov.getHora() == null) continue;

            try {
                int hora = Integer.parseInt(mov.getHora().split(":")[0]);
                produccionPorHora[hora] += mov.getValor();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> horasLabels = new ArrayList<>();

        for (int i = 0; i < produccionPorHora.length; i++) {
            if (produccionPorHora[i] > 0) {
                entries.add(new BarEntry(entries.size(), produccionPorHora[i]));
                horasLabels.add(i + "h");
            }
        }

        if (entries.isEmpty()) {
            barChart.clear();
            barChart.setNoDataText("No hay datos para mostrar.");
            return;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Producción por hora");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        BarData data = new BarData(dataSet);
        barChart.setData(data);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(horasLabels));
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45);
        xAxis.setDrawGridLines(false);

        barChart.getAxisRight().setEnabled(false);
        barChart.getDescription().setText("Producido por hora");
        barChart.invalidate(); // Refresca el gráfico
    }


    private List<String> getHoras() {
        List<String> horas = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            horas.add(i + "h");
        }
        return horas;
    }
}
