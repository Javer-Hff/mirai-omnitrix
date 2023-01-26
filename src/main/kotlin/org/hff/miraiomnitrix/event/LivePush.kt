package org.hff.miraiomnitrix.event

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.hff.miraiomnitrix.app.service.LiveService
import org.hff.miraiomnitrix.utils.bot
import org.hff.miraiomnitrix.utils.getBilibiliUserInfo
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class LivePush(val liveService: LiveService) : CommandLineRunner {

    val livesCache = mutableMapOf<Long, Int>()

    override fun run(vararg args: String?) {
        runBlocking {
            delay(10 * 1000)
            while (true) {
                val lives = liveService.list()
                lives.forEach {
                    val cache = livesCache[it.qq]
                    val userInfo = getBilibiliUserInfo(it.uid)
                    val liveRoom = userInfo.live_room ?: return@forEach
                    if (cache != null) {
                        val group = bot.groups[it.groupId] ?: return@forEach
                        if (cache == 0 && liveRoom.liveStatus == 1) {
                            group.sendMessage("${userInfo.name}开始直播，地址:${liveRoom.url}")
                        }
                    }
                    livesCache[it.qq] = liveRoom.liveStatus
                }
                delay(10 * 1000)
            }
        }
    }
}