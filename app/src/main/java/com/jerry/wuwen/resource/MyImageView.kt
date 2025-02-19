package com.jerry.wuwen.resource

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MyImageView : AppCompatImageView {
    //子线程不能操作UI，通过Handler设置图片
    private val handler_new: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                GET_DATA_SUCCESS -> {
                    val bitmap = msg.obj as Bitmap
                    setImageBitmap(bitmap)
                }
                /*NETWORK_ERROR -> Toast.makeText(
                    context,
                    "网络连接失败",
                    Toast.LENGTH_SHORT
                ).show()*/
                SERVER_ERROR -> Toast.makeText(
                    context,
                    "服务器发生错误",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context!!, attrs, defStyleAttr) {
    }

    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!,
        attrs
    ) {
    }

    //设置网络图片
    fun setImageURL(path: String?) {
        //开启一个线程用于联网
        Log.d("此处使用", "获取String${path}")
        thread {

                try {
                    //把传过来的路径转成URL

                    val url = URL(path)
                    Log.d("此处使用", "获取")
                    //获取连接
                    val connection =
                        url.openConnection() as HttpURLConnection

                    //使用GET方法访问网络
                    connection.requestMethod = "GET"
                    //超时时间为10秒
                    connection.connectTimeout = 10000
                    //获取返回码
                    val code = connection.responseCode
                    if (code == 200) {
                        val inputStream = connection.inputStream
                        //使用工厂把网络的输入流生产Bitmap
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        //利用Message把图片发给Handler
                        val msg = Message.obtain()
                        msg.obj = bitmap
                        msg.what = GET_DATA_SUCCESS
                        handler_new.sendMessage(msg)
                        inputStream.close()
                    } else {
                        //服务启发生错误
                        handler_new.sendEmptyMessage(SERVER_ERROR)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    //网络连接错误
                    handler_new.sendEmptyMessage(NETWORK_ERROR)
                }
            }

    }

    companion object {
        const val GET_DATA_SUCCESS = 1
        const val NETWORK_ERROR = 2
        const val SERVER_ERROR = 3
    }
}