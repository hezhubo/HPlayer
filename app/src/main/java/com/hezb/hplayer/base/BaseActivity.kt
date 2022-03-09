package com.hezb.hplayer.base

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

/**
 * Project Name: HPlayer
 * File Name:    BaseActivity
 *
 * Description: Activity基类.
 *
 * @author  hezhubo
 * @date    2022年03月02日 21:30
 */
abstract class BaseActivity : AppCompatActivity() {

    private var isRunning: Boolean = false

    override fun onResume() {
        super.onResume()
        isRunning = true
    }

    override fun onPause() {
        super.onPause()
        isRunning = false
    }

    fun getContext(): Context {
        return this
    }

}