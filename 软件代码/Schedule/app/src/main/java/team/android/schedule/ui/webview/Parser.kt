package team.android.schedule.ui.webview

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.jsoup.Jsoup
import team.android.schedule.ui.course.Course
import team.android.schedule.ui.settings.Settings
import java.io.*

object Parser
/**
 * 从HTML解析课程表，目前测试仅支持华师教务网，需要Jsoup
 * 在build.gradle的dependencies内加入
 * implementation group: 'org.jsoup', name: 'jsoup', version: '1.13.1'
 * 目前读取的是本地HTML文件，tmp\学生课表查询.html，首次使用需要将文件放到虚拟机的
 * /data/data/<team.android.schedule><记得替换包名>/files
 *
 * 目前将数据以json形式存储，通过Gson库进行序列化与反序列化
 */
{
    fun parseJson(stream: FileInputStream?): ArrayList<ArrayList<Course>> {
        /**
         * 反序列化Json数据，该函数会自动关闭stream
         */
        val reader = BufferedReader(InputStreamReader(stream))
        val gType =
            object : TypeToken<ArrayList<ArrayList<Course>>>() {}.type  //帮助Gson确定数据结构，便于反序列化
        val json = reader.readText()
        reader.close()
        return Gson().fromJson(json, gType)
    }

    fun parseSCNUHtml(html: String): ArrayList<ArrayList<Course>> {
        /**
         * 从Html文本中解析课程表，返回一周七天的全部课程
         */
        val courses = ArrayList<ArrayList<Course>>()
        val document = Jsoup.parse(html)

        for (i in 1..7) {
            val day = document?.getElementById("xq_$i")  //星期i的课
            val dayTime = day?.getElementsByClass("festival")  //上课节次
            val detail = day?.getElementsByClass("timetable_con text-left")  //课程信息

            if (dayTime == null || detail == null) continue

            val dayList = ArrayList<Course>()
            for ((index, item) in dayTime.withIndex()) {
                val tmp = detail[index].getElementsByTag("font")
                val course = toSCNUCourse(
                    tmp[0].text(),  //name
                    tmp[2].text(),  //classroom
                    tmp[3].text(),  //teacher
                    item.text(),  //dayTime
                    tmp[1].text(),  //weekTime
                    i
                )
                dayList.add(course)
            }
            courses.add(dayList)

        }
        return if (courses.size >= 5)
            courses
        else
            Settings.emptyList()
    }

    private fun toSCNUCourse(
        name: String, classroom: String, teacher: String, dayTime: String,
        weekTime: String, weekDay: Int
    ): Course {
        /**
         * 封装课程数据
         */
        val course = Course(name)
        course.classroom = classroom.substring(classroom.lastIndexOf('：') + 1)
        course.teacher = teacher.substring(teacher.lastIndexOf("：") + 1)
        val re = Regex("[0-9]+")
        for (str in re.findAll(dayTime)) {
            course.dayTime.add(str.value.toInt())
        }
        for (str in re.findAll(weekTime)) {
            course.weekTime.add(str.value.toInt())
        }
        course.weekDay = weekDay
        return course
    }

    fun save(stream: FileOutputStream, courses: ArrayList<ArrayList<Course>>?) {
        /**
         * 保存课程数据，保证会关闭stream
         */
        if (courses != null) {
            val writer = BufferedWriter(OutputStreamWriter(stream))
            val gType =
                object : TypeToken<ArrayList<ArrayList<Course>>>() {}.type  //帮助Gson确定数据结构，便于反序列化
            writer.write(Gson().toJson(courses, gType))
            writer.close()
        } else stream.close()

    }

}

