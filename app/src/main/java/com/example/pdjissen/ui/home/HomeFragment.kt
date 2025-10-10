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
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // ボタンを取得
        val startButton: Button = view.findViewById(R.id.btnStartMeasure)

        // ボタンを押したらDashboardへ遷移
        startButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_dashboard)
        }

        return view
    }
}
