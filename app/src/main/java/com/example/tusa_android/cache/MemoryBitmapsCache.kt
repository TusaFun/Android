package com.example.tusa_android.cache

import android.graphics.Bitmap
import android.os.Bundle
import android.util.LruCache

class MemoryBitmapsCache(savedInstanceState: Bundle?) {
    companion object {
        fun createInstance(savedInstanceState: Bundle?) {
            instance = MemoryBitmapsCache(savedInstanceState)
        }

        fun getInstance() : MemoryBitmapsCache{
            return instance
        }

        private lateinit var instance: MemoryBitmapsCache
    }

    fun saveBitmap(path: String, bitmap: Bitmap) {
        memoryCache.put(path, bitmap)
    }

    fun loadBitmap(bitmapKey: String): Bitmap? {
        try {
            return memoryCache.get(bitmapKey)
        } catch (ex: Exception) {
           ex.printStackTrace()
        }
        return null
    }

    private var memoryCache: LruCache<String, Bitmap>

    init {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory
        memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String?, bitmap: Bitmap?): Int {
                return (bitmap!!.rowBytes * bitmap.height)/1024;
            }
        }
    }
}