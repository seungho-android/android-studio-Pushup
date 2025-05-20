package com.example.pushupupup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// 데이터 클래스 정의
data class PushupRecord(
    val dateTime: String,
    val name: String,
    val age: String,
    val count: Int
)

class RecordAdapter(private val records: List<PushupRecord>) :
    RecyclerView.Adapter<RecordAdapter.RecordViewHolder>() {

    class RecordViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateTimeText: TextView = view.findViewById(R.id.dateTimeText)
        val userInfoText: TextView = view.findViewById(R.id.userInfoText)
        val countText: TextView = view.findViewById(R.id.countText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_record, parent, false)
        return RecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val record = records[position]
        holder.dateTimeText.text = record.dateTime
        holder.userInfoText.text = "${record.name} (${record.age}세)"
        holder.countText.text = "푸쉬업 ${record.count}개"
    }

    override fun getItemCount(): Int = records.size
}
