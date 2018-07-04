package me.nicofisi.commonspigotstuff.requirements

import me.nicofisi.commonspigotstuff.commands.CCommand
import me.nicofisi.commonspigotstuff.{CRequirement, PluginInfo}
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object ReqIsPlayer extends CRequirement {
  override def check(sender: CommandSender, command: Option[CCommand])(implicit info: PluginInfo): Boolean =
    sender.isInstanceOf[Player]

  override def createErrorMessage(sender: CommandSender, action: String)(implicit info: PluginInfo): String =
      s"&pYou can only $action in-game, as a player"
}
