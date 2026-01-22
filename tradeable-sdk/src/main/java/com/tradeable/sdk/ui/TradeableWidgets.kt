package com.tradeable.sdk.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tradeable.sdk.config.TradeableCallbackEvent

/**
 * Tradeable Dashboard widget that can be embedded in your Compose UI.
 * 
 * This displays the main Tradeable Learn dashboard/home widget.
 * 
 * ```kotlin
 * TradeableDashboard(
 *     modifier = Modifier.fillMaxWidth(),
 *     onCallback = { event ->
 *         // Handle dashboard interactions
 *     }
 * )
 * ```
 * 
 * @param modifier Compose modifier
 * @param height Height of the dashboard widget
 * @param dateThreshold Number of days to filter courses (default: all)
 * @param onCallback Callback for dashboard interactions
 */
@Composable
fun TradeableDashboard(
    modifier: Modifier = Modifier,
    height: Dp = 300.dp,
    dateThreshold: Int? = null,
    onCallback: ((TradeableCallbackEvent) -> Unit)? = null
) {
    val data = buildMap<String, Any> {
        put("type", "dashboard")
        dateThreshold?.let { put("dateThreshold", it) }
    }
    
    TradeableFlutterView(
        mode = DisplayMode.DIRECT,
        height = height,
        width = 400.dp,
        data = data,
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Tradeable Course Card widget
 */
@Composable
fun TradeableCourseCard(
    courseId: String,
    modifier: Modifier = Modifier,
    height: Dp = 120.dp,
    width: Dp = 200.dp,
    onCallback: ((TradeableCallbackEvent) -> Unit)? = null
) {
    TradeableFlutterView(
        mode = DisplayMode.DIRECT,
        height = height,
        width = width,
        data = mapOf("type" to "course_card", "courseId" to courseId),
        modifier = modifier
    )
}

/**
 * Tradeable Learn Container that wraps content and shows the learn sheet
 */
@Composable
fun TradeableLearnSheet(
    pageId: String? = null,
    modifier: Modifier = Modifier,
    height: Dp = 200.dp,
    onCallback: ((TradeableCallbackEvent) -> Unit)? = null
) {
    val data = buildMap<String, Any> {
        put("type", "learn_sheet")
        pageId?.let { put("pageId", it) }
    }
    
    TradeableFlutterView(
        mode = DisplayMode.DIRECT,
        height = height,
        width = 400.dp,
        data = data,
        modifier = modifier.fillMaxWidth()
    )
}
