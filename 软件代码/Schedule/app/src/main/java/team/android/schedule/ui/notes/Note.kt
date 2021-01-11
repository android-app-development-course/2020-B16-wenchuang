package team.android.schedule.ui.notes

import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


data class Note(var type: Int) {
    /**
     * 笔记数据类
     * @property title 笔记的标题，用于列表中的显示
     * @property content 笔记的文字内容
     * @property image 图片的绝对路径，图片在添加时会被复制到应用空间内的files/images文件夹下，
     *                  且会重命名为毫秒时间戳.png
     * @property createTime 笔记创建的时间 yyyy.MM.dd HH:mm
     * @property finalTime 笔记上次修改的时间
     * @property filename 笔记保存的文件名，毫秒时间戳.xml
     * @property curWeek 课表中传递的周数信息
     * @property weekDay 课表传递的星期信息
     * @property className 课表传递的课程名
     */
    var title: String = ""
    var content: String = ""
    var image = ArrayList<String>()

    var createTime: String = getFormattedTimeString() //Date().toLocaleString()

    //    var time: Int = (System.currentTimeMillis() % 10000).toInt()
    var finalTime: String = ""

    var filename: String = System.currentTimeMillis().toString()

    var curWeek: String = ""
    var weekDay: String = ""
    var className: String = ""

    fun getFormattedTimeString(): String {
        return SimpleDateFormat(
            "yyyy.MM.dd HH:mm",
            Locale.getDefault()
        ).format(Date())
    }
}
