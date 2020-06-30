package net.shoal.sir.voteup.config

import org.serverct.parrot.parrotx.PPlugin
import org.serverct.parrot.parrotx.config.PConfig
import java.util.*
import java.util.function.Consumer

class ConfPath : PConfig(PPlugin.getInstance(), "config", "主配置文件") {
    override fun saveDefault() {
        plugin.saveDefaultConfig()
    }

    enum class Path(val path: String) {
        BSTATS("bStats"), SOUND_ENABLE("Sound.Enable"), SOUND_ACTION_START("Sound.Action.Start"), SOUND_ACTION_SUCCESS("Sound.Action.Success"), SOUND_ACTION_FAILURE("Sound.Action.Failure"), SOUND_VOTE_START("Sound.Vote.Start"), SOUND_VOTE_END("Sound.Vote.End"), AUTOCAST_ENABLE("Autocast.Enable"), AUTOCAST_USERMODE("Autocast.Usermode"), AUTOCAST_BLACKLIST("Autocast.Blacklist"), AUTOCAST_LIST("Autocast.List"), ADMIN("Admin"), SETTINGS_PARTICIPANT_LEAST("Settings.ParticipantLeast"), SETTINGS_BROADCAST_TITLE_VOTESTART("Settings.Broadcast.Title.VoteStart"), SETTINGS_BROADCAST_TITLE_VOTEEND("Settings.Broadcast.Title.VoteEnd"), SETTINGS_BROADCAST_TITLE_FADEIN("Settings.Broadcast.Title.FadeIn"), SETTINGS_BROADCAST_TITLE_STAY("Settings.Broadcast.Title.Stay"), SETTINGS_BROADCAST_TITLE_FADEOUT("Settings.Broadcast.Title.FadeOut"), SETTINGS_ALLOW_ANONYMOUS("Settings.Allow.Anonymous"), SETTINGS_ALLOW_PUBLIC("Settings.Allow.Public"), SETTINGS_ALLOW_EDIT_VOTE("Settings.Allow.Edit.Vote"), SETTINGS_ALLOW_EDIT_PARTICIPANT("Settings.Allow.Edit.Participant");

    }

    fun admins(): List<UUID> {
        val admins: MutableList<UUID> = ArrayList()
        config.getStringList(Path.ADMIN.path).forEach(Consumer { uuid: String? -> admins.add(UUID.fromString(uuid)) })
        return admins
    }
}