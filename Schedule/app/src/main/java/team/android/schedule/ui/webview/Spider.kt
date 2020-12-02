package team.android.schedule.ui.webview

import org.jsoup.Jsoup
import team.android.schedule.ui.course.Course
import team.android.schedule.ui.settings.Settings

class Spider
/**
 * @author Yehoar
 * 从HTML解析课程表，目前测试仅支持华师教务网，需要Jsoup
 * 在build.gradle的dependencies内加入
 * implementation group: 'org.jsoup', name: 'jsoup', version: '1.13.1'
 * 目前读取的是本地HTML文件，tmp\学生课表查询.html，首次使用需要将文件放到虚拟机的
 * /data/data/<team.android.schedule><记得替换包名>/files
 */
{

    private val keys = listOf(
        " ",
        "Monday",
        "Tuesday",
        "Wednesday",
        "Thursday",
        "Friday",
        "Saturday",
        "Sunday"
    )

    fun fromSCNU(html: String): ArrayList<Map<String, ArrayList<Course>>>
    {
        val courses = HashMap<String, List<Course>>()
        val document = Jsoup.parse(html)
        for (i in 1..7)
        {
            val day = document.getElementById("xq_$i")  //星期i的课
            val dayTime = day.getElementsByClass("festival")  //上课节次
            val detail = day.getElementsByClass("timetable_con text-left")  //课程信息
            val dayList = ArrayList<Course>()
            for ((index, item) in dayTime.withIndex())
            {
                val tmp = detail[index].getElementsByTag("font")
                val course = handleSCNU(
                    tmp[0].text(),  //name
                    tmp[2].text(),  //classroom
                    tmp[3].text(),  //teacher
                    item.text(),  //dayTime
                    tmp[1].text()  //weekTime
                )
                dayList.add(course)
            }
            courses[keys[i]] = dayList

        }
        return map2List(courses)
    }

    private fun handleSCNU(
        name: String,
        classroom: String,
        teacher: String,
        dayTime: String,
        weekTime: String
    ): Course
    {
        val course = Course(name)
        course.classroom = classroom.substring(classroom.lastIndexOf('：') + 1)
        course.teacher = teacher.substring(teacher.lastIndexOf("：") + 1)
        val re = Regex("[0-9]+")
        for (str in re.findAll(dayTime))
        {
            course.dayTime.add(str.value.toInt())
        }
        for (str in re.findAll(weekTime))
        {
            course.weekTime.add(str.value.toInt())
        }

        return course
    }

    private fun map2List(courses: Map<String, List<Course>>): ArrayList<Map<String, ArrayList<Course>>>
    {
        val weekList = ArrayList<Map<String, ArrayList<Course>>>()
        for (i in 0 until Settings.maxWeek)
        {
            val map = HashMap<String, ArrayList<Course>>()
            for (key in keys)
            {
                map[key] = ArrayList()
            }
            weekList.add(map)
        }

        for ((key, value) in courses)
        {
            for (course in value)
            {
                for (week in 1..Settings.maxWeek)
                {
                    var t = 0
                    while (t < course.weekTime.lastIndex)
                    {
                        if (week >= course.weekTime[t] && week <= course.weekTime[t + 1])
                        {
                            weekList[week - 1][key]?.add(course)
                            break
                        }
                        t += 2
                    }
                }
            }
        }
        return weekList
    }
}

