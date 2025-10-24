package com.example.pdjissen.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText // EditTextを使うためのimportを追加
import android.widget.TextView // TextViewを使うためのimportを追加
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pdjissen.R

// SensorEventListener はもう必要ないから消したよ！
class HomeFragment : Fragment() {

    // initialSteps はもう使わないから消したよ！

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? { // onCreateView は1回だけ書くよ！
        // 1. 見た目のファイル（fragment_home.xml）を画面に表示するよ
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // 2. XMLから部品を探してくるよ
        val startButton: Button = view.findViewById(R.id.btnStartMeasure)
        val statusButton: Button = view.findViewById(R.id.home_Status) // 追加されたボタン
        val editText: EditText = view.findViewById(R.id.editTextText) // 追加されたEditText
        val textView: TextView = view.findViewById(R.id.text_home) // ようこそ！のTextView

        // 3. 「START」ボタンに、「押されたらDashboard画面に移動してね」って命令するよ
        startButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_dashboard)
        }

        // --- ここに、新しく追加されたボタン（home_Next, home_Status）や ---
        // --- EditText（editTextText）が押されたり、入力されたりした時の ---
        // --- 処理をこれから追加していくことになるよ！ ---
        // 例：
        // nextButton.setOnClickListener { /* 何か処理 */ }
        // statusButton.setOnClickListener { /* 何か処理 */ }


        // 4. 準備ができたViewを返すよ
        return view // return view も1回だけ！
    }

    // 歩数計関係のメソッド (onViewCreated, onResume, onPause, onSensorChanged など) は
    // DashboardFragment にお引越ししたので、ここではもう必要ないよ！
}