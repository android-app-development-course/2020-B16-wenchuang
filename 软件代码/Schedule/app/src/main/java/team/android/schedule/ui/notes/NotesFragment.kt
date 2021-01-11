package team.android.schedule.ui.notes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import team.android.schedule.R
import team.android.schedule.ui.settings.Settings


class NotesFragment : Fragment() {
    /**
     * NotesFragment 笔记列表界面
     * Fragment加载时会从SharePreferences中的index.xml取出笔记列表
     * 该界面的布局是RecyclerView，对应的Adapter为NotesAdapter
     *
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notes, container, false)
        //加载recyclerview
        val recyclerView = view.findViewById<RecyclerView>(R.id.notes)
        //读取index，将泛型map转为HashMap
        val index = requireActivity().getSharedPreferences("index", Context.MODE_PRIVATE).all
        val temp = HashMap<String, String>()
        for (item in index) {
            temp[item.key] = item.value as String
        }
        //在单例类Settings中放置NotesAdapter的引用，便于跨Activity的访问
        Settings.notesAdapter = NotesAdapter(temp)
        recyclerView.adapter = Settings.notesAdapter
        //悬浮按钮
        val fab = view.findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener() {
            val intent = Intent(context, EditNoteActivity::class.java)
            intent.putExtra("Action", "Create")
            startActivity(intent)
        }
        return view

    }

    override fun onStart() {
        super.onStart()
        //启动时判断是否为空
        Settings.notesAdapter?.let {
            if (it.map.isNotEmpty())
                view?.findViewById<TextView>(R.id.empty)?.visibility = View.GONE
        }

    }

}