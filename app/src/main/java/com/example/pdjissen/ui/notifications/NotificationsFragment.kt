package com.example.pdjissen.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

import com.example.pdjissen.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var gameViewModel: GameViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        gameViewModel = ViewModelProvider(this).get(GameViewModel::class.java)
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)

        // ViewModelのデータ変更を監視して、GameViewに描画指示を出す
        gameViewModel.characterRect.observe(viewLifecycleOwner) { rect ->
            binding.gameView.characterRect = rect
            binding.gameView.updateView()
        }
        gameViewModel.itemRects.observe(viewLifecycleOwner) { rects ->
            binding.gameView.itemRects = rects
        }
        gameViewModel.obstacleRects.observe(viewLifecycleOwner) { rects ->
            binding.gameView.obstacleRects = rects
        }
        gameViewModel.score.observe(viewLifecycleOwner) { score ->
            // スコアが0より大きい初回のみToastを表示するなどの工夫も可能
            // if (score > 0) { ... }
        }

        // ゲーム状態の変更を監視する処理
        gameViewModel.gameStatus.observe(viewLifecycleOwner) { status ->
            if (status == GameStatus.GAME_OVER) {
                // ゲームオーバーになったらメッセージを表示
                Toast.makeText(requireContext(), "ゲームオーバー！", Toast.LENGTH_LONG).show()
                binding.jumpButton.text = "リトライ" // ボタンのテキストを変更
            } else if (status == GameStatus.PLAYING) {
                // プレイ中になったらボタンのテキストを「ジャンプ」に戻す
                binding.jumpButton.text = "ジャンプ"
            }
        }

        // ジャンプボタンのクリックリスナー
        binding.jumpButton.setOnClickListener {
            // ゲーム状態でボタンの役割を変える
            if (gameViewModel.gameStatus.value == GameStatus.PLAYING) {
                gameViewModel.jump()
            } else {
                // ゲームオーバー状態なら、ゲームをリスタートする
                gameViewModel.startGameLoop()
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        gameViewModel.startGameLoop()
    }

    override fun onPause() {
        super.onPause()
        gameViewModel.stopGameLoop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
