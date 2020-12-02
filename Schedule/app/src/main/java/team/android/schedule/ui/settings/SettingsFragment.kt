package team.android.schedule.ui.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_settings.*
import team.android.schedule.R
import team.android.schedule.ui.login.LoginActivity
import team.android.schedule.ui.webview.Spider
import team.android.schedule.ui.webview.WebActivity

/**
 * A fragment representing a list of Items.
 */
class SettingsFragment : Fragment()
{

    private var columnCount = 1

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Set the adapter
        //        if (view is RecyclerView) {
        //            with(view) {
        //                layoutManager = when {
        //                    columnCount <= 1 -> LinearLayoutManager(context)
        //                    else -> GridLayoutManager(context, columnCount)
        //                }
        //                adapter = SettingsAdapter(DummyContent.ITEMS)
        //            }
        //        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        btn_login.setOnClickListener() {
            val intent = Intent(context, LoginActivity::class.java)
            startActivity(intent)

        }
        btn_openWebView.setOnClickListener() {
            val intent = Intent(context, WebActivity::class.java)
            startActivityForResult(intent, 1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode)
        {
            1 -> if (resultCode == AppCompatActivity.RESULT_OK)
            {
                val html = data?.getStringExtra("html") ?: ""
                val courses = Spider().fromSCNU(html)
                Settings.adapter?.replaceData(courses) ?: Log.e("Setting.adapter:", "未被初始化")
                if (html.isNotEmpty()) Toast.makeText(context, "导入课表成功！", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object
    {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) = SettingsFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_COLUMN_COUNT, columnCount)
            }
        }
    }


}