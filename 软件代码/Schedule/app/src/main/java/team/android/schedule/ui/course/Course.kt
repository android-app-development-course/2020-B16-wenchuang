package team.android.schedule.ui.course

import team.android.schedule.ui.settings.Settings

data class Course(var name: String) : Comparable<Course>
/**
 * 课程数据类
 * @param name 课程名，必需
 * @property classroom 上课地点
 * @property teacher 教师
 * @property dayTime 该课程每天上课的时间，成对使用[开始，结束]，必需
 * @property weekTime 该课程上课的周次，成对使用[开始，结束]，必需
 * @property weekDay 在星期几上课
 * @property color 该课程在课程表显示的颜色，创建后同一门课颜色一样，初始化时随机确定
 */
{
    var classroom = ""
    var teacher = ""
    var dayTime = ArrayList<Int>()
    var weekTime = ArrayList<Int>()
    var weekDay = 0
    var color = 0

    init
    {
        val random = (0..Settings.colors.lastIndex).shuffled().first()
        color = Settings.colors[random]
    }

    override fun toString(): String
    {
        return "$name\n$teacher\n$classroom"
    }

    override fun compareTo(other: Course): Int
    {
        return when
        {
            this.dayTime[0] < other.dayTime[0] -> -1
            this.dayTime[0] == other.dayTime[0] -> 0
            else -> 1
        }

    }

}


