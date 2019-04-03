package me.mauricee.pontoon.common.moshi

import com.squareup.moshi.*
import java.io.IOException
import java.lang.reflect.Type
import java.util.Collections.emptyList

internal class ListJsonAdapter(private val delegate: JsonAdapter<List<*>>) : JsonAdapter<List<*>>() {

    @Throws(IOException::class)
    override fun fromJson(reader: JsonReader): List<*>? {
        if (reader.peek() == JsonReader.Token.NULL) {
            reader.skipValue()
            return emptyList<Any>()
        }
        return delegate.fromJson(reader)
    }

    @Throws(IOException::class)
    override fun toJson(writer: JsonWriter, value: List<*>?) {
        if (value == null) {
            throw IllegalStateException("Wrap JsonAdapter with .nullSafe().")
        }
        delegate.toJson(writer, value)
    }

    companion object {
        val Factory: JsonAdapter.Factory = object : JsonAdapter.Factory {
            override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
                if (!annotations.isEmpty()) {
                    return null
                }
                if (Types.getRawType(type) != List::class.java) {
                    return null
                }
                val objectJsonAdapter = moshi.nextAdapter<List<*>>(this, type, annotations)
                return ListJsonAdapter(objectJsonAdapter)
            }
        }
    }
}
