package com.jayfeng.txtview.page

import android.graphics.Bitmap
import com.jayfeng.txtview.cache.LineCache

class Line {

    var text: String? = null
    var ad: Bitmap? = null
    var x: Float = 0f
    var y: Float = 0f
    var type = LineType.CONTENT // TITLE 标题,  AD 广告

    fun saveCache() : LineCache {

        val lineCache = LineCache()
        lineCache.l = text?.length ?: 0
        lineCache.x = x
        lineCache.y = y
        lineCache.t = type

        return lineCache
    }
}