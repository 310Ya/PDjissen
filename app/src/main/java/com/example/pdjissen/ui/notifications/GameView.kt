package com.example.pdjissen.ui.notifications

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class GameView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val charBitmap: Bitmap = BitmapFactory.decodeResource(resources, android.R.drawable.ic_menu_camera)
    private val itemBitmap: Bitmap = BitmapFactory.decodeResource(resources, android.R.drawable.ic_menu_help)

    private val obstacleBitmap: Bitmap = BitmapFactory.decodeResource(resources, android.R.drawable.ic_menu_delete)

    var characterRect = RectF()
    var itemRects = mutableListOf<RectF>()
    var obstacleRects = mutableListOf<RectF>()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(charBitmap, null, characterRect, null)

        itemRects.forEach { rect ->
            canvas.drawBitmap(itemBitmap, null, rect, null)
        }

        obstacleRects.forEach { rect ->
            canvas.drawBitmap(obstacleBitmap, null, rect, null)
        }
    }

    fun updateView() {
        invalidate()
    }
}
