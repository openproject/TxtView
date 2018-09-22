package com.jayfeng.txtview.page

import android.graphics.Color
import android.graphics.Paint

object NightPaint {

    fun toNight(paint: Paint): Paint {
        val nightPaint = Paint()
        nightPaint.isAntiAlias = paint.isAntiAlias
        nightPaint.textSize = paint.textSize

        nightPaint.color = Color.parseColor("#C2C2C2")

        return nightPaint
    }
}