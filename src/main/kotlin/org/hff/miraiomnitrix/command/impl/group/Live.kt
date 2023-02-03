package org.hff.miraiomnitrix.command.impl.group

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.getMember
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.app.entity.Live
import org.hff.miraiomnitrix.app.service.LiveService
import org.hff.miraiomnitrix.command.core.Command
import org.hff.miraiomnitrix.command.type.GroupCommand
import org.hff.miraiomnitrix.result.ResultMessage
import org.hff.miraiomnitrix.result.result
import org.hff.miraiomnitrix.utils.Util.getBilibiliUserInfo
import org.hff.miraiomnitrix.utils.Util.getQq

@Command(name = ["直播", "live"])
class Live(private val liveService: LiveService) : GroupCommand {

    override suspend fun execute(
        sender: Member,
        message: MessageChain,
        group: Group,
        args: List<String>
    ): ResultMessage? {
        if (args.isEmpty()) {
            val list = liveService.ktQuery().eq(Live::groupId, group.id).list()
            if (list.isEmpty()) return result("尚未添加主播/n使用live help指令获取使用方法")

            val liveStates = mutableListOf<String>()
            coroutineScope {
                list.forEach {
                    val member = group.getMember(it.qq) ?: return@forEach
                    launch {
                        val liveState = getLiveState(it.uid) ?: return@launch
                        liveStates.add(member.nick + "：" + liveState)
                    }
                }
            }
            if (liveStates.isNotEmpty()) return result("当前正在直播：\n" + liveStates.joinToString("\n"))
            return result("当前没有人在直播")
        }

        when (args[0]) {
            "帮助", "help" -> {}
            "添加", "add", "save" -> {
                if (args.size < 3) return result("参数错误")
                val qq = getQq(args[1])
                val member = group.getMember(qq) ?: return result("未找到成员")
                val count = liveService.ktQuery().eq(Live::qq, qq).eq(Live::groupId, group.id).count()
                if (count != 0L) return result("人员重复")
                val uid = args[2].toLong()
                val userInfo = getBilibiliUserInfo(uid)
                val live = userInfo.live_room?.let { Live(null, qq, group.id, uid, it.roomid) }
                    ?: return result("未找到直播间信息")
                val save = liveService.save(live)
                if (!save) return result("保存失败")
                return result("已添加${member.nameCardOrNick}的数据")
            }
//            "订阅", "subscribe" -> {}
//            "更新", "update" -> {}
            "移除", "del", "remove" -> {
                if (args.size < 2) return result("参数错误")
                val qq = getQq(args[1])
                val member = group.getMember(qq) ?: return result("未找到成员")
                val remove = liveService.removeById(qq)
                if (!remove) return result("删除失败")
                return result("已添加${member.nameCardOrNick}的数据")
            }
        }
        return null
    }

    suspend fun getLiveState(uid: Long): String? {
        val userInfo = getBilibiliUserInfo(uid)
        val (liveStatus, roomStatus, _, title, url) = userInfo.live_room ?: return null
        if (liveStatus != 1 || roomStatus != 1) return null
        val newUrl = removeStr(url)
        return "[$title] $newUrl"
    }

    private fun removeStr(str: String): String {
        val index = str.indexOf("?")
        return if (index > 0) {
            str.slice(0 until index)
        } else str
    }
}