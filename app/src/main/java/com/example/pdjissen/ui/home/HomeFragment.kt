package com.example.pdjissen.ui.home // ← パッケージ名はお兄ちゃんの環境に合わせてね

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pdjissen.R

// SensorEventListener を消して、ただの Fragment にするよ
class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 1. 見た目のファイル（fragment_home.xml）を画面に表示するよ
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // 2. 「START」ボタン（btnStartMeasure）を探してくるよ
        val startButton: Button = view.findViewById(R.id.btnStartMeasure)

        // 3. 「START」ボタンに、「押されたらDashboard画面に移動してね」って命令するよ
        startButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_dashboard)
        }

        // 4. 準備ができたViewを返すよ
        return view
    }

    // ↓↓↓ 歩数計関係のコードはぜんぶ消したよ！ ↓↓↓
    // (onViewCreated, onResume, onPause, onSensorChanged,
    //  onAccuracyChanged, checkPermissionAndStart,
    //  startStepCounter, onRequestPermissionsResult は全部削除！)
}