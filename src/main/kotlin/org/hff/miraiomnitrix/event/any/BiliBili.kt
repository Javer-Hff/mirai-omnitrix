package org.hff.miraiomnitrix.event.any

import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChainBuilder
import org.hff.miraiomnitrix.config.PermissionProperties
import org.hff.miraiomnitrix.event.*
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.JsonUtil

@Event(priority = 2)
class BiliBili(private val permissionProperties: PermissionProperties) : AnyEvent {

    private val videoUrl = "https://api.bilibili.com/x/web-interface/view"
    private val videoRegex = """(?i)(?<!\w)(?:av(\d+)|(BV1[1-9A-NP-Za-km-z]{9}))""".toRegex()
    override suspend fun handle(args: List<String>, event: MessageEvent): EventResult {
        if (args.isEmpty()) return next()

        val subject = event.subject
        if (permissionProperties.bvExcludeGroup.contains(subject.id)) return next()

        val first = args[0]
        if (first.length > 30 || !(videoRegex matches args[0])) return next()

        val json = HttpUtil.getString(videoUrl, mapOf("bvid" to first))
        val data: BiliVideoInfo = JsonUtil.fromJson(json, "data")
        val share = MessageChainBuilder()
            .append(subject.uploadImage(HttpUtil.getInputStream(data.pic)))
            .append("标题：${data.title}\n")
            .append("简介：${data.desc.takeIf { it.length > 100 }?.take(100) + "……"}\n")
            .append("UP主: ${data.owner.name}\n")
            .append("链接：https://www.bilibili.com/video/$first\n")
            .build()
        return stop(share)
    }

    data class BiliVideoInfo(
        val pic: String,
        val title: String,
        val desc: String,
        val owner: Owner
    )

    data class Owner(val name: String)

}