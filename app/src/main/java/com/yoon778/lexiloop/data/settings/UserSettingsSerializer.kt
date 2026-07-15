package com.yoon778.lexiloop.data.settings

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import java.io.InputStream
import java.io.OutputStream
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class UserSettingsSerializer(
    private val json: Json = strictSettingsJson,
) : Serializer<UserSettings> {
    override val defaultValue: UserSettings = UserSettings()

    override suspend fun readFrom(input: InputStream): UserSettings = try {
        json.decodeFromString<UserSettings>(input.readBytes().decodeToString()).validated()
    } catch (error: SerializationException) {
        throw CorruptionException("Cannot read settings", error)
    } catch (error: IllegalArgumentException) {
        throw CorruptionException("Invalid settings", error)
    }

    override suspend fun writeTo(t: UserSettings, output: OutputStream) {
        output.write(json.encodeToString(t.validated()).encodeToByteArray())
    }
}

val strictSettingsJson = Json {
    ignoreUnknownKeys = false
    explicitNulls = false
    encodeDefaults = true
}
