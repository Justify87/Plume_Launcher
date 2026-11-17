package dev.plume

import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherDataSourceTest {
    @Test fun forecastKeepsExistingUnitsAndFields() {
        assertEquals(
            "https://api.open-meteo.com/v1/forecast?latitude=51.3&longitude=12.4&current=temperature_2m,weather_code,is_day",
            WeatherDataSource.forecastUrl(51.3f, 12.4f),
        )
    }

    @Test fun geocodingKeepsLanguageLimitAndFormEncoding() {
        assertEquals(
            "https://geocoding-api.open-meteo.com/v1/search?count=8&format=json&language=de&name=Leipzig+S%C3%BCd",
            WeatherDataSource.geocodeUrl("Leipzig Süd", "de"),
        )
    }
}
