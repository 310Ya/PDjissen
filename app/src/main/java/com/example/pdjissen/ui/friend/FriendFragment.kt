package com.example.pdjissen.ui.friend // ← friend フォルダになってるよね？

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.pdjissen.R

class FriendFragment : Fragment() { // クラス名は FriendFragment だよね？

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_friend, container, false)

        // 2. （今はまだ何もしない！）

        // 3. 準備ができたViewを返すよ
        return view
    }
}