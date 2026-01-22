package com.tradeable.sdk.core

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.util.Log

/**
 * Content provider for early SDK initialization.
 * This runs before Application.onCreate() and helps pre-warm the Flutter engine.
 */
internal class TradeableInitProvider : ContentProvider() {
    
    companion object {
        private const val TAG = "TradeableInitProvider"
    }
    
    override fun onCreate(): Boolean {
        Log.d(TAG, "TradeableInitProvider onCreate - SDK provider registered")
        // We don't auto-initialize here, the app must call TradeableSDK.initialize()
        // This provider just ensures the SDK classes are loaded early
        return true
    }
    
    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = null
    
    override fun getType(uri: Uri): String? = null
    
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    
    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0
}
