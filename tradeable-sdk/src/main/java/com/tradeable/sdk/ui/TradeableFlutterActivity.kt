package com.tradeable.sdk.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.delay
import com.tradeable.sdk.android.wrapper.FlutterBridge
import androidx.activity.compose.BackHandler

/**
 * Activity that displays a full-page Flutter view.
 * Matches iOS TradeableFlutterView pattern with mode, data, and topicId
 */
class TradeableFlutterActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "TradeableFlutterActivity"
        const val MAX_INIT_RETRIES = 50
        const val INIT_RETRY_DELAY_MS = 100L
    }
    
    private var flutterView: io.flutter.embedding.android.FlutterView? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Extract intent extras (matching iOS pattern)
        val mode = intent.getStringExtra("mode") ?: "fullscreen"
        val text = intent.getStringExtra("text") ?: "Open Fullscreen"
        val topicId = intent.getIntExtra("topicId", 0)
        val width = intent.getDoubleExtra("width", 0.0)
        val height = intent.getDoubleExtra("height", 0.0)
        
        Log.d(TAG, "Opening Flutter: mode=$mode, topicId=$topicId, width=$width, height=$height")
        
        setContent {
            MaterialTheme {
                TradeableFullPageContent(
                    mode = mode,
                    text = text,
                    topicId = topicId,
                    width = width,
                    height = height,
                    onBack = { finish() },
                    onViewCreated = { view -> flutterView = view }
                )
            }
        }
    }
    
    override fun onDestroy() {
        Log.d(TAG, "onDestroy - cleaning up Flutter view")
        flutterView?.let { view ->
            try {
                val bridge = FlutterBridge.getInstance(this)
                bridge.detachView(view)
                flutterView = null
                // Stop engine to ensure next launch is fresh (clears stacked Flutter state)
                bridge.stopEngine()
            } catch (e: Exception) {
                Log.e(TAG, "Error detaching Flutter view", e)
            }
        }
        super.onDestroy()
    }
    
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause - pausing Flutter engine")
        FlutterBridge.getInstance(this).pauseEngine()
    }
    
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume - resuming Flutter engine")
        // Always resume engine even if view isn't created yet
        FlutterBridge.getInstance(this).resumeEngine()
    }
    
    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }
    
    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TradeableFullPageContent(
    mode: String,
    text: String,
    topicId: Int,
    width: Double,
    height: Double,
    onBack: () -> Unit,
    onViewCreated: (io.flutter.embedding.android.FlutterView) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var initState by remember { mutableStateOf<InitState>(InitState.Initializing) }
    var viewReady by remember { mutableStateOf(false) }
    
    // Always finish the Activity on system back
    BackHandler {
        onBack()
    }
    
    // Wait for TFS initialization
    LaunchedEffect(Unit) {
        val bridge = FlutterBridge.getInstance(context)
        var retries = 0
        
        Log.d("TradeableFlutterActivity", "Waiting for TFS initialization...")
        
        while (!bridge.isTFSInitialized() && retries < TradeableFlutterActivity.MAX_INIT_RETRIES) {
            delay(TradeableFlutterActivity.INIT_RETRY_DELAY_MS)
            retries++
        }
        
        if (!bridge.isTFSInitialized()) {
            Log.w("TradeableFlutterActivity", "TFS not initialized after ${retries * TradeableFlutterActivity.INIT_RETRY_DELAY_MS}ms")
            initState = InitState.Failed("SDK initialization timeout")
            return@LaunchedEffect
        }
        
        Log.d("TradeableFlutterActivity", "TFS initialized, ready to create view")
        initState = InitState.Ready
    }
    
    // Send data to Flutter after view is created
    LaunchedEffect(viewReady) {
        if (viewReady) {
            delay(100) // Small delay to ensure view is fully attached
            Log.d("TradeableFlutterActivity", "View ready, sending view state")
            try {
                val bridge = FlutterBridge.getInstance(context)
                bridge.sendViewState(
                    mode = mode,
                    text = text,
                    width = if (width > 0) width else 400.0,
                    height = if (height > 0) height else 600.0,
                    topicId = topicId
                )
                Log.d("TradeableFlutterActivity", "View state sent successfully")
            } catch (e: Exception) {
                Log.e("TradeableFlutterActivity", "Error sending view state", e)
            }
        }
    }
    
    // Fullscreen Flutter should own the whole surface; no top app bar overlay
    Scaffold(
        topBar = {},
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when (initState) {
                is InitState.Initializing -> {
                    CircularProgressIndicator()
                }
                is InitState.Ready -> {
                    FlutterViewContainer(
                        onViewCreated = { view ->
                            onViewCreated(view)
                            viewReady = true
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is InitState.Failed -> {
                    Text(
                        text = "Failed to load: ${(initState as InitState.Failed).error}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun FlutterViewContainer(
    onViewCreated: (io.flutter.embedding.android.FlutterView) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var viewCreated by remember { mutableStateOf(false) }
    var flutterView by remember { mutableStateOf<io.flutter.embedding.android.FlutterView?>(null) }
    
    // Observe lifecycle events and hook close handler so Flutter back buttons finish the Activity
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> Log.d("FlutterViewContainer", "Lifecycle ON_RESUME")
                Lifecycle.Event.ON_PAUSE -> Log.d("FlutterViewContainer", "Lifecycle ON_PAUSE")
                Lifecycle.Event.ON_DESTROY -> Log.d("FlutterViewContainer", "Lifecycle ON_DESTROY")
                else -> {}
            }
        }

        val bridge = FlutterBridge.getInstance(context)
        bridge.setupCloseHandler { (context as? Activity)?.finish() }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            Log.d("FlutterViewContainer", "DisposableEffect cleanup")
            lifecycleOwner.lifecycle.removeObserver(observer)
            bridge.setupCloseHandler(null)
            flutterView?.let { view ->
                try {
                    bridge.detachView(view)
                } catch (e: Exception) {
                    Log.e("FlutterViewContainer", "Error detaching view", e)
                }
            }
        }
    }
    
    AndroidView(
        factory = { ctx ->
            Log.d("FlutterViewContainer", "Creating Flutter view")
            val bridge = FlutterBridge.getInstance(ctx)
            val view = bridge.createFlutterView()
            view.setBackgroundColor(Color.TRANSPARENT)
            flutterView = view
            onViewCreated(view)
            viewCreated = true

            // Force layout to ensure view renders
            view.post {
                Log.d("FlutterViewContainer", "View posted to ensure rendering started")
            }

            view
        },
        modifier = modifier,
        update = { view ->
            Log.d("FlutterViewContainer", "AndroidView update callback - view size: ${view.width}x${view.height}")
        }
    )
}

private sealed class InitState {
    object Initializing : InitState()
    object Ready : InitState()
    data class Failed(val error: String) : InitState()
}