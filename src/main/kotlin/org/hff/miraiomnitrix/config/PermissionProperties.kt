package org.hff.miraiomnitrix.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("permission")
data class PermissionProperties(
    /** 管理员qq号 */
    val admin: List<Long>,
    /** 排除复读功能的群号 */
    val repeatExcludeGroup: List<Long>,
    /** 能使用聊天机器人的群号 */
    val chatIncludeGroup: List<Long>,
    /** 能使用bv解析的群号 */
    val bvIncludeGroup: List<Long>
)
