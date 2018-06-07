package com.junerver.baidumapdemo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mTvSelectAddress.setOnClickListener { v->
            startActivityForResult(Intent(this,SelectAddressByMapActivity::class.java),99)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 99 && resultCode == Activity.RESULT_OK) {
            var string:String = data!!.getStringExtra("address")
            mTvSelectAddress.text=string
        } else {
            Toast.makeText(this,"未选择地址",Toast.LENGTH_SHORT).show()
        }
    }
}
