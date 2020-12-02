package team.android.schedule.ui.course

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import team.android.schedule.R
import team.android.schedule.ui.settings.Settings


class TableAdapter(private var weekList: ArrayList<Map<String, ArrayList<Course>>>) :
    RecyclerView.Adapter<TableAdapter.ViewHolder>()
/**
 * RecyclerView 适配器，加载的item为src\main\res\layout\table_layout.xml
 * @author Yehoar
 * @param weekList 来源可以是Spride.fromSCNU()，也可以从文件读取，JSON格式（待开发)
 */
{
    private val keys =
        listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    private val values = listOf(
        R.id.Monday,
        R.id.Tuesday,
        R.id.Wednesday,
        R.id.Thursday,
        R.id.Friday,
        R.id.Saturday,
        R.id.Sunday
    )

    //private val weekList = ArrayList<Map<String, ArrayList<Course>>>()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    {
        val layouts = HashMap<String, LinearLayout>()

        init
        {
            for (i in 0..6)
            {
                layouts[keys[i]] = view.findViewById(values[i])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        //默认情况下只有四个ViewHolder
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_course_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        for ((key, value) in holder.layouts)
        {
            value.removeAllViewsInLayout()
            if (weekList.size == 0)
            {
                for (i in 1..Settings.coursesOfDay)
                {
                    value.addView(createCell(value))
                }
                continue
            }
            val day = weekList[position][key]
            if (day != null)
            {
                var counter = 1
                var cur = 0
                while (counter <= Settings.coursesOfDay)
                {
                    //默认无课的格子为白色单元格
                    var cell: TextView

                    if (day.isEmpty() || cur >= day.size)
                    {   //填充空白单元格
                        cell = createCell(value)
                        value.addView(cell)
                        counter++
                        continue
                    }

                    val course = day[cur]
                    if (counter == course.dayTime[0])
                    {
                        val tmp = course.dayTime[1] - course.dayTime[0] + 1
                        val height = tmp * Settings.cellHeight + (tmp - 1) * Settings.margin
                        cell = createCell(value, course.toString(), height, course.color)
                        counter += tmp
                        cur++
                    } else
                    {
                        counter++
                        cell = createCell(value)
                    }
                    value.addView(cell)
                }
            } else
            {
                for (i in 1..Settings.coursesOfDay)
                {
                    value.addView(createCell(value))
                }
            }
        }
    }

    override fun getItemCount(): Int
    {
        return if(weekList.size>0)
            weekList.size
        else
            1
    }

    private fun createCell(
        view: View,
        text: String = "",
        height: Int = Settings.cellHeight,
        color: Int = Settings.White
    ): TextView
    {
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, Settings.margin, Settings.margin)
        val cell = TextView(view.context)
        cell.layoutParams = params
        cell.text = text
        cell.gravity = Gravity.CENTER_HORIZONTAL
        cell.textSize = 12.0f
        cell.setBackgroundColor(color)
        cell.height = height
        cell.width = Settings.cellWidth
        cell.setPadding(5, 5, 5, 5)  //文字距离四周有5px距离
        return cell
    }

    fun replaceData(courses: ArrayList<Map<String, ArrayList<Course>>>)
    {
        weekList = courses
        notifyDataSetChanged()
    }

}