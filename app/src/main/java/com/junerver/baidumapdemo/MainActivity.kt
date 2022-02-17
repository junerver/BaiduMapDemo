package com.junerver.baidumapdemo

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    lateinit var mContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mContext = this
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        mTvSelectAddress.setOnClickListener { v ->

            PermissionsUtils.getInstance()
                .checkPermissions(this, permissions, object : PermissionsUtils.PermissionsResult {
                    override fun passPermission() {
                        startActivityForResult(
                            Intent(
                                mContext,
                                SelectAddressByMapActivity::class.java
                            ), 99
                        )
                    }

                    override fun continuePermission() {
                        toast("读写权限被拒绝")
                    }

                    override fun refusePermission() {
                        toast("读写权限被拒绝")
                    }
                })

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 99 && resultCode == Activity.RESULT_OK) {
            val string: String? = data?.getStringExtra("address")
            string?.let {
                mTvSelectAddress.text = it
            }
        } else {
            Toast.makeText(this, "未选择地址", Toast.LENGTH_SHORT).show()
        }
    }
}
