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

    private lateinit var questViewModel: QuestViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        questViewModel = ViewModelProvider(this).get(QuestViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root


        questViewModel.quest.observe(viewLifecycleOwner) { quest ->

            binding.textQuestTitle.text = quest.title
            binding.textQuestDescription.text = quest.description


            if (quest.state == GameState.COMPLETED) {

                binding.imageItem.visibility = View.GONE // アイテムを非表示
                Toast.makeText(requireContext(), "${quest.point}ポイントゲット！", Toast.LENGTH_SHORT).show()
            } else {

                binding.imageItem.visibility = View.VISIBLE // アイテムを表示
            }
        }


        binding.imageCharacter.setOnClickListener {
            questViewModel.completeQuest()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
