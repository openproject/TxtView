package com.jayfeng.txtview.cache

import java.io.Serializable

class PageCache : Serializable {

    var s: Int = 0
    var l: Int = 0

    var lines: ArrayList<LineCache>? = null

}