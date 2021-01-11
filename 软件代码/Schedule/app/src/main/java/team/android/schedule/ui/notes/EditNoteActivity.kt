package team.android.schedule.ui.notes

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_note_edit.*
import team.android.schedule.R
import team.android.schedule.ui.settings.Settings
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException


class EditNoteActivity : AppCompatActivity() {
    /**
     * 笔记的详情/编辑界面，主要通过StartActivity和Intent传参进行调用
     * 笔记功能需要读写外部资源文件的权限
     * Intent参数：
     * Action:Create 从笔记界面的悬浮按钮触发的添加笔记事件
     * Action:Load 从笔记界面点击标题进行笔记的显示，需要附带filename参数
     * Action:AddByCourse 从课程界面悬浮菜单触发的添加笔记事件，需要附带curWeek，weekDay，className参数
     * 当用户保存笔记时，Note内的信息会以键值对的信息储存到SharePreferences中，
     * 文件名为创建时间戳.xml，同时会在index.xml中添加<文件名，标题>作为索引
     */
    private lateinit var note: Note
    private var hasChange = false
    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            stateChange(true)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_edit)
        supportActionBar?.hide()
        note_edit_header.visibility = View.GONE

        imageAdd.setOnClickListener {
            val intent = Intent("android.intent.action.GET_CONTENT")
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

        saveButton.setOnClickListener {
            saveNote()
            val temp = "上次修改时间：${note.finalTime}"
            editTime.text = temp
            stateChange(false)
        }

        note_quit.setOnClickListener {
            confirm()
        }
        editTitle.addTextChangedListener(textWatcher)
        editText.addTextChangedListener(textWatcher)
        onActivityCreated()
    }

    override fun onBackPressed() {
        confirm()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                //image_load(uri)
                val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
                if (bitmap != null) {
                    val folder = File("${filesDir.absolutePath}/images")
                    val path =
                        "${filesDir.absolutePath}/images/${System.currentTimeMillis()}.png"
                    if (!folder.exists() || !folder.isDirectory) {
                        folder.mkdirs()
                    }
                    val out = FileOutputStream(path)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    out.flush()
                    out.close()
                    note.image.add(path)
                    createImageView(bitmap, path)
                    stateChange(true)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    doSomething()
                } else {
                    Toast.makeText(this, "未授权访问本地文件！", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun onActivityCreated() {
        if (ContextCompat.checkSelfPermission(
                baseContext,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1
            )
        } else {
            doSomething()
        }

    }

    private fun doSomething() {
        when (intent.getStringExtra("Action")) {
            "Create" -> {
                note = Note(1)
            }
            "Load" -> {
                loadFromSharedPreferences(intent.getStringExtra("filename").toString())
                showNote()
                stateChange(false)
            }

            "AddByCourse" -> {
                note = Note(1).apply {
                    curWeek = intent.getStringExtra("curWeek").toString() //当前周
                    weekDay = intent.getStringExtra("weekDay").toString() //星期
                    className = intent.getStringExtra("className").toString() //课程
                }
                showNote()
            }

        }
    }

    private fun showNote() {
        editTitle.setText(note.title)
        val temp = "上次修改时间：${note.finalTime}"
        editTime.text = temp
        for (img in note.image) {
            val input = FileInputStream(img)
            val bitmap = BitmapFactory.decodeStream(input)
            input.close()
            createImageView(bitmap, img)
        }
        val str = StringBuilder().apply {
            if (note.curWeek.isNotBlank() && !note.content.contains("周数：${note.curWeek}\n")) {
                append("周数：")
                append(note.curWeek)
                append("\n")
            }
            if (note.className.isNotBlank() && !note.content.contains("课程：${note.className}\n")) {
                append("课程：")
                append(note.className)
                append("\n")
            }
            if (note.content.isNotBlank()) {
                append(note.content)
            }
        }
        editText.setText(str.toString())
    }

    private fun confirm() {
        if (hasChange) {
            AlertDialog.Builder(this).let {
                it.setMessage("是否保存？")
                it.setPositiveButton("确定") { _, _ ->
                    saveNote()
                    stateChange(false)
                    Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
                }
                it.setNeutralButton("不保存") { _, _ ->
                    finish()
                }
                it.setNegativeButton("取消", null)
                it.show()
            }
        } else {
            finish()
        }
    }

    private fun stateChange(flag: Boolean) {
        hasChange = flag
        note_edit_header.visibility = if (flag) View.VISIBLE else View.GONE
    }

    private fun createImageView(bitmap: Bitmap, path: String) {
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val imView = ImageView(this).apply {
            layoutParams = params
            setOnLongClickListener {
                AlertDialog.Builder(context).let {
                    it.setMessage("是否删除图片？")
                    it.setPositiveButton("确定") { _, _ ->
                        note_image_view.removeView(this)
                        note.image.remove(path)
                        stateChange(true)
                        val file = File(path)
                        if (file.exists())
                            file.delete()
                        Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show()
                    }
                    it.setNegativeButton("取消", null)
                    it.show()
                }
                false
            }
        }

        imView.setImageBitmap(bitmap)
        note_image_view.addView(imView)

    }

    private fun loadFromSharedPreferences(fileName: String) {

        val prefs = getSharedPreferences(fileName, Context.MODE_PRIVATE)
        note = Note(1).apply {
            filename = prefs.getString("filename", "").toString()
            title = prefs.getString("title", "").toString()
            content = prefs.getString("content", "").toString()
            createTime = prefs.getString("createTime", "").toString()
            finalTime = prefs.getString("finalTime", "").toString()
            curWeek = prefs.getString("curWeek", "").toString()
            weekDay = prefs.getString("weekDay", "").toString()
            className = prefs.getString("className", "").toString()
            val img = prefs.getString("images", "").toString().split("\n")
            for (i in img) {
                if (i.isNotBlank()) {
                    image.add(i)
                }
            }
        }

    }

    private fun saveNote() {
        try {

            note.title = editTitle.text.toString()
            note.content = editText.text.toString()
            note.finalTime = note.getFormattedTimeString()
            if (note.title.isBlank())
                note.title = "无标题"

            val editor = getSharedPreferences(note.filename, Context.MODE_PRIVATE).edit()
            editor.let {
                it.putString("filename", note.filename)
                it.putString("title", note.title)
                it.putString("content", note.content)
                val img = StringBuilder().apply {
                    for (i in note.image) {
                        append(i)
                        append("\n")
                    }
                    if (this.lastIndex > 0)
                        deleteCharAt(this.lastIndex)
                }
                it.putString("images", img.toString())
                it.putString("createTime", note.createTime)
                it.putString("finalTime", note.finalTime)
                it.putString("curWeek", note.curWeek)
                it.putString("className", note.className)
                it.putString("weekDay", note.weekDay)
            }
            editor.apply()

            Settings.notesAdapter?.let {
                if (it.map[note.filename] != note.title) {
                    it.map[note.filename] = note.title
                    val index = getSharedPreferences("index", MODE_PRIVATE).edit()
                    index.clear()
                    for (item in it.map) {
                        index.putString(item.key, item.value)
                    }
                    index.apply()
                }
                it.updateData()
            }
            Toast.makeText(this, "已保存", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


}