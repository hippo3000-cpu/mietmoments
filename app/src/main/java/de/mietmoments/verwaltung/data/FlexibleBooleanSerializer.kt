package de.mietmoments.verwaltung.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull

object FlexibleBooleanSerializer : KSerializer<FlexibleBoolean> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("FlexibleBoolean", PrimitiveKind.BOOLEAN)

    override fun deserialize(decoder: Decoder): FlexibleBoolean {
        if (decoder is JsonDecoder) {
            val element = decoder.decodeJsonElement()
            val primitive = element as? JsonPrimitive
            val value = primitive?.booleanOrNull
                ?: primitive?.intOrNull?.let { it != 0 }
                ?: primitive?.content?.trim()?.lowercase()?.let { it in setOf("1", "true", "yes", "ja") }
                ?: false
            return FlexibleBoolean(value)
        }
        return FlexibleBoolean(decoder.decodeBoolean())
    }

    override fun serialize(encoder: Encoder, value: FlexibleBoolean) = encoder.encodeBoolean(value.value)
}
