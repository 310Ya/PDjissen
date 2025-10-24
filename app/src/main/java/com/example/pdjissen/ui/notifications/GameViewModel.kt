package com.example.pdjissen.ui.notifications

import android.graphics.RectF
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*

// ゲームの状態を管理するenumクラス
enum class GameStatus {
    PLAYING,
    GAME_OVER
}

class GameViewModel : ViewModel() {

    // ゲームオブジェクトの位置や状態を保持するLiveData
    val characterRect = MutableLiveData<RectF>()
    val itemRects = MutableLiveData<MutableList<RectF>>()
    val obstacleRects = MutableLiveData<MutableList<RectF>>()
    val score = MutableLiveData<Int>()

    // ゲームの状態を保持するLiveData
    val gameStatus = MutableLiveData<GameStatus>()

    // ゲームの物理状態
    private var charVelocityY = 0f // Y軸方向の速度
    private val gravity = 2f // 重力
    private var isJumping = false

    // ゲームループ用のCoroutineScope
    private val gameScope = CoroutineScope(Dispatchers.Default)
    private var gameJob: Job? = null

    init {
        // ゲームの初期化
        resetGame()
    }

    // ゲームをリセットする処理
    fun resetGame() {
        score.postValue(0)
        characterRect.postValue(RectF(100f, 600f, 200f, 700f)) // 初期位置
        itemRects.postValue(mutableListOf(
            RectF(600f, 600f, 650f, 650f),
            RectF(1200f, 600f, 1250f, 650f)
        ))
        obstacleRects.postValue(mutableListOf(
            RectF(900f, 650f, 1000f, 700f)
        ))
        gameStatus.postValue(GameStatus.PLAYING) // ゲーム状態をプレイ中に
    }

    // ゲームを開始する
    fun startGameLoop() {
        if (gameStatus.value == GameStatus.GAME_OVER) {
            resetGame() // ゲームオーバーならリセットしてから開始
        }
        gameJob?.cancel() // 既存のループがあればキャンセル
        gameJob = gameScope.launch {
            while (isActive && gameStatus.value == GameStatus.PLAYING) { // プレイ中のみループ
                updateGame()
                delay(16) // 約60FPS
            }
        }
    }

    // ゲームを停止する
    fun stopGameLoop() {
        gameJob?.cancel()
    }

    // ジャンプ処理
    fun jump() {
        if (!isJumping && gameStatus.value == GameStatus.PLAYING) {
            charVelocityY = -35f // 上向きの初速
            isJumping = true
        }
    }

    // ゲームの状態を更新するメインロジック
    private fun updateGame() {
        val charRect = characterRect.value ?: return

        // --- 物理演算 ---
        charVelocityY += gravity
        charRect.top += charVelocityY
        charRect.bottom += charVelocityY

        if (charRect.bottom >= 700f) {
            charRect.bottom = 700f
            charRect.top = 600f
            charVelocityY = 0f
            isJumping = false
        }

        // --- スクロール処理 ---
        val scrollSpeed = 5f
        itemRects.value?.forEach { it.offset(-scrollSpeed, 0f) }
        obstacleRects.value?.forEach { it.offset(-scrollSpeed, 0f) }

        // --- 当たり判定 ---
        val items = itemRects.value ?: mutableListOf()
        val itemsIterator = items.iterator()
        while (itemsIterator.hasNext()) {
            val itemRect = itemsIterator.next()
            if (RectF.intersects(charRect, itemRect)) {
                score.postValue((score.value ?: 0) + 10)
                itemsIterator.remove()
            }
        }

        // 障害物との当たり判定
        obstacleRects.value?.forEach { obstacleRect ->
            if (RectF.intersects(charRect, obstacleRect)) {
                gameStatus.postValue(GameStatus.GAME_OVER)
                stopGameLoop()
            }
        }

        // --- LiveDataを更新してUIに通知 ---
        characterRect.postValue(charRect)
        itemRects.postValue(items)
        obstacleRects.postValue(obstacleRects.value)
    }

    override fun onCleared() {
        super.onCleared()
        stopGameLoop()
    }
}
