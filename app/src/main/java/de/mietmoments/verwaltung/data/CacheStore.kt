package de.mietmoments.verwaltung.data

import android.content.Context
import android.util.AtomicFile
import java.io.File

class CacheStore(context: Context) {
    private val dir = File(context.filesDir, "cache_v2").apply { mkdirs() }

    fun readSnapshot(): String? = read("snapshot.json")
    fun writeSnapshot(json: String) = write("snapshot.json", json)
    fun readCustomer(customerId: Int, orderId: Int): String? = read("customer_${customerId}_${orderId}.json")
    fun writeCustomer(customerId: Int, orderId: Int, json: String) = write("customer_${customerId}_${orderId}.json", json)

    private fun read(name: String): String? = runCatching {
        val f = File(dir, name)
        if (!f.exists()) null else f.readText(Charsets.UTF_8)
    }.getOrNull()

    private fun write(name: String, content: String) {
        runCatching {
            val atomic = AtomicFile(File(dir, name))
            val out = atomic.startWrite()
            try {
                out.write(content.toByteArray(Charsets.UTF_8))
                atomic.finishWrite(out)
            } catch (t: Throwable) {
                atomic.failWrite(out)
                throw t
            }
        }
    }
}
