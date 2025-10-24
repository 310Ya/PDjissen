package com.example.pdjissen.ui.notifications

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.example.pdjissen.R

class GameView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    // 画像リソースを読み込んでBitmapに変換
    private val charBitmap: Bitmap = BitmapFactory.decodeResource(resources, android.R.drawable.ic_menu_camera)
    private val itemBitmap: Bitmap = BitmapFactory.decodeResource(resources, android.R.drawable.ic_menu_help)
    // 障害物用の画像を drawable に追加してください (例: obstacle.png)
    private val obstacleBitmap: Bitmap = BitmapFactory.decodeResource(resources, android.R.drawable.ic_menu_delete)

    // ゲームオブジェクトの描画位置とサイズを保持するRectF
    var characterRect = RectF()
    var itemRects = mutableListOf<RectF>()
    var obstacleRects = mutableListOf<RectF>()

    // onDrawメソッドで、Canvasに画像を描画する
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 背景などを描画 (今回は省略)

        // キャラクターを描画
        canvas.drawBitmap(charBitmap, null, characterRect, null)

        // アイテムを描画
        itemRects.forEach { rect ->
            canvas.drawBitmap(itemBitmap, null, rect, null)
        }

        // 障害物を描画
        obstacleRects.forEach { rect ->
            canvas.drawBitmap(obstacleBitmap, null, rect, null)
        }
    }

    // ゲーム状態が更新されたときに再描画を要求する
    fun updateView() {
        invalidate() // このメソッドを呼ぶと onDraw が再実行される
    }
}
