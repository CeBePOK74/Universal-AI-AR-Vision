package com.atlantiksvisionfinal

import kotlin.math.ceil

class EstimateCalculator {

    // Считаем чистую площадь
    fun calculateArea(width: Float, height: Float): Float {
        return width * height
    }

    // Считаем количество плиток с запасом 10% на подрезку
    fun calculateTiles(area: Float, tileWidth: Float, tileHeight: Float): Int {
        val tileArea = (tileWidth / 1000) * (tileHeight / 1000) // мм в метры
        val count = area / tileArea
        return ceil(count * 1.10).toInt() // Округляем до целой плитки вверх
    }

    // Считаем итоговую стоимость (Площадь * (Работа + Материал))
    fun estimatePrice(area: Float, workPrice: Double, materialPrice: Double): Double {
        return area * (workPrice + materialPrice)
    }
}