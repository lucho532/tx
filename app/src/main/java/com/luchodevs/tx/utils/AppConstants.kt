package com.luchodevs.tx.utils



object AppConstants {
    val SERVICIOS = listOf("Tipo1", "Tipo2", "Tipo3", "Tipo4", "Tipo5")
    val METODOS_PAGO = listOf("Metodo1", "Metodo2", "Metodo3", "Metodo4")


    val NOMBRES_SERVICIOS_DEFAULT = mapOf(
        "Tipo1" to "Taxi",
        "Tipo2" to "Emisora",
        "Tipo3" to "Uber",
        "Tipo4" to "Bolt",
        "Tipo5" to "Cabify"
    )

    val NOMBRES_METODOS_DEFAULT = mapOf(
        "Metodo1" to "Tarjeta",
        "Metodo2" to "Efectivo",
        "Metodo3" to "App",
        "Metodo4" to "Otro"
    )

}
