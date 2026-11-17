package dev.plume

import android.content.SharedPreferences
import org.json.JSONObject
import java.net.URLEncoder
import java.util.Locale

/** Synchronous weather/geocoding source. MainActivity retains its existing IO executor and UI callbacks. */
internal object WeatherDataSource {
    data class City(val name: String, val where: String, val lat: Float, val lon: Float)

    fun forecastUrl(lat: Float, lon: Float): String =
        "https://api.open-meteo.com/v1/forecast?latitude=$lat" +
            "&longitude=$lon&current=temperature_2m,weather_code,is_day"

    fun geocodeUrl(query: String, language: String): String =
        "https://geocoding-api.open-meteo.com/v1/search?count=8&format=json" +
            "&language=$language&name=${URLEncoder.encode(query, "UTF-8")}"

    fun refresh(prefs: SharedPreferences, getJson: (String) -> JSONObject): Boolean {
        if (!prefs.getBoolean("geo_manual", false) &&
            System.currentTimeMillis() - prefs.getLong("geo_time", 0) > 6 * 3600_000L) {
            val geo = getJson("https://ipwho.is/?fields=success,city,latitude,longitude")
            if (geo.optBoolean("success")) prefs.edit()
                .putString("geo_city", geo.getString("city"))
                .putFloat("geo_lat", geo.getDouble("latitude").toFloat())
                .putFloat("geo_lon", geo.getDouble("longitude").toFloat())
                .putLong("geo_time", System.currentTimeMillis()).apply()
        }
        if (!prefs.contains("geo_lat")) return false
        val now = getJson(forecastUrl(prefs.getFloat("geo_lat", 0f), prefs.getFloat("geo_lon", 0f)))
            .getJSONObject("current")
        prefs.edit()
            .putFloat("wx_temp", now.getDouble("temperature_2m").toFloat())
            .putInt("wx_code", now.getInt("weather_code"))
            .putBoolean("wx_day", now.optInt("is_day", 1) == 1)
            .putLong("wx_time", System.currentTimeMillis()).apply()
        return true
    }

    fun searchCities(query: String, getJson: (String) -> JSONObject): List<City> {
        val result = getJson(geocodeUrl(query, Locale.getDefault().language))
        val list = result.optJSONArray("results")
        return (0 until (list?.length() ?: 0)).map { i ->
            val city = list!!.getJSONObject(i)
            City(
                city.getString("name"),
                listOf(city.optString("admin1"), city.optString("country"))
                    .filter { it.isNotEmpty() }.joinToString(", "),
                city.getDouble("latitude").toFloat(),
                city.getDouble("longitude").toFloat(),
            )
        }
    }
}
