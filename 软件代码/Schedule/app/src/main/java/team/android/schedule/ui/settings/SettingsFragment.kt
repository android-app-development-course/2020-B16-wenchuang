package team.android.schedule.ui.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_settings.*
import team.android.schedule.R
import team.android.schedule.ui.webview.WebActivity
import java.io.File
import java.util.*

class SettingsFragment : Fragment() {
    /**
     * @author Yehoar
     * 设置界面
     * 控件都在fragment_settings.xml文件内定义
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

//        btn_login.setOnClickListener() {
//            val intent = Intent(context, LoginActivity::class.java)
//            startActivity(intent)
//        }

        btn_openWebView.setOnClickListener() {
            val intent = Intent(context, WebActivity::class.java)
            startActivity(intent)
        }

        curWeek.let {
            it.setText(Settings.curWeek.toString())
            it.setOnFocusChangeListener() { _, hasFocus ->
                if (!hasFocus) {
                    val tag = it.text.toString().toInt()
                    if (tag <= Settings.totalWeek) {
                        Settings.curWeek = tag
                        val calendar = Calendar.getInstance()
                        val year = calendar.get(Calendar.YEAR)
                        val week = calendar.get(Calendar.WEEK_OF_YEAR)
                        Settings.weekStart = "$year,$week,$tag"
                    } else {
                        it.setText(Settings.curWeek.toString())
                    }
                }
            }
        }

        totalWeek.let {
            it.setText(Settings.totalWeek.toString())
            it.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    val tmp = it.text.toString().toInt()
                    Settings.totalWeek = tmp
                    if (Settings.curWeek > tmp) {
                        Settings.curWeek = tmp
                    }
                }
            }
        }

        showWeekend.let {
            it.isChecked = Settings.dayOfWeek == 7
            it.setOnCheckedChangeListener { _, isChecked ->
                Settings.dayOfWeek = if (isChecked) 7 else 5
            }
        }

        clearTable.setOnClickListener() {
            AlertDialog.Builder(requireContext()).let {
                it.setMessage("确定要清空课表?")
                it.setPositiveButton("确定") { _, _ ->
                    Settings.tableAdapter?.updateData(Settings.emptyList())
                    Settings.curWeek = 1
                    val file = File("${context?.filesDir?.path}/学生课表查询.json")
                    if (file.exists()) file.delete()
                    Toast.makeText(context, "课表已清空", Toast.LENGTH_SHORT).show()
                }
                it.setNegativeButton("取消", null)
                it.show()
            }
        }
        view?.let {
            it.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    curWeek.clearFocus()
                    totalWeek.clearFocus()
                    val im =
                        requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    im.hideSoftInputFromWindow(it.windowToken, 0)
                }
                false
            }
        }
    }


}