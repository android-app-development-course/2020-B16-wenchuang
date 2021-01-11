package team.android.schedule.ui.settings

import android.graphics.Color
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import team.android.schedule.ui.course.BottomSheetPopupWin
import team.android.schedule.ui.course.Course
import team.android.schedule.ui.course.TableAdapter
import team.android.schedule.ui.notes.NotesAdapter
import java.io.*
import java.util.*


object Settings
/**
 * @author Yehoar
 * 保存设置的单例类，全局共享
 * 该类的作用类似于ViewModel
 *
 * 该类持有一个TableAdapter，缓存课程表
 * 该类持有一个NotesAdapter，用于共享及更新笔记列表
 * 减少重复加载布局，更新数据使用adapter.updateData()
 */
{
    //课程相关
    var weekStart = ""
    var curWeek = 1
    var totalWeek = 18
    var dayOfWeek = 7
    var coursesOfDay = 11

    //显示相关的参数
    var screenWidth = 0
    var screenHeight = 0
    var cellHeight = 0
    var cellWidth = 0
    var barWidth = 0
    var showWeek = curWeek - 1
    const val margin = 1

    //寄存数据，减少重复加载
    var tableAdapter: TableAdapter? = null
    var notesAdapter: NotesAdapter? = null
    var popupWindow: BottomSheetPopupWin? = null

    //表头颜色
    val WhiteSnow = Color.parseColor("#F5F5F5")
    val White = Color.parseColor("#FFFFFFFF")
    val DeepSkyBlue = Color.parseColor("#00BFFF")

    //课程颜色
    val colors = listOf(
        Color.parseColor("#e6f4ff"),
        Color.parseColor("#fdebdd"),
        Color.parseColor("#defbf7"),
        Color.parseColor("#eeedff"),
        Color.parseColor("#fcebcd"),
        Color.parseColor("#ffeff0"),
        Color.parseColor("#eaf2ff"),
        Color.parseColor("#ffeef8"),
        Color.parseColor("#e2f9f3"),
        Color.parseColor("#fff9c9"),
        Color.parseColor("#faedff"),
        Color.parseColor("#f4f2fd")
    )

    fun setScreenParameter(w: Int, h: Int) {
        screenWidth = w
        screenHeight = h

        cellWidth = w / 16
        cellHeight = h / 40
        barWidth = cellWidth
    }

    fun emptyList(): ArrayList<ArrayList<Course>> {
        return ArrayList<ArrayList<Course>>(7).apply {
            for (i in 0..6) {
                add(ArrayList())
            }
        }
    }

    //读取配置文件
    fun load(stream: FileInputStream) {
        val reader = BufferedReader(InputStreamReader(stream))
        val str = reader.readText()
        reader.close()
        if (str.isBlank() || str == "{}") return
        val gType = object : TypeToken<Map<String, String>>() {}.type
        val gson = Gson().fromJson<Map<String, String>>(str, gType)
        weekStart = gson["weekStart"] ?: ""
        totalWeek = gson["totalWeek"]?.toInt() ?: 18
        dayOfWeek = gson["dayOfWeek"]?.toInt() ?: 7
        coursesOfDay = gson["coursesOfDay"]?.toInt() ?: 11

        if (weekStart.isNotBlank()) {
            val calendar = Calendar.getInstance()
            val temp = weekStart.split(",")
            val y = calendar.get(Calendar.YEAR) - temp[0].toInt()
            val w = calendar.get(Calendar.WEEK_OF_YEAR) - temp[1].toInt()
            val t = temp[2].toInt()
            curWeek = when (y) {
                0 -> {// 同一年
                    t + w
                }
                1 -> {//跨年了
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.YEAR, temp[0].toInt())
                    calendar.get(Calendar.WEEK_OF_YEAR) + cal.getActualMaximum(Calendar.WEEK_OF_YEAR) - temp[1].toInt()
                }
                else -> {
                    1
                }
            }
        } else {
            curWeek = 1
        }
    }

    //保存设置
    fun save(stream: FileOutputStream) {
        val writer = BufferedWriter(OutputStreamWriter(stream))
        val map = mapOf(
            "weekStart" to weekStart, "totalWeek" to totalWeek.toString(),
            "dayOfWeek" to dayOfWeek.toString(), "coursesOfDay" to coursesOfDay.toString()
        )
        val gType = object : TypeToken<Map<String, String>>() {}.type
        writer.write(Gson().toJson(map, gType))
        writer.close()
    }
}
