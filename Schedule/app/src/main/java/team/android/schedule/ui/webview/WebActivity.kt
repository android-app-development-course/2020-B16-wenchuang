package team.android.schedule.ui.webview

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.webkit.JavascriptInterface
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_webview.*
import team.android.schedule.R
import team.android.schedule.ui.settings.Settings
import java.io.BufferedWriter
import java.io.OutputStreamWriter

class WebActivity : AppCompatActivity()
{
    /**
     * @author Yehoar
     * @property Handler js对象
     * 打开一个内嵌的网页，注入js对象，获取课程表的HTML(部分)到本地
     */

    var htmlHolder = ""

    inner class Handler
    {
        @SuppressLint("JavascriptInterface")
        @JavascriptInterface
        fun load(html: String)
        {
            if (html.isNotEmpty())
            {
                val output = openFileOutput("学生课表查询.html", MODE_PRIVATE)
                val writer = BufferedWriter(OutputStreamWriter(output))
                htmlHolder = html
                writer.write(html)
                writer.close()
                val intent = Intent()
                intent.putExtra("html", htmlHolder)
                setResult(RESULT_OK, intent)
                finish()
            } else
            {
                Toast.makeText(baseContext, "错误：未抓取到数据！", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface", "AddJavascriptInterface")
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        webView.clearCache(true)  //尝试清理缓存，但似乎未生效
        webView.loadUrl("https://jwxt.scnu.edu.cn/xtgl/login_slogin.html")  //打开初始页面
        webView.addJavascriptInterface(Handler(), "handler")  // 注入js对象

        btn_load.height = Settings.screenHeight / 20  //初始化按钮
        btn_quit.height = Settings.screenHeight / 20

        btn_quit.setOnClickListener()  //用户取消导入
        {
            val intent = Intent()
            intent.putExtra("", "html")
            setResult(RESULT_CANCELED, intent)
            finish()
        }
        btn_load.setOnClickListener()  // 开始导入
        {
            val curUrl = webView.url ?: ""
            if (curUrl.startsWith("https://jwxt.scnu.edu.cn/kbcx/xskbcx_cxXskbcxIndex.html"))
            {
                val js =
                    "javascript:window.handler.load(document.getElementById(\"table2\").innerHTML);"
                webView.evaluateJavascript(js) { Log.v("webView", "js run") }
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