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
            binding.gameView.updateView() // ★再描画を呼び出す
        }
        gameViewModel.itemRects.observe(viewLifecycleOwner) { rects ->
            binding.gameView.itemRects = rects
        }
        gameViewModel.obstacleRects.observe(viewLifecycleOwner) { rects ->
            binding.gameView.obstacleRects = rects
        }

        // ゲーム状態の変更を監視
        gameViewModel.gameStatus.observe(viewLifecycleOwner) { status ->
            if (status == GameStatus.GAME_OVER) {
                Toast.makeText(requireContext(), "ゲームオーバー！", Toast.LENGTH_LONG).show()
                binding.jumpButton.text = "リトライ"
            } else if (status == GameStatus.PLAYING) {
                binding.jumpButton.text = "ジャンプ"
            }
        }

        // ジャンプボタンのクリックリスナー
        binding.jumpButton.setOnClickListener {
            if (gameViewModel.gameStatus.value == GameStatus.PLAYING) {
                gameViewModel.jump()
            } else {
                gameViewModel.startGameLoop()
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // 画面が表示されたらゲームループを開始
        gameViewModel.startGameLoop()
    }

    override fun onPause() {
        super.onPause()
        // 画面が非表示になったらゲームループを停止
        gameViewModel.stopGameLoop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
