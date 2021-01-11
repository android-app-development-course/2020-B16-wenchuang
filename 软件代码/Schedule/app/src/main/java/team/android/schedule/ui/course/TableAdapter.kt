package team.android.schedule.ui.course

import android.content.Context
import android.content.Intent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import team.android.schedule.R
import team.android.schedule.ui.notes.EditNoteActivity
import team.android.schedule.ui.settings.Settings


class TableAdapter(var weekList: ArrayList<ArrayList<Course>>) :
    RecyclerView.Adapter<TableAdapter.ViewHolder>()
/**
 * CourseTable对应的RecyclerView 适配器，加载的item为src\main\res\layout\table_layout.xml
 * @param weekList ArrayList<ArrayList<Course>>将整个学期的课程打包成一周，每个ArrayList<Course>
 *     代表一周中一天的全部课程
 */
{
    private val linearLayout =
        listOf(
            R.id.Monday, R.id.Tuesday, R.id.Wednesday, R.id.Thursday, R.id.Friday,
            R.id.Saturday, R.id.Sunday
        )

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layouts = ArrayList<LinearLayout>()

        init {
            for (i in 0..6) {
                layouts.add(view.findViewById(linearLayout[i]))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //默认情况下只有四个ViewHolder
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_course_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        for ((index, layout) in holder.layouts.withIndex()) {
            layout.removeAllViewsInLayout()
            if (index > Settings.dayOfWeek) continue
            if (weekList.size == 0) {
                for (i in 1..Settings.coursesOfDay) {
                    layout.addView(createCell(view = layout, tagC = "${position + 1},$index,$i"))
                }
                continue
            }
            val day = ArrayList<Course>()  //取出当天的课程
            for (c in weekList[index]) {
                var t = 0
                while (t < c.weekTime.lastIndex) {  //该课程是否在本周显示
                    if (position + 1 >= c.weekTime[t] && position + 1 <= c.weekTime[t + 1]) {
                        day.add(c)
                        break
                    }
                    t += 2
                }
            }
            day.sort()
            if (day.isNotEmpty()) {
                var counter = 1
                var cur = 0
                while (counter <= Settings.coursesOfDay) {
                    //默认无课的格子为白色单元格
                    var cell: TextView

                    if (cur >= day.size) {   //当天课程已填充完毕
                        //填充空白单元格
                        cell = createCell(layout, tagC = "${position + 1},$index,$counter")
                        layout.addView(cell)
                        counter++
                        continue
                    }

                    val course = day[cur]

                    // 该节次是否有课要显示
                    if (counter == course.dayTime[0]) {
                        val tmp = course.dayTime[1] - course.dayTime[0] + 1
                        val height = tmp * Settings.cellHeight + (tmp - 1) * Settings.margin
                        cell = createCell(
                            layout, course.toString(), height, course.color,
                            tagC = course
                        )
                        counter += tmp
                        cur++
                    } else {
                        cell = createCell(layout, tagC = "${position + 1},$index,$counter")
                        counter++
                    }
                    layout.addView(cell)
                }
            } else {
                for (i in 1..Settings.coursesOfDay) {
                    layout.addView(createCell(layout, tagC = "${position + 1},$index,$i"))
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return Settings.totalWeek
    }

    private fun createCell(
        view: View, _text: String = "", _height: Int = Settings.cellHeight,
        color: Int = Settings.White, tagC: Any
    ): TextView {
        //创建课程的TextView，tagC为Course或String形式的表格位置信息"周数,星期,节次"
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, Settings.margin)
        return TextView(view.context).apply {
            layoutParams = params
            text = _text
            gravity = Gravity.CENTER_HORIZONTAL
            textSize = 12.0f
            setBackgroundColor(color)
            height = _height
            width = Settings.cellWidth
            setPadding(5, 5, 5, 5)  //文字距离四周有5px距离
            tag = tagC
            setOnClickListener() {
                if (text.isNotBlank()) {
                    showMenu(context, it)
                } else {
                    Settings.popupWindow?.showAtLocation(tagC)
                }
            }
        }
    }

    private fun showMenu(context: Context, view: View) {
        // 显示悬浮菜单
        val menu = PopupMenu(context, view)
        val course = ((view as TextView).tag as Course)
        menu.menuInflater.inflate(R.menu.popup_menu, menu.menu)

        menu.setOnMenuItemClickListener { menuItem ->

            when (menuItem.itemId) {
                R.id.add_note -> {
                    val intent = Intent(context, EditNoteActivity::class.java)
                    intent.putExtra("Action", "AddByCourse")
                    intent.putExtra("curWeek", Settings.curWeek.toString())
                    intent.putExtra("weekDay", course.weekDay.toString())
                    intent.putExtra("className", course.name)
                    context.startActivity(intent)
                }
                R.id.modify_course -> {
                    Settings.popupWindow?.showAtLocation(view.tag)
                }
                R.id.del_course -> {
                    AlertDialog.Builder(context).let {
                        it.setMessage("确定要删除?")
                        it.setPositiveButton("确定") { _, _ -> delCourse(view.tag) }
                        it.setNegativeButton("取消", null)
                        it.show()
                    }
                }

            }
            false
        }

        menu.show()
    }

    private fun delCourse(tag: Any) {
        //删除课程
        if (tag is Course) {
            for (list in weekList) {
                if (list.remove(tag)) {
                    notifyDataSetChanged()
                    break
                }
            }
        }
    }

    fun updateData(courses: ArrayList<ArrayList<Course>>) {
        //更新数据
        weekList = courses
        notifyDataSetChanged()
    }
}