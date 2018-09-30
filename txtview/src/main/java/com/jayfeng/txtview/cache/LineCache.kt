package com.jayfeng.txtview.cache

import com.jayfeng.txtview.page.LineType
import java.io.Serializable

class LineCache : Serializable {

    var l: Int = 0

    var x: Float = 0f
    var y: Float = 0f

    var t: LineType = LineType.CONTENT
}