package me.nicofisi.commonspigotstuff
package commands

import me.nicofisi.commonspigotstuff.{CommonRequirementException, PluginInfo}
import org.bukkit.command.{Command, CommandExecutor, CommandSender}

import scala.util.control.NonFatal

class CCommandExecutor(commands: List[CCommand])(implicit info: PluginInfo) extends CommandExecutor {
  override def onCommand(sender: CommandSender, command: Command, label: String, args: Array[String]): Boolean = {
    val name = command.getName
    commands.find(_.aliases.contains(name)) match {
      case Some(cc) => CCommandExecutor.handleCCommand(sender, cc, label, args.toList)
      case None => ???
    }
    true
  }
}

object CCommandExecutor {
  def handleCCommand(sender: CommandSender, cc: CCommand,
                     label: String, args: List[String])(implicit info: PluginInfo): Unit = {
    def doHandle(): Unit = {
      cc.requirements.foreach(_.validate(sender, Some(cc)))

      val argsAfterJoin =
        args.slice(1, cc.arguments.size - 1) :+ args.drop(cc.arguments.size - 1).mkString(" ")

      val parsedArgs = CParsedArguments(cc.arguments.zipWithIndex.map { case (cArg, index) =>
        argsAfterJoin.lift(index) match {
          case Some(arg) =>
            cArg.cType.parse(arg) match {
              case Left(parsed) => Some(parsed)
              case Right(failParseReason) =>
                sender.sendError(failParseReason)
                return
            }
          case None =>
            if (cArg.defValue.isDefinedAt(sender)) {
              cArg.defValue(sender).orElse {
                if (cArg.isRequired) {
                  sender.sendError(s"The argument ${cArg.name} needs to be defined") // TODO show help
                  return
                } else None
              }
            } else if (cArg.isRequired) { // TODO why ctrl+c ctrl+v
              sender.sendError(s"The argument ${cArg.name} needs to be defined") // TODO show help
              return
            } else None
        }
      })

      cc.execute(sender, parsedArgs)
    }

    try doHandle() catch {
      case _: CommonRequirementException =>
      // only threw to stop the execution, informing the user
      // should have been manually handled before
      case NonFatal(ex) =>
        sender.sendError("An error has occurred while attempting to perform your command")
        sender.sendError(
          s"The error: &p${ex.getClass.getCanonicalName}${Option(ex.getMessage).map(": " + _).getOrElse("")}")
        ex.printStackTrace()
    }
  }
}
