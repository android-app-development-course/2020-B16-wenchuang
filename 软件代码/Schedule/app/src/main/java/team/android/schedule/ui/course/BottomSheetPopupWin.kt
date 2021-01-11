package team.android.schedule.ui.course

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import team.android.schedule.R
import team.android.schedule.ui.settings.Settings


@SuppressLint("ClickableViewAccessibility")
class BottomSheetPopupWin(context: Context) : PopupWindow(context) {
    /**
     * BottomSheetPopupWin
     * 修改或添加课程的弹窗，占半屏
     * 布局文件为：layout\popup_bottom_sheet.xml
     * 该类是在CourseTableFragment中实例化的，通过Settings.popupWindow共享
     * Fragment重载时一并重载
     */
    private var bottomSheet: View
    lateinit var tag: Any
    private var courseName: EditText
    private var classroom: EditText
    private var teacher: EditText
    private var dayTime: EditText
    private var weekTime: EditText
    private var btn_sub: Button
    private var btn_add: Button

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        bottomSheet = inflater.inflate(R.layout.popup_bottom_sheet, null)
        this.contentView = bottomSheet
        this.width = ViewGroup.LayoutParams.MATCH_PARENT
        //设置SelectPicPopupWindow弹出窗体的高
        this.height = ViewGroup.LayoutParams.WRAP_CONTENT
        //设置SelectPicPopupWindow弹出窗体可点击
        this.isFocusable = true
        //设置SelectPicPopupWindow弹出窗体动画效果
        this.animationStyle = R.style.AnimBottom
        //实例化一个ColorDrawable颜色为半透明
        val dw = ColorDrawable(0xb0000000.toInt())
        //设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw)

        courseName = bottomSheet.findViewById(R.id.course_name)
        classroom = bottomSheet.findViewById(R.id.classroom)
        teacher = bottomSheet.findViewById(R.id.teacher)
        dayTime = bottomSheet.findViewById(R.id.dayTime)
        weekTime = bottomSheet.findViewById(R.id.weeks)
        btn_add = bottomSheet.findViewById(R.id.btn_add)
        btn_sub = bottomSheet.findViewById(R.id.btn_sub)

        bottomSheet.let {
            //监听点击事件，焦点转移时收起软键盘
            it.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    courseName.clearFocus()
                    classroom.clearFocus()
                    teacher.clearFocus()
                    val im =
                        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    im.hideSoftInputFromWindow(it.windowToken, 0)
                }
                false
            }
        }
        dayTime.isEnabled=false
//        dayTime.addTextChangedListener()
//        {
//            val str = it.toString()
//            if (!str.isBlank() && str.toInt() < 1)
//                dayTime.setText("1")
//        }
        btn_sub.setOnClickListener() {
            var num = dayTime.text.toString().toInt()
            if (num > 1) {
                num -= 1
                dayTime.setText(num.toString())
            }
        }

        btn_add.setOnClickListener() {
            var num = dayTime.text.toString().toInt()
            var max = 0
            var start = 0
            val day = if (tag is String) //${position + 1},$index,$counter
            {
                val info = (tag as String).split(",")
                start = info[2].toInt()
                Settings.tableAdapter?.weekList?.get(info[1].toInt())
            } else {
                val c = tag as Course
                start = c.dayTime[1] + 1
                max += start - c.dayTime[0]
                Settings.tableAdapter?.weekList?.get(c.weekDay - 1)
            }

            day?.let {
                val temp = BooleanArray(Settings.coursesOfDay + 1) { true }
                for (i in it) {
                    val t1 = i.dayTime[0]
                    val t2 = i.dayTime[1]
                    for (t in t1..t2) {
                        temp[t] = false
                    }
                }
                for (i in start..temp.lastIndex) {
                    if (temp[i]) max += 1
                    else break
                }
            }
            if (num < max) {
                num += 1
                dayTime.setText(num.toString())
            }
        }

        bottomSheet.findViewById<TextView>(R.id.bs_quit).setOnClickListener() {
            dismiss()
        }

        bottomSheet.findViewById<TextView>(R.id.bs_complete).setOnClickListener() {
            val name = courseName.text.toString()
            val room = classroom.text.toString()
            val tea = teacher.text.toString()
            val day = dayTime.text.toString()
            val week = weekTime.text.toString()
            //检查必需的信息
            if (name.isBlank()) {
                AlertDialog.Builder(context).let { builder ->
                    builder.setMessage("课程名不能为空！")
                    builder.setPositiveButton("确定", null)
                    builder.show()
                }
                return@setOnClickListener
            }
            if (week.isEmpty()) {
                AlertDialog.Builder(context).let { builder ->
                    builder.setMessage("上课周数不能为空！")
                    builder.setPositiveButton("确定", null)
                    builder.show()
                }
                return@setOnClickListener
            }

            if (day.isEmpty()) {
                AlertDialog.Builder(context).let { builder ->
                    builder.setMessage("上课节次不能为空！")
                    builder.setPositiveButton("确定", null)
                    builder.show()
                }
                return@setOnClickListener
            }

            val re = Regex("[0-9]+")
            val temp = ArrayList<Int>()
            val course: Course
            for (str in re.findAll(week)) {
                temp.add(str.value.toInt())
            }

            if (tag is Course) {
                course = tag as Course
                course.name = name
                course.dayTime[1] = day.toInt() + course.dayTime[0] - 1
            } else {
                course = Course(name)
                val info = (tag as String).split(",")
                course.dayTime.add(info[2].toInt())
                course.dayTime.add(day.toInt() + info[2].toInt() - 1)
                course.weekDay = info[1].toInt() + 1
                Settings.tableAdapter?.weekList?.get(info[1].toInt())?.add(course)
            }
            course.classroom = room
            course.teacher = tea
            course.weekTime = temp
            Settings.tableAdapter?.notifyDataSetChanged()
            dismiss()
        }

    }

    fun showAtLocation(tag: Any) {
        //接收TextView中的tag用以填充信息
        super.showAtLocation(contentView, Gravity.BOTTOM, 10, 10)
        courseName.setText("")
        classroom.setText("")
        teacher.setText("")
        dayTime.setText("1")
        weekTime.setText("1-${Settings.totalWeek}")

        this.tag = tag

        if (tag is Course) {
            courseName.setText(tag.name)
            classroom.setText(tag.classroom)
            teacher.setText(tag.teacher)
            val t = tag.dayTime[1] - tag.dayTime[0] + 1
            dayTime.setText(t.toString())
            var t2 = "${tag.weekTime[0]}-${tag.weekTime[1]}"
            var t3 = 2
            while (t3 < tag.weekTime.lastIndex) {
                t2 += ",${tag.weekTime[t3]}-${tag.weekTime[t3 + 1]}"
                t3 += 2
            }
            weekTime.setText(t2)

        }

    }

}