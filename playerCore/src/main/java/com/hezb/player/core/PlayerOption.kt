package com.hezb.player.core

/**
 * Project Name: HPlayer
 * File Name:    PlayerOption
 *
 * Description: 播放器配置项.
 *
 * @author  hezhubo
 * @date    2022年03月02日 15:31
 */
class PlayerOption {

    val category: Int
    val name: String
    val value: Any

    constructor(category: Int, name: String, value: Long) {
        this.category = category
        this.name = name
        this.value = value
    }

    constructor(category: Int, name: String, value: String) {
        this.category = category
        this.name = name
        this.value = value
    }

}