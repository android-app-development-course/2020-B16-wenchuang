package team.android.schedule

import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import team.android.schedule.ui.settings.Settings
import team.android.schedule.ui.webview.Parser
import java.io.FileNotFoundException

class MainLayoutActivity : AppCompatActivity()
{
    /**
     * 主界面
     * 启动时加载设置文件和课程表文件，设置屏幕大小
     * 暂停时保存文件
     */

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)
        supportActionBar?.hide()

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
                setOf(R.id.navigation_course, R.id.navigation_notes, R.id.navigation_settings))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        initial()
    }

    override fun onPause()
    {
        super.onPause()
        aftermath()
    }

    private fun initial()
    {
        try
        {
            val dm = DisplayMetrics()
            windowManager?.defaultDisplay?.getMetrics(dm)  //替换需要API30的 display
            Settings.setScreenParameter(dm.widthPixels, dm.heightPixels)
            val input = openFileInput("settings.json")
            Settings.load(input)
        } catch (e: FileNotFoundException)
        {
            Log.v("FileNotFoundException", "can not find settings.json")
        }
    }

    private fun aftermath()
    {
        /**
         * 善后
         */
        Settings.save(openFileOutput("settings.json", MODE_PRIVATE))
        Parser.save(openFileOutput("学生课表查询.json", MODE_PRIVATE),Settings.tableAdapter?.weekList)
    }

}
