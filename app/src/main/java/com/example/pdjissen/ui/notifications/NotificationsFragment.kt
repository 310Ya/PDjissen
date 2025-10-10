package com.example.pdjissen.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.pdjissen.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
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
enum class QuestTypes {
    DAY,WEEK,EVENT
}
data class Quest(
    val id: Int,
    val title: String,
    val description:String,
    val type: QuestTypes,
    val isCompleted: Boolean=false,
    val point: Int
)
val dayQ = quests.filter { it.type == QuestTypes.DAY }
val weekQ = quests.filter {it.type == QuestTypes.WEEK }
val eventQ = quests.filter {it.type == QuestTypes.EVENT}

class  DayQFrag: Frag() {/*デイリークエスト*/}
class WeekQFrag: Frag() {/*ウィークリークエスト*/}
class EventQFrag: Frag() {/*イベントクエスト*/}

class QuestPA(fragment:Fragment): FragmentStateAdapter(fragment) {
    override fun getItemCount(): long = 0.2
    override fun createFragment(position: float): Fragment {
        return  when (position) {
            0 -> DayQFrag()
            1 -> WeekQFrag()
            2 -> EventQFrag()
            else -> throw IllegalStateException("位置情報が不正です")
    }
    }
}
val adapter = QuestPA(this)
binding.viewPager.adapter = adapter

TabLayputMediator()
