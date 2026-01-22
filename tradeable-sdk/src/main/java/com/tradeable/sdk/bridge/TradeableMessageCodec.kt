package com.tradeable.sdk.bridge

import org.json.JSONArray
import org.json.JSONObject

/**
 * Message codec for encoding/decoding messages between Android and Flutter
 */
object TradeableMessageCodec {
    
    /**
     * Encode a map to JSON string
     */
    fun encode(data: Map<String, Any?>): String {
        return JSONObject(data).toString()
    }
    
    /**
     * Decode JSON string to map
     */
    fun decode(json: String): Map<String, Any?> {
        return jsonObjectToMap(JSONObject(json))
    }
    
    /**
     * Create card view message
     */
    fun createCardMessage(
        type: String,
        parameters: Map<String, String>
    ): String {
        return encode(mapOf(
            "action" to "renderCard",
            "type" to type,
            "parameters" to parameters
        ))
    }
    
    /**
     * Create page navigation message
     */
    fun createPageMessage(
        route: String,
        parameters: Map<String, String>
    ): String {
        return encode(mapOf(
            "action" to "navigate",
            "route" to route,
            "parameters" to parameters
        ))
    }
    
    /**
     * Parse callback message from Flutter
     */
    fun parseCallback(json: String): CallbackMessage? {
        return try {
            val obj = JSONObject(json)
            CallbackMessage(
                action = obj.optString("action"),
                data = obj.optJSONObject("data")?.let { jsonObjectToMap(it) }
            )
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Parse analytics event from Flutter
     */
    fun parseAnalyticsEvent(json: String): AnalyticsMessage? {
        return try {
            val obj = JSONObject(json)
            AnalyticsMessage(
                eventName = obj.optString("eventName"),
                data = obj.optJSONObject("data")?.let { jsonObjectToMap(it) }
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private fun jsonObjectToMap(jsonObject: JSONObject): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        val keys = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = jsonObject.get(key)
            map[key] = when (value) {
                is JSONObject -> jsonObjectToMap(value)
                is JSONArray -> jsonArrayToList(value)
                JSONObject.NULL -> null
                else -> value
            }
        }
        return map
    }
    
    private fun jsonArrayToList(jsonArray: JSONArray): List<Any?> {
        val list = mutableListOf<Any?>()
        for (i in 0 until jsonArray.length()) {
            val value = jsonArray.get(i)
            list.add(when (value) {
                is JSONObject -> jsonObjectToMap(value)
                is JSONArray -> jsonArrayToList(value)
                JSONObject.NULL -> null
                else -> value
            })
        }
        return list
    }
    
    data class CallbackMessage(
        val action: String,
        val data: Map<String, Any?>?
    )
    
    data class AnalyticsMessage(
        val eventName: String,
        val data: Map<String, Any?>?
    )
}
