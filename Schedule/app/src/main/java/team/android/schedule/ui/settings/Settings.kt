package team.android.schedule.ui.settings

import android.graphics.Color
import team.android.schedule.ui.course.TableAdapter


object Settings
/**
 * @author Yehoar
 * 保存设置的单例类，全局共享
 */
{
    //课程相关
    var curWeek = 12
    var maxWeek = 18
    var coursesOfDay = 11

    //显示相关的参数
    var screenWidth = 0
    var screenHeight = 0
    var cellHeight = 0
    var cellWidth = 0
    var showWeek = curWeek - 1
    const val margin = 1

    //暂时使用该adapter做中间变量
    var adapter: TableAdapter? = null

    //表头颜色
    val WhiteSnow = Color.parseColor("#F5F5F5")
    val White = Color.parseColor("#FFFFFFFF")
    val DeepSkyBlue = Color.parseColor("#00BFFF")

    //课程颜色
    val PaleTurquoise1 = Color.parseColor("#BBFFFF")
    val LemonChiffon = Color.parseColor("#FFFACD")
    val Honeydew = Color.parseColor("#F0FFF0")
    val MistyRose = Color.parseColor("#FFE4E1")
    val LightSkyBlue = Color.parseColor("#87CEFA")
    val Wheat = Color.parseColor("#FFE7BA")

    val colors = listOf(PaleTurquoise1, LemonChiffon, Honeydew, MistyRose, LightSkyBlue, Wheat)

    fun setScreenParameter(w: Int, h: Int)
    {
        screenWidth = w
        screenHeight = h

        cellWidth = w / 16
        cellHeight = h / 40
    }
}
