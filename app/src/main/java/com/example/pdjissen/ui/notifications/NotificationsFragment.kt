package com.example.pdjissen.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.lifecycle.observe
import com.example.pdjissen.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textNotifications
        notificationsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class Quest(
    val id: Int,
    val title: String="クエスト",
    val description: String,
    val isCompleted: Boolean = false,
    val point: Int
)

// This would typically be in its own file, e.g., QuestViewModel.kt
class QuestUI : ViewModel() {
    private val _quests = MutableLiveData<List<Quest>>()
    val quests: LiveData<List<Quest>> get() = _quests
    init{
        _quests.value = listOf(
            Quest(1,"今日は4kmを目標にして歩きましょう","4km歩くと4ポイント入手できます。",false,4),
            Quest(2,"1時間歩きましょう","1時間歩くと2ポイント入手できます。",false,2)
        )
    }

    fun compQuest(id: Int) {
        // The '.value?' ensures this runs only if the LiveData has a value.
        _quests.value = _quests.value?.map { quest ->
            if (quest.id == id) {
                quest.copy(isCompleted = true)
            } else {
                quest
            }
        }
    }
}
