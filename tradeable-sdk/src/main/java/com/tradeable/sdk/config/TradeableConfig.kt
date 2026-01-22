package com.tradeable.sdk.config

/**
 * Credentials required for Tradeable SDK authentication
 */
data class TradeableCredentials(
    val authorization: String,
    val portalToken: String,
    val appId: String,
    val clientId: String,
    val publicKey: String
)

/**
 * Analytics event from Flutter SDK
 */
data class TradeableAnalyticsEvent(
    val eventName: String,
    val data: Map<String, Any?>? = null
)

/**
 * Callback event from Flutter views
 */
data class TradeableCallbackEvent(
    val action: String,
    val route: String? = null,
    val parameters: Map<String, Any?>? = null
)

/**
 * Configuration for Tradeable SDK initialization
 */
data class TradeableConfig(
    /**
     * Base URL for the Tradeable backend
     */
    val baseUrl: String,
    
    /**
     * Callback to refresh credentials when token expires
     * This will be called by the SDK when authentication is needed
     */
    val onRefreshCredentials: suspend () -> TradeableCredentials,
    
    /**
     * Optional callback for analytics events from Flutter
     */
    val onAnalyticsEvent: ((TradeableAnalyticsEvent) -> Unit)? = null,
    
    /**
     * Optional callback for when Flutter view triggers navigation or action
     */
    val onCallback: ((TradeableCallbackEvent) -> Unit)? = null,
    
    /**
     * Enable debug logging
     */
    val debugMode: Boolean = false
)

/**
 * Parameters for embedded Flutter card views
 */
data class TradeableCardParams(
    val type: String,
    val parameters: Map<String, String> = emptyMap()
)

/**
 * Parameters for full-page Flutter views
 */
data class TradeablePageParams(
    val route: String,
    val parameters: Map<String, String> = emptyMap(),
    val title: String? = null
)
