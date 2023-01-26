package org.hff.miraiomnitrix.utils

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Member

object Util {
    lateinit var bot: Bot

    private const val QQ_URL = "https://q1.qlogo.cn/g?b=qq&s=640&nk="
    private const val BILIBILI_INFO_API = "https://api.bilibili.com/x/space/wbi/acc/info?mid="

    fun getQq(args: List<String>) = args.find { it.startsWith("@") }?.substring(1)?.toLong()

    fun getQq(args: List<String>, sender: Member) = getQq(args) ?: sender.id

    fun getQqImg(args: List<String>) = HttpUtil.getInputStream(QQ_URL + getQq(args))

    fun getQqImg(args: List<String>, sender: Member) = HttpUtil.getInputStream(QQ_URL + getQq(args, sender))

    fun getBilibiliUserInfo(uid: Long): UserInfo {
        val apiResult = HttpUtil.getString(BILIBILI_INFO_API + uid)
        return JsonUtil.fromJson(apiResult, "data", UserInfo::class)
    }

    data class UserInfo(val name: String, val live_room: LiveRoom?)

    data class LiveRoom(
        val liveStatus: Int,
        val roomStatus: Int,
        val roomid: Int,
        val title: String,
        val url: String
    )
}