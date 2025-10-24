package com.example.pdjissen.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.example.pdjissen.R

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // レイアウトを読み込む
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // ダッシュボード画面へ遷移するボタンを取得
        val btnGoDashboard: Button = view.findViewById(R.id.btnStartMeasure)

        // クリック時に DashboardFragment へ遷移
        btnGoDashboard.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_dashboard)
        }

        return view
    }
}
