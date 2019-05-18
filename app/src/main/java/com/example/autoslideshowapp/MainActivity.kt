package com.example.autoslideshowapp

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100
    private var cursor: Cursor? = null
    private var mTimer: Timer? = null
    private var mTimerSec = 0.0
    private var mHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                getContentsInfo()
            } else {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
        } else {
            getContentsInfo()
        }

        start_button.setOnClickListener {
            if (!cursor!!.moveToNext()) {
                cursor!!.moveToFirst()
                setImageView()
            } else {
                cursor!!.moveToNext()
                setImageView()
            }
        }

        return_button.setOnClickListener {
            if (!cursor!!.moveToPrevious()) {
                cursor!!.moveToLast()
                setImageView()
            } else {
                cursor!!.moveToPrevious()
                setImageView()
            }
        }

        slideShow_button.setOnClickListener {
            if (mTimer == null) {
                mTimer = Timer()
                mTimer!!.schedule(object: TimerTask() {
                    override fun run() {
                        mTimerSec += 2000
                        mHandler.post {
                            if (!cursor!!.moveToNext()) {
                                cursor!!.moveToFirst()
                                setImageView()
                            } else {
                                cursor!!.moveToNext()
                                setImageView()
                            }
                        }
                    }
                }, 2000, 2000)
                start_button.isClickable = false
                return_button.isClickable = false
                slideShow_button.text = "停止"
            } else {
                mTimer!!.cancel()
                mTimer = null
                slideShow_button.text = "再生"
                start_button.isClickable = true
                return_button.isClickable = true
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                } else {
                    imageView.setImageResource(R.drawable.img)
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cursor!!.close()
    }

    private fun getContentsInfo() {
        val resolver = contentResolver
        cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            null
        )

        if (cursor!!.moveToFirst()) {
            setImageView()
        }
    }

    private fun setImageView() {
        val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
        val id = cursor!!.getLong(fieldIndex)
        val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

        imageView.setImageURI(imageUri)
    }
}
