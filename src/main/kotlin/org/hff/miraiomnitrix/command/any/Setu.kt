package org.hff.miraiomnitrix.command.any

import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.ForwardMessageBuilder
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.message.data.toPlainText
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.utils.*

@Command(name = ["涩图", "setu"])
class Setu : AnyCommand {

    private val api1 = "https://api.lolicon.app/setu/v2"
    private val api2 = "https://image.anosu.top/pixiv/json"

    override suspend fun MessageEvent.execute(args: List<String>): Message? {
        var r18 = 0
        var num = 1
        val keywords = mutableListOf<String>()
        args.forEach { arg ->
            when {
                arg.matches("(r|r18)".toRegex(RegexOption.IGNORE_CASE)) -> r18 = 1
                arg.matches("^(n|num)[0-9]+$".toRegex(RegexOption.IGNORE_CASE)) ->
                    num = arg.substringAfter("n", "num").toInt()

                else -> keywords.add(arg)
            }
        }
        val forwardBuilder = ForwardMessageBuilder(subject)

        supervisorScope {
            val tags = keywords.joinToString("&tag=", "&tag=") { it.toUrl() }
            try {
                val json = HttpUtil.getString("$api1?r18=$r18&num=$num$tags")
                JsonUtil.getArray(json, "data").map { it.get("urls").getAsStr("original") }
            } catch (_: Exception) {
                val url = "$api2?r18=$r18&num=$num&keyword=${keywords.joinToString("|").toUrl()}"
                JsonUtil.getArray(HttpUtil.getString(url)).map { it.getAsStr("url") }
            }.forEach { launch { forwardBuilder.add(subject, it) } }
        }

        if (forwardBuilder.size > 0) {
            subject.sendMessage(forwardBuilder.build())
                .run { if (r18 == 1) recallIn(60 * 1000) }
            return null
        }
        return "没有找到符合条件的涩图".toPlainText()
    }

    suspend fun ForwardMessageBuilder.add(subject: Contact, imgUrl: String) {
        val regex = Regex("(?<=/)[^/]*?(?=_\\w+\\.[^.]*\$)")
        val match = regex.find(imgUrl)
        try {
            HttpUtil.getInputStream(imgUrl, isProxy = true).use {
                buildMessageChain {
                    +subject.uploadImage(it)
                    if (match != null) +"\nhttps://www.pixiv.net/artworks/${match.value}"
                }.run(::add)
            }
        } catch (_: Exception) {
        }
    }

}
