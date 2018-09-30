package com.jayfeng.txtview.cache

import android.content.Context
import com.jayfeng.lesscode.core.SerializeLess
import com.jayfeng.txtview.page.Page
import java.io.File
import java.io.Serializable
import kotlin.collections.ArrayList

class TxtCache : Serializable {

    companion object {

        fun restore(context: Context, cacheKey: String) : TxtCache {

            val txtCache = SerializeLess.`$de`<TxtCache>(context.cacheDir.absolutePath + File.separator + cacheKey)
            return txtCache
        }
    }

    var cacheKey: String? = null
    var pageCacheList : ArrayList<PageCache>? = null
    var fontSize = 18f

    var pageList: ArrayList<Page>? = null
    var pageMark = 1

    fun save(context: Context) {
        SerializeLess.`$se`(context.cacheDir.absolutePath + File.separator + cacheKey!!, this@TxtCache)
    }


}