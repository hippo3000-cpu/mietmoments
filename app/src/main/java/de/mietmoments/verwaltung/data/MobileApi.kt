package de.mietmoments.verwaltung.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class MobileApi {
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
        coerceInputValues = true
    }

    suspend fun pair(serverUrl: String, code: String): String = withContext(Dispatchers.IO) {
        require(serverUrl.startsWith("https://")) { "Nur HTTPS-Server sind erlaubt." }
        val connection = (URL(serverUrl + "mobile_api.php?action=pair").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 12_000
            readTimeout = 25_000
            doOutput = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
            setRequestProperty("User-Agent", "MietMoments-Android/2")
        }

        return@withContext try {
            val body = "code=" + enc(code)
            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { it.write(body) }

            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val response = BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }
            val parsed = runCatching { json.decodeFromString<PairResponse>(response) }.getOrElse {
                error("Die Kopplungsantwort des Servers ist ungültig.")
            }

            if (status !in 200..299 || !parsed.ok || parsed.token.isBlank()) {
                error(parsed.message.ifBlank {
                    if (status == 401) "Kopplungscode ist ungültig oder abgelaufen."
                    else "Kopplung fehlgeschlagen (Serverfehler $status)."
                })
            }
            parsed.token
        } finally {
            connection.disconnect()
        }
    }

    suspend fun health(settings: AppSettings): Boolean = withContext(Dispatchers.IO) {
        val text = request(settings, "mobile_api.php?action=health")
        text.contains("\"ok\":true")
    }

    suspend fun snapshot(settings: AppSettings, from: String, to: String): Pair<SnapshotResponse, String> = withContext(Dispatchers.IO) {
        val path = "mobile_api.php?action=snapshot&from=${enc(from)}&to=${enc(to)}"
        val raw = request(settings, path)
        val parsed = json.decodeFromString<SnapshotResponse>(raw)
        if (!parsed.ok) error(parsed.message.ifBlank { "Daten konnten nicht geladen werden." })
        parsed to raw
    }

    suspend fun customer(settings: AppSettings, customerId: Int, orderId: Int): Pair<CustomerDetailResponse, String> = withContext(Dispatchers.IO) {
        val path = buildString {
            append("mobile_api.php?action=customer&customer_id=").append(customerId)
            if (orderId > 0) append("&order_id=").append(orderId)
        }
        val raw = request(settings, path)
        val parsed = json.decodeFromString<CustomerDetailResponse>(raw)
        if (!parsed.ok) error(parsed.message.ifBlank { "Kundendaten konnten nicht geladen werden." })
        parsed to raw
    }

    suspend fun saveBooking(settings: AppSettings, payload: SaveBookingRequest): SaveResponse = withContext(Dispatchers.IO) {
        val body = json.encodeToString(SaveBookingRequest.serializer(), payload)
        parseSaveResponse(postJson(settings, "mobile_api.php?action=save", body))
    }

    suspend fun saveLocation(settings: AppSettings, payload: SaveLocationRequest): SaveResponse = withContext(Dispatchers.IO) {
        val body = json.encodeToString(SaveLocationRequest.serializer(), payload)
        parseSaveResponse(postJson(settings, "mobile_api.php?action=save", body))
    }

    suspend fun saveItem(settings: AppSettings, payload: SaveItemRequest): SaveResponse = withContext(Dispatchers.IO) {
        val body = json.encodeToString(SaveItemRequest.serializer(), payload)
        parseSaveResponse(postJson(settings, "mobile_api.php?action=save", body))
    }

    private fun parseSaveResponse(raw: String): SaveResponse {
        val parsed = runCatching { json.decodeFromString<SaveResponse>(raw) }.getOrElse {
            error("Die Speicherantwort des Servers ist ungültig.")
        }
        if (!parsed.ok) error(parsed.message.ifBlank { "Änderungen konnten nicht gespeichert werden." })
        return parsed
    }

    private fun postJson(settings: AppSettings, relativePath: String, body: String): String {
        require(settings.serverUrl.startsWith("https://")) { "Nur HTTPS-Server sind erlaubt." }
        val connection = (URL(settings.serverUrl + relativePath).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 12_000
            readTimeout = 30_000
            doOutput = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("X-MM-Mobile-Token", settings.token)
            setRequestProperty("User-Agent", "MietMoments-Android/2")
        }
        return try {
            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { it.write(body) }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val response = BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }
            if (code !in 200..299) {
                val message = runCatching { json.decodeFromString<SaveResponse>(response).message }.getOrNull()
                error(message?.takeIf { it.isNotBlank() }
                    ?: if (code == 401) "App-Verbindung ist nicht mehr gültig." else "Serverfehler $code")
            }
            response
        } finally {
            connection.disconnect()
        }
    }

    private fun request(settings: AppSettings, relativePath: String): String {
        require(settings.serverUrl.startsWith("https://")) { "Nur HTTPS-Server sind erlaubt." }
        val connection = (URL(settings.serverUrl + relativePath).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 12_000
            readTimeout = 25_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("X-MM-Mobile-Token", settings.token)
            setRequestProperty("User-Agent", "MietMoments-Android/2")
        }
        return try {
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val body = BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }
            if (code !in 200..299) {
                val message = runCatching { json.parseToJsonElement(body).toString() }.getOrNull()
                error(if (code == 401) "App-Verbindung ist nicht mehr gültig." else "Serverfehler $code${message?.let { ": $it" }.orEmpty()}")
            }
            body
        } finally {
            connection.disconnect()
        }
    }

    private fun enc(value: String): String = URLEncoder.encode(value, Charsets.UTF_8.name())
}
