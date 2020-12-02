package team.android.schedule.ui.course

import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_course_table.*
import team.android.schedule.R
import team.android.schedule.ui.settings.Settings
import team.android.schedule.ui.webview.Spider
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CourseTableFragment.newInstance] factory method to
 * create an instance of this fragment.
 */


class CourseTableFragment : Fragment()
{
    /**
     * @author Yehoar
     * 该类负责填充课程表格布局，布局文件为src\main\res\layout\fragment_course_table.xml
     * 项目安卓API等级为16，部分过时的函数需要更高版本的API替换
     * 界面目前分为四个部分：
     * (1)顶部showWeek:TextView 封装在一个RelativeLayout中居中显示周数,在createTopTitle()中初始化
     * (2)中间weekTitle负责显示星期几，共8个TextView封装在一个LinearLayout中,在createTopTitle()中初始化
     * (3)左侧课程节次负责显示第几节课，共Settings.coursesOfDay个TextView封装在一个LinearLayout中,在createLeftBar()中初始化
     * (4)右侧课程信息，该部分是一个RecyclerView，与上面的leftBar封装在同一个LinearLayout中，放在界面的下半部分
     * 以上4个View都封装在id为root的LinearLayout中
     * 利用动态计算的单元格大小实现类似GridLayout的视图
     * 该布局原本是在Activity中使用，现修改适配Fragment
     */

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var adapter: TableAdapter

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_course_table, container, false)
    }

    companion object
    {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CourseTableFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) = CourseTableFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
                putString(ARG_PARAM2, param2)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        val dm = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(dm)  //替换需要API30的 display
        Settings.setScreenParameter(dm.widthPixels, dm.heightPixels)
        //填充布局

        //        openWebView.setOnClickListener {
        //            val intent = Intent(this, WebActivity::class.java)
        //            startActivityForResult(intent, 1)
        //        }
        createTopTitle()
        createLeftBar()
        fillTable()
    }

    private fun createTopTitle()
    {
        val t = "第${Settings.curWeek}周"  //此处单纯不想让IDE警告，可合并
        showWeek.text = t
        showWeek.height = Settings.cellHeight
        showWeek.width = Settings.screenWidth

        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1  //计算今天是星期几，注意虚拟机默认时区
        val header = listOf(" ", "周一", "周二", "周三", "周四", "周五", "周六", "周日")
        val tmp = (Settings.screenWidth - Settings.cellWidth) / 7   //非空白格子的宽度较大
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.setMargins(0, 0, Settings.margin, Settings.margin)

        for ((index, str) in header.withIndex())
        {
            val cell = TextView(context)
            cell.text = str
            cell.gravity = Gravity.CENTER_HORIZONTAL
            cell.textSize = 14.0f
            cell.layoutParams = params
            cell.height = Settings.cellHeight
            cell.setBackgroundColor(Settings.WhiteSnow)
            //宽度设置，第一项略窄
            if (index == 0)
            {
                cell.width = Settings.cellWidth
            } else
            {
                cell.width = tmp
            }
            if (today == index)
            {
                cell.setTextColor(Settings.DeepSkyBlue)
            }
            weekTitle.addView(cell)
        }

        //此处重新定义了每个格子的高度：剩余空白部分/(最大每天课程数+3),确定导航栏之后需修改该部分权重
        //此时单元格高度已被固定，但宽度未定
        Settings.cellHeight = ((Settings.screenHeight - 2 * (Settings.cellHeight + Settings.margin)) / (Settings.coursesOfDay + 2.5)).toInt()
    }

    private fun createLeftBar()
    {
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.setMargins(0, 0, 0, Settings.margin)
        for (i in 1..Settings.coursesOfDay)
        {
            val cell = TextView(context)
            cell.text = i.toString()
            cell.gravity = Gravity.CENTER_HORIZONTAL
            cell.textSize = 14.0f
            cell.layoutParams = params
            //注意，单元格的高度已被createTopTitle修改
            params.height = Settings.cellHeight
            params.width = Settings.cellWidth
            cell.setBackgroundColor(Settings.WhiteSnow)
            leftBar.addView(cell)
        }

        //此处重新定义了每个格子的宽度：剩余空白部分/7
        Settings.cellWidth = (Settings.screenWidth - Settings.cellWidth - Settings.margin) / 7
    }

    private fun fillTable()
    {
        val layoutManager = LinearLayoutManager(context)  //RecyclerView布局管理
        val helper = PagerSnapHelper()  //分页助手，每次划动只翻一页，且将该页居中
        val listener = object : RecyclerView.OnScrollListener()  //RecyclerView划动事件监听，负责更新showWeek
        {
            var move = 0
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int)
            {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                {
                    val child = recyclerView.getChildAt(0)  //获取RecyclerView当前显示的item
                    Settings.showWeek = recyclerView.getChildLayoutPosition(child) + 1

                    val tmp = "第${Settings.showWeek}周"
                    showWeek.text = tmp
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int)
            {
                super.onScrolled(recyclerView, dx, dy)
                move = dx  //从右往左划 dx>0 反之dx<0
            }
        }

        // 课程测试文件载入
        var courses: ArrayList<Map<String, ArrayList<Course>>>
        try
        {
            val input = activity?.openFileInput("学生课表查询.html")
            val reader = BufferedReader(InputStreamReader(input))
            courses = Spider().fromSCNU(reader.readText())
            reader.close()
        } catch (error: FileNotFoundException)
        {
            Log.e("fillTable()", "找不到课程表文件")
            courses = ArrayList(0)
        }

        layoutManager.orientation = LinearLayoutManager.HORIZONTAL

        recyclerView.addOnScrollListener(listener)
        recyclerView.layoutManager = layoutManager

        helper.attachToRecyclerView(recyclerView)
        adapter = TableAdapter(courses)
        recyclerView.adapter = adapter
        Settings.adapter = adapter

        //启动时默认显示当前周
        layoutManager.scrollToPosition(Settings.curWeek - 1)
    }

    //    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    //        super.onActivityResult(requestCode, resultCode, data)
    //        when (requestCode) {
    //            1 -> if (resultCode == AppCompatActivity.RESULT_OK) {
    //                val html = data?.getStringExtra("html") ?: ""
    //                val courses = Spider().fromSCNU(html)
    //                adapter.replaceData(courses)
    //            }
    //        }
    //    }
}