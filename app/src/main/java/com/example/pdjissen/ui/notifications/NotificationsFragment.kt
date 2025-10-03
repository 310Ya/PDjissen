package com.example.pdjissen.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.example.pdjissen.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    // This property is only valid between onCreateView and
    // onDestroyView.

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
    private val id: Int,
    private val title: String="クエスト",
    private val description: String,
    private val isCompared: Boolean = false,
    val point: Int
)
// quest list added
class QuestUI : questUI() {
    private val _quests = MutableLiveData<List<Quest>>()
    val quests: LiveData<List<Quest>> get() = _quests
    init{
        _quests.value = listOf(
            Quest(1,"今日は4kmを目標にして歩きましょう","4km歩くと4ポイント入手できます。",false,4),
            Quest(2,"1時間歩きましょう","1時間歩くと2ポイント入手できます。",false,2)
        )
    }
    fun compQuest(id: Int) {
        _quests.value = _quests.value?.map {
            if (it.id == id) it.copy(isCompleted = true) else it
        }
    }
}
//quest management added