package me.nicofisi.commonspigotstuff.requirements

import me.nicofisi.commonspigotstuff.commands.CCommand
import me.nicofisi.commonspigotstuff.{CRequirement, PluginInfo}
import org.bukkit.command.CommandSender

case class ReqHasPermission(permission: String) extends CRequirement {
  override def check(sender: CommandSender, command: Option[CCommand])(implicit info: PluginInfo): Boolean =
    sender.hasPermission(permission)

  override def createErrorMessage(sender: CommandSender, action: String)(implicit info: PluginInfo): String =
      s"&sYou need the &p$permission &spermission to &p$action"
}
