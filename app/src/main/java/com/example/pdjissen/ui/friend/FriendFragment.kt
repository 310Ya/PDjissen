package com.example.pdjissen.ui.friend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.ui.layout.layout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pdjissen.Friend
import com.example.pdjissen.R
import kotlin.io.path.name

class FriendFragment : Fragment() {

    // onCreateView は、このFragmentの「見た目」を生成するときに呼ばれる
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // fragment_friend.xml のレイアウトを読み込む
        val view = inflater.inflate(R.layout.fragment_friend, container, false)

        // 仮のフレンドデータを作成する（本来はサーバーなどから取得する）
        val friendList = listOf(
            Friend("user001", "Alice", true, "5分前"),
            Friend("user002", "Bob", false, "3時間前"),
            Friend("user003", "Charlie", true, "オンライン"),
            Friend("user004", "David", false, "1日前"),
            Friend("user005", "Eve", false, "2024/10/26"),
            Friend("user006", "Frank", true, "1分前"),
            Friend("user007", "Grace", false, "5日前")
        )

        // レイアウトからRecyclerViewを見つける
        val friendsRecyclerView: RecyclerView = view.findViewById(R.id.friends_recycler_view)

        // RecyclerViewにアダプターとレイアウトマネージャーをセット
        friendsRecyclerView.adapter = FriendAdapter(friendList)
        friendsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 準備ができたViewを返す
        return view
    }
}


/**
 * RecyclerViewにフレンドのデータを表示するためのアダプタークラス
 * FriendFragment の中に書くことで、このファイルだけで完結する
 */
class FriendAdapter(private val friends: List<Friend>) : RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {

    // 1. 各項目の「見た目」（item_friend.xml）を保持する ViewHolder を定義
    class FriendViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val statusIndicator: View = view.findViewById(R.id.status_indicator)
        val nameTextView: TextView = view.findViewById(R.id.friend_name_text)
        val lastLoginTextView: TextView = view.findViewById(R.id.last_login_text)
    }

    // 2. ViewHolder を生成する時に呼ばれる（レイアウトを生成）
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        // item_friend.xml のレイアウトを読み込む
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend, parent, false)
        return FriendViewHolder(view)
    }

    // 3. ViewHolder にデータをセットする時に呼ばれる
    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friends[position] // 表示するフレンドを取得

        // データを各Viewにセットする
        holder.nameTextView.text = friend.name
        holder.lastLoginTextView.text = friend.lastLogin

        // オンライン状態に応じて、インジケーターの色を変える
        if (friend.isOnline) {
            holder.statusIndicator.setBackgroundResource(R.drawable.shape_circle_green)
            holder.lastLoginTextView.text = "オンライン" // オンラインならステータスを上書き
        } else {
            holder.statusIndicator.setBackgroundResource(R.drawable.shape_circle_gray)
            holder.lastLoginTextView.text = "最終ログイン: ${friend.lastLogin}"
        }
    }

    // 4. 表示するアイテムの総数を返す
    override fun getItemCount() = friends.size
}
