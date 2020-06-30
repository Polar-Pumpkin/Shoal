package net.shoal.sir.voteup.api

import net.shoal.sir.voteup.config.ConfPath
import net.shoal.sir.voteup.data.Vote
import net.shoal.sir.voteup.enums.Msg
import org.bukkit.ChatColor
import org.bukkit.inventory.ItemStack
import org.serverct.parrot.parrotx.PPlugin
import org.serverct.parrot.parrotx.utils.EnumUtil
import org.serverct.parrot.parrotx.utils.I18n
import org.serverct.parrot.parrotx.utils.TimeUtil
import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.regex.Matcher
import java.util.regex.Pattern

object VoteUpPlaceholder {
    private val PLACEHOLDER_PATTERN = Pattern.compile("[%]([^%]+)[%]")
    fun parse(vote: Vote?, text: String?): String? {
        var text = text ?: return null
        val m = PLACEHOLDER_PATTERN.matcher(text)
        while (m.find()) {
            val format = m.group(1)
            val index = format.indexOf("_")
            if (index >= format.length) continue
            val identifier = EnumUtil.valueOf(Vote.Data::class.java, (if (index <= 0) format else format.substring(0, index)).toUpperCase())
                    ?: continue
            val params = if (index <= 0) "" else format.substring(index + 1)
            val value = request(vote, identifier, params)
            if (value != null) text = text.replace(Pattern.quote(m.group()).toRegex(), Matcher.quoteReplacement(value))
        }
        return I18n.color(text)
    }

    private fun request(vote: Vote?, identifier: Vote.Data, params: String): String? {
        if (vote == null) return Msg.ERROR_PLACEHOLDER_REQUEST.msg
        if (params.equals("display", ignoreCase = true)) return identifier.name
        val plugin = PPlugin.getInstance()
        return when (identifier) {
            Vote.Data.ID -> vote.voteID
            Vote.Data.OPEN -> plugin.lang.getRaw(plugin.localeKey, "Vote", "Status." + if (vote.open) "Processing" else "End")
            Vote.Data.TYPE -> {
                if ("desc".equals(params, ignoreCase = true)) vote.type!!.desc else vote.type!!.name
            }
            Vote.Data.GOAL -> vote.goal.toString()
            Vote.Data.OWNER -> vote.ownerName
            Vote.Data.STARTTIME -> {
                if ("desc".equals(params, ignoreCase = true)) TimeUtil.getDescriptionTimeFromTimestamp(vote.timestamp) else vote.time
            }
            Vote.Data.DURATION -> {
                var duration = vote.duration
                for (key in Vote.Duration.values()) duration = duration!!.replace(key.code.toString(), " " + key.name + " ")
                if (duration!!.endsWith(" ")) duration = duration.substring(0, duration.lastIndexOf(" "))
                duration
            }
            Vote.Data.TITLE -> vote.title
            Vote.Data.DESCRIPTION -> String.format(Msg.VOTE_VALUE_DESCRIPTION.msg, vote.description!!.size)
            Vote.Data.CHOICE -> {
                val choice = EnumUtil.valueOf(Vote.Choice::class.java, params.toUpperCase())
                        ?: return Msg.ERROR_PLACEHOLDER_REQUEST.msg
                vote.choices!!.getOrDefault(choice, ChatColor.BLUE.toString() + choice.name)
            }
            Vote.Data.AUTOCAST -> {
                val usermode = plugin.pConfig.config.getBoolean(ConfPath.Path.AUTOCAST_USERMODE.path, true)
                val blacklist = plugin.pConfig.config.getBoolean(ConfPath.Path.AUTOCAST_BLACKLIST.path, true)
                if ("mode".equals(params, ignoreCase = true)) return if (usermode) Msg.AUTOCAST_MODE_USERMODE.msg else if (blacklist) Msg.AUTOCAST_MODE_BLACKLIST.msg else Msg.AUTOCAST_MODE_WHITELIST.msg else if ("desc".equals(params, ignoreCase = true)) return if (blacklist) Msg.AUTOCAST_MODE_BLACKLIST_DESC.msg else Msg.AUTOCAST_MODE_WHITELIST_DESC.msg else if ("content".equals(params, ignoreCase = true)) return if (usermode) Msg.AUTOCAST_MODE_USERMODE_DESC.msg else Arrays.toString(plugin.pConfig.config.getStringList(ConfPath.Path.AUTOCAST_LIST.path).toTypedArray())
                String.format(Msg.VOTE_VALUE_AUTOCAST.msg, vote.autocast!!.size)
            }
            Vote.Data.RESULT -> {
                val result = EnumUtil.valueOf(Vote.Result::class.java, params.toUpperCase())
                        ?: return vote.result().name
                vote.results!!.getOrDefault(result, ChatColor.BLUE.toString() + result.name)
            }
            Vote.Data.PARTICIPANT -> {
                val choiceType = EnumUtil.valueOf(Vote.Choice::class.java, params.toUpperCase())
                if (choiceType != null) vote.listParticipants(Predicate { user: Vote.Participant? -> user!!.choice == choiceType }).size.toString() else String.format(Msg.VOTE_VALUE_PARTICIPANT.msg, vote.participants!!.size)
            }
            Vote.Data.PROCESS -> vote.process.toString() + "%"
            Vote.Data.ANONYMOUS -> {
                if ("desc".equals(params, ignoreCase = true)) return Msg.VOTE_ANONYMOUS_DESC.msg
                if (vote.allowAnonymous) Msg.VOTE_ANONYMOUS_ENABLE.msg else Msg.VOTE_ANONYMOUS_DISABLE.msg
            }
            Vote.Data.PUBLIC -> if (vote.isPublic) Msg.VOTE_PUBLIC_ENABLE.msg else Msg.VOTE_PUBLIC_DISABLE.msg
            Vote.Data.EDITABLE -> if (vote.allowEdit) Msg.VOTE_EDITABLE_ENABLE.msg else Msg.VOTE_EDITABLE_DISABLE.msg
            else -> Msg.ERROR_PLACEHOLDER_REQUEST.msg
        }
    }

    fun parse(vote: Vote, lore: List<String>?): List<String?> {
        val result: MutableList<String?> = ArrayList()
        for (text in lore!!) {
            result.add(parse(vote, text))
            if (text.contains("%DESCRIPTION%") || text.contains("%AUTOCAST%")) {
                val isDescription = text.contains("%DESCRIPTION%")
                val content: MutableList<String> = ArrayList()
                (if (isDescription) vote.description else vote.autocast)!!.forEach(
                        Consumer { line: String? -> content.add(I18n.color(text.substring(0, text.indexOf("%")) + (if (isDescription) "" else "/") + line)) }
                )
                result.addAll(content)
            }
        }
        return result
    }

    fun applyPlaceholder(item: ItemStack, vote: Vote): ItemStack {
        val result = item.clone()
        val meta = result.itemMeta ?: return result
        meta.setDisplayName(parse(vote, meta.displayName))
        if (meta.lore != null) meta.lore = parse(vote, meta.lore)
        result.itemMeta = meta
        return result
    }
}