package com.luchodevs.tx.generador

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.luchodevs.tx.entity.Movimiento
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

fun generarArchivoMovimientos(context: Context, movimientos: List<Movimiento>, fecha: String) {
    if (movimientos.isNotEmpty()) {
        // Crear el libro Excel
        val workbook: Workbook = XSSFWorkbook()
        val sheet: Sheet = workbook.createSheet("Movimientos")

        // Crear la fila de encabezados
        val headerRow: Row = sheet.createRow(0)
        val headerCell0 = headerRow.createCell(0)
        headerCell0.setCellValue("Fecha")
        val headerCell1 = headerRow.createCell(1)
        headerCell1.setCellValue("Valor")
        val headerCell2 = headerRow.createCell(2)
        headerCell2.setCellValue("Método de Pago")
        val headerCell3 = headerRow.createCell(3)
        headerCell3.setCellValue("Hora")

        // Añadir los movimientos a las filas del Excel
        for (i in movimientos.indices) {
            val movimiento = movimientos[i]
            val row: Row = sheet.createRow(i + 1) // Comienza en la fila 1 (después del encabezado)

            // Agregar datos del movimiento
            val cell0 = row.createCell(0)
            cell0.setCellValue(movimiento.fecha)
            val cell1 = row.createCell(1)
            cell1.setCellValue(movimiento.valor.toString())
            val cell2 = row.createCell(2)
            cell2.setCellValue(movimiento.metodoDePago)
            val cell3 = row.createCell(3)
            cell3.setCellValue(movimiento.hora)
        }

        // Guardar el archivo Excel
        val fechaLimpia = fecha.replace("/", "-")
        val file = File(context.getExternalFilesDir(null), "movimientos_$fechaLimpia.xlsx")
        try {
            val fileOut = FileOutputStream(file)
            workbook.write(fileOut)
            fileOut.close()
            workbook.close()

            // Abrir el archivo Excel
            abrirArchivoExcel(context, file)
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Error al generar el archivo Excel.", Toast.LENGTH_SHORT).show()
        }
    } else {
        Toast.makeText(context, "No hay movimientos para esta fecha.", Toast.LENGTH_SHORT).show()
    }
}

fun abrirArchivoExcel(context: Context, archivo: File) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", archivo)

    val intent = Intent(Intent.ACTION_VIEW)
    intent.setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

    // ❌ No uses try-catch aquí
    context.startActivity(intent) // Deja que lance ActivityNotFoundException si no hay app

}
