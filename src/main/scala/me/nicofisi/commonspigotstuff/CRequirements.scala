package me.nicofisi.commonspigotstuff

import me.nicofisi.commonspigotstuff.commands.CCommand
import org.bukkit.command.CommandSender

trait CRequirement {
  def check(sender: CommandSender, command: Option[CCommand] = None)(implicit info: PluginInfo): Boolean =
    throw new UnsupportedOperationException

  def createErrorMessage(sender: CommandSender, action: String)(implicit info: PluginInfo): String

  def createErrorMessage(sender: CommandSender, command: Option[CCommand] = None)
                        (implicit info: PluginInfo): String =
    createErrorMessage(sender, if (command.isDefined) "execute this command" else "perform this action")

  def sendErrorMessage(sender: CommandSender, command: Option[CCommand] = None)
                      (implicit info: PluginInfo): Unit =
    sender.sendError(createErrorMessage(sender, command))

  final def validate(sender: CommandSender, command: Option[CCommand] = None)
                    (implicit info: PluginInfo): Unit = {
    if (!check(sender, command)) {
      val error = createErrorMessage(sender, command)
      sender.sendError(error)
      throw new CommonRequirementException(error)
    }
  }

  final def validate(sender: CommandSender, message: String, command: Option[CCommand])(implicit info: PluginInfo): Unit = {
    if (!check(sender, command)) {
      sender.sendError(message)
      throw new CommonRequirementException(message) // does the 'message' here really make sense
    }
  }
}


class CommonRequirementException(message: String) extends RuntimeException {
  def this() = this(null)
}

object CommonRequirementException {
  def apply(): CommonRequirementException = new CommonRequirementException()
}
