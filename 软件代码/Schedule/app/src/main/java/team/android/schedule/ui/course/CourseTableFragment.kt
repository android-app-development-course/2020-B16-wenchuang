package team.android.schedule.ui.course

import android.os.Bundle
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
import team.android.schedule.ui.webview.Parser
import java.io.FileNotFoundException
import java.util.*


class CourseTableFragment : Fragment() {
    /**
     * 该类负责填充课程表格布局，布局文件为src\main\res\layout\fragment_course_table.xml
     * 项目安卓API等级为16，部分过时的函数需要更高版本的API替换
     * 界面目前分为四个部分：
     * (1)顶部showWeek:TextView 封装在一个RelativeLayout中居中显示周数,在createTopTitle()中初始化
     * (2)中间weekTitle负责显示星期几，共8个TextView封装在一个LinearLayout中,在createTopTitle()中初始化
     * (3)左侧课程节次负责显示第几节课，共Settings.coursesOfDay个TextView封装在一个LinearLayout中,在createLeftBar()中初始化
     * (4)右侧课程信息，该部分是一个RecyclerView，与上面的leftBar封装在同一个LinearLayout中，放在界面的下半部分
     * 以上4个View都封装在LinearLayout中
     * 利用动态计算的单元格大小实现类似GridLayout的视图
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_course_table, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //填充布局
        createTopTitle()
        createLeftBar()
        fillTable()
        Settings.popupWindow = BottomSheetPopupWin(requireContext())
    }

    private fun createTopTitle() {
        val t = "第${Settings.curWeek}周"  //此处单纯不想让IDE警告，可合并
        showWeek.text = t

        val calendar = Calendar.getInstance()
        //calendar.firstDayOfWeek = Calendar.MONDAY
        val today = calendar.get(Calendar.DAY_OF_WEEK) - 1  //计算今天是星期几，注意虚拟机默认时区
        val header = listOf(" ", "周一", "周二", "周三", "周四", "周五", "周六", "周日")
        val tmp = (Settings.screenWidth - Settings.barWidth) / Settings.dayOfWeek   //非空白格子的宽度较大
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, Settings.margin, Settings.margin)

        for ((index, str) in header.withIndex()) {
            if (index <= Settings.dayOfWeek) {
                val cell = TextView(context).apply {
                    text = str
                    gravity = Gravity.CENTER_HORIZONTAL
                    textSize = 14.0f
                    layoutParams = params
                    setBackgroundColor(Settings.WhiteSnow)
                    //宽度设置，第一项略窄
                    width = if (index == 0) {
                        Settings.barWidth
                    } else {
                        tmp
                    }
                    if (today == 0 && index == 7) {
                        setTextColor(Settings.DeepSkyBlue)
                    } else if (today == index) {  //高亮周几
                        setTextColor(Settings.DeepSkyBlue)
                    }
                }
                weekTitle.addView(cell)
            }
        }

        //此处重新定义了每个格子的高度：剩余空白部分/(最大每天课程数+3),确定导航栏之后需修改该部分权重
        //此时单元格高度已被固定，但宽度未定
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        val bottomBarHeight = resources.getDimensionPixelSize(resourceId)
        val usedHeight = showWeek.height + weekTitle.height + 4 * Settings.margin + bottomBarHeight
        Settings.cellHeight =
            ((Settings.screenHeight - usedHeight) / (Settings.coursesOfDay + 3)).toInt()

    }

    private fun createLeftBar() {
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, Settings.margin)
        //注意，单元格的高度已被createTopTitle修改
        params.height = Settings.cellHeight
        params.width = Settings.barWidth

        for (i in 1..Settings.coursesOfDay) {
            val cell = TextView(context).apply {
                text = i.toString()
                gravity = Gravity.CENTER
                textSize = 14.0f
                layoutParams = params
                setBackgroundColor(Settings.WhiteSnow)
            }
            leftBar.addView(cell)
        }

        //此处重新定义了每个格子的宽度：剩余空白部分/dayOfWeek
        Settings.cellWidth =
            (Settings.screenWidth - Settings.barWidth - Settings.margin) / Settings.dayOfWeek
    }

    private fun fillTable() {
        val layoutManager = LinearLayoutManager(context)  //RecyclerView布局管理
        val helper = PagerSnapHelper()  //分页助手，每次划动只翻一页，且将该页居中
        val listener = object : RecyclerView.OnScrollListener()  //RecyclerView划动事件监听，负责更新showWeek
        {
            var move = 0
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val child = recyclerView.getChildAt(0)  //获取RecyclerView当前显示的item
                    Settings.showWeek = recyclerView.getChildLayoutPosition(child) + 1

                    val tmp = "第${Settings.showWeek}周"
                    showWeek.text = tmp
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                move = dx  //从右往左划 dx>0 反之dx<0
            }
        }

        // 课程文件载入
        if (Settings.tableAdapter == null) {
            try {
                val input = requireActivity().openFileInput("学生课表查询.json")
                val courses = Parser.parseJson(input)
                Settings.tableAdapter = TableAdapter(courses)
            } catch (e: FileNotFoundException) {
                Settings.tableAdapter = TableAdapter(Settings.emptyList())
            }
        }
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL

        recyclerView.addOnScrollListener(listener)
        recyclerView.layoutManager = layoutManager

        helper.attachToRecyclerView(recyclerView)

        //减少布局的重新加载
        recyclerView.adapter = Settings.tableAdapter

        //启动时默认显示当前周

        layoutManager.scrollToPosition(Settings.curWeek - 1)
    }

}