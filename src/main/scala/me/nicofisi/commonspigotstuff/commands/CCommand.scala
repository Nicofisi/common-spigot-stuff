package me.nicofisi.commonspigotstuff
package commands

import me.nicofisi.commonspigotstuff.{CRequirement, PluginInfo, TypeString}
import org.bukkit.command.CommandSender

abstract class CCommand {

  /** For child commands all aliases matter, but for base ones,
    * only the first one is important, and it must match the one from the plugin.yml
    */
  def aliases: List[String]

  def arguments: List[CArgument[_]]

  def requirements: List[CRequirement]

  def execute(sender: CommandSender, arguments: CParsedArguments)(implicit info: PluginInfo): Unit = execute(sender)

  def execute(sender: CommandSender)(implicit info: PluginInfo): Unit = throw new NotImplementedError
}

object CCommandWithArgs {
  def apply(cmdAliases: List[String], reqs: List[CRequirement], cmdArgs: List[CArgument[_]],
            onExecute: (CommandSender, CParsedArguments) => Unit): CCommand = new CCommand {

    override def aliases: List[String] = cmdAliases

    override def requirements: List[CRequirement] = reqs

    override def arguments: List[CArgument[_]] = cmdArgs

    override def execute(sender: CommandSender, arguments: CParsedArguments)(implicit info: PluginInfo): Unit =
      onExecute(sender, arguments)
  }
}

object CCommandNoArgs {
  def apply(cmdAliases: List[String], reqs: List[CRequirement],
            onExecute: CommandSender => Unit): CCommand = new CCommand {
    override def aliases: List[String] = cmdAliases

    override def requirements: List[CRequirement] = reqs

    override def arguments: List[CArgument[_]] = Nil

    override def execute(sender: CommandSender, arguments: CParsedArguments)(implicit info: PluginInfo): Unit =
      onExecute(sender)
  }
}

object CParentCommand {
  def apply(cmdAliases: List[String], children: CCommand*): CCommand = new CCommand {
    override def aliases: List[String] = cmdAliases

    override def arguments: List[CArgument[_]] = List(
      CArgument(TypeString, "subcommand", defValue = {
        case _ => Some(("help", "help"))
      }),
      CArgument(TypeString, "arguments", isRequired = false)
    )

    override def requirements: List[CRequirement] = Nil

    override def execute(sender: CommandSender, arguments: CParsedArguments)(implicit info: PluginInfo): Unit = {
      val argAlias = arguments.get[String](0).toLowerCase

      if (argAlias == "help" || argAlias == "?") {
        showHelp(sender)

      } else {
        val argArgs = arguments.getOpt[String](1).map(_.split(" ").toList).getOrElse(Nil)

        children.find(_.aliases.contains(argAlias)) match {
          case Some(cc) => CCommandExecutor.handleCCommand(sender, cc, argAlias, argArgs)
          case None => showHelp(sender, Some(argAlias))
        }
      }

      def showHelp(sender: CommandSender, unknownAlias: Option[String] = None): Unit = {
        sender.sendColored("This is help.")
        ???
      }
    }
  }
}
