package team.android.schedule.ui.webview

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.webkit.JavascriptInterface
import android.webkit.WebStorage
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_webview.*
import team.android.schedule.R
import team.android.schedule.ui.course.Course
import team.android.schedule.ui.settings.Settings
import java.io.BufferedWriter
import java.io.OutputStreamWriter

class WebActivity : AppCompatActivity()
{
    /**
     * @property Handler js对象
     * 打开一个内嵌的网页，注入js对象，提取课程表的HTML(部分)到本地
     * 直接使用webView.evaluateJavascript(js)返回的字符串是unicode编码，jsoup无法解析
     */
    var htmlHolder = ""

    inner class Handler
    {
        @SuppressLint("JavascriptInterface")
        @JavascriptInterface
        fun load(html: String)
        {
            htmlHolder = html
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface", "AddJavascriptInterface")
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        supportActionBar?.hide()
        WebStorage.getInstance().deleteAllData()
        webView.let {
            it.settings.javaScriptEnabled = true
            it.settings.domStorageEnabled = true
            it.webViewClient = WebViewClient()
            it.loadUrl("https://jwxt.scnu.edu.cn")  //打开初始页面
            it.addJavascriptInterface(Handler(), "handler")  // 注入js对象
        }

        wv_quit.setOnClickListener()  //用户取消导入
        {
//            val intent = Intent()
//            intent.putExtra("", "html")
//            setResult(RESULT_CANCELED, intent)
            finish()
        }
        wv_load.setOnClickListener()  // 开始导入
        {
            val curUrl = webView.url ?: ""
            if (curUrl.startsWith("https://jwxt.scnu.edu.cn/kbcx/xskbcx_cxXskbcxIndex.html"))
            {
                val js =
                        "javascript:window.handler.load(document.getElementById(\"table2\").innerHTML);"
                webView.evaluateJavascript(js) {
                    if (htmlHolder.isNotBlank() || htmlHolder == "null")
                    {
                        val courses = Parser.parseSCNUHtml(htmlHolder)
                        if (courses.size > 0)
                        {
                            val output = openFileOutput("学生课表查询.json", MODE_PRIVATE)
                            Parser.save(output,courses)
                            Settings.tableAdapter?.updateData(courses)
                            Toast.makeText(baseContext, "导入课表成功！", Toast.LENGTH_SHORT).show()
                        } else Toast.makeText(baseContext, "未解析到课程！", Toast.LENGTH_SHORT).show()
                    } else
                    {
                        Toast.makeText(baseContext, "错误：未抓取到数据！", Toast.LENGTH_SHORT).show()
                    }
                }
                finish()
            } else
            {
                Toast.makeText(this, "请到学生课表查询页面后再导入", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack())
        {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}