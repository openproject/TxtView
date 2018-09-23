package com.jayfeng.txtview.theme

import android.graphics.Color
import android.graphics.Paint

class NightTheme: ITheme {

    override fun toNight(paint: Paint): Paint {
        val nightPaint = Paint()
        nightPaint.isAntiAlias = paint.isAntiAlias
        nightPaint.textSize = paint.textSize

        nightPaint.color = Color.parseColor("#C2C2C2")

        return nightPaint
    }
}