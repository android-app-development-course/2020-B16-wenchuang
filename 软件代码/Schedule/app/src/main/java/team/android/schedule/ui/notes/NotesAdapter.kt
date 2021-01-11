package team.android.schedule.ui.notes

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import team.android.schedule.R
import team.android.schedule.ui.settings.Settings
import java.io.File

class NotesAdapter(
    var map: HashMap<String, String>
) : RecyclerView.Adapter<NotesAdapter.ViewHolder>() {

    /**
     * @param map:HashMap<String,String> key为笔记文件的文件名，value为标题
     * @property items：List<Pair<key,value>> map以键排序后的列表，用于显示
     * 每个item都是一个TextView，text显示标题，tag绑定文件名，通过点击事件打开EditNoteActivity
     */
    private var items = map.toList().sortedBy { it.first }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_notes_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.item.text = items[position].second
        holder.item.tag = items[position].first
        val context = holder.itemView.context
        val path = items[position].first
        holder.item.setOnClickListener()
        {
            val intent = Intent(context, EditNoteActivity::class.java)
            intent.putExtra("Action", "Load")
            intent.putExtra("filename", items[position].first)
            context.startActivity(intent)
        }
        holder.item.setOnLongClickListener { _ ->
            AlertDialog.Builder(context).let { builder ->
                builder.setMessage("是否删除记录？")
                builder.setPositiveButton("确定") { _, _ ->
                    val prefs = context.getSharedPreferences(path, Context.MODE_PRIVATE)
                    val image = prefs.getString("Images", "")
                    if (image != null) {//删除图片
                        for (str in image.split(",")) {
                            if (str.isNotBlank()) {
                                val file = File(str)
                                if (file.exists())
                                    file.delete()
                            }
                        }
                    }
                    val file = File("/data/data/${context.packageName}/shared_prefs/${path}.xml")
                    if (file.exists())//删除note
                        file.delete()

                    Settings.notesAdapter?.let {
                        it.map.remove(path)
                        it.updateData()
                        val index = context.getSharedPreferences(
                            "index", Context.MODE_PRIVATE
                        ).edit()  //更新索引
                        index.clear()
                        for (item in it.map) {
                            index.putString(item.key, item.value)
                        }
                        index.apply()
                    }
                    Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show()
                }
                builder.setNegativeButton("取消", null)
                builder.show()
            }
            false
        }
    }

    override fun getItemCount(): Int = map.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val item: TextView = view.findViewById(R.id.note_item)

        override fun toString(): String {
            return super.toString() + " '" + item.text + "'"
        }
    }

    fun updateData(notes: HashMap<String, String> = map) {
        map = notes
        items = notes.toList().sortedBy { it.first }
        notifyDataSetChanged()
    }
}