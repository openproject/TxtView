package com.jayfeng.txtview.touch

import com.jayfeng.txtview.page.Page


interface PageTouchLinstener {

    fun onClick(touchType: TouchType, page: Page)

}