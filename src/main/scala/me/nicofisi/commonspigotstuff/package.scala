package me.nicofisi

import java.io.File

import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import org.bukkit.{Bukkit, ChatColor}

import scala.concurrent.duration.Duration
import scala.runtime.NonLocalReturnControl

package object commonspigotstuff {
  val ErrorPrefix: String = "&p[Error] &s"
  //  val ErrorPrefixColored: String = ErrorPrefix.colored

  /** Returns the location of the jar file containing this class, which should be the plugin's jar file */
  def pluginJarFile = new File(getClass.getProtectionDomain.getCodeSource.getLocation.toURI)

  /** Returns the plugin's data folder */
  def dataFolder(implicit info: PluginInfo): File = info.plugin.getDataFolder

  /** Logs the given message to plugin's Logger object, with severity of [[java.util.logging.Level.INFO]] */
  def logInfo(message: String)(implicit info: PluginInfo): Unit = info.plugin.getLogger.info(message)

  /** Logs the given message to plugin's Logger object, with severity of [[java.util.logging.Level.WARNING]] */
  def logWarning(warning: String)(implicit info: PluginInfo): Unit = info.plugin.getLogger.warning(warning)

  /** Logs the given message to plugin's Logger object, with severity of [[java.util.logging.Level.SEVERE]] */
  def logError(error: String)(implicit info: PluginInfo): Unit = info.plugin.getLogger.severe(error)


  /** Returns a task that will run on the next server tick.
    *
    * @param info the info object containing a reference to the plugin scheduling the task
    * @param task the code to run
    */
  def runTask(task: => Unit)(implicit info: PluginInfo): BukkitTask =
    Bukkit.getScheduler.runTask(info.plugin, enhanceTask(task))

  def runTaskAsync(task: => Unit)(implicit plugin: JavaPlugin): BukkitTask =
    Bukkit.getScheduler.runTaskAsynchronously(plugin, enhanceTask(task))

  def runTaskLater(delay: Duration, task: => Unit)(implicit info: PluginInfo): BukkitTask =
    Bukkit.getScheduler.runTaskLater(info.plugin, enhanceTask(task), delay.toMillis / 50)

  def runTaskLaterAsync(delay: Duration, task: => Unit)(implicit info: PluginInfo): BukkitTask =
    Bukkit.getScheduler.runTaskLaterAsynchronously(info.plugin, enhanceTask(task), delay.toMillis / 50)

  def runTaskTimer(delayToFirst: Duration, delayBetween: Duration, task: => Unit)
                  (implicit info: PluginInfo): BukkitTask =
    Bukkit.getScheduler.runTaskTimer(
      info.plugin, enhanceTask(task), delayToFirst.toMillis / 50, delayBetween.toMillis / 50)

  def runTaskTimerAsync(delayToFirst: Duration, delayBetween: Duration, task: => Unit)
                       (implicit info: PluginInfo): BukkitTask =
    Bukkit.getScheduler.runTaskTimerAsynchronously(
      info.plugin, enhanceTask(task), delayToFirst.toMillis / 50, delayBetween.toMillis / 50)

  def enhanceTask(task: => Unit): Runnable =
    () => try task catch {
      case _: NonLocalReturnControl[_] =>
      // we don't catch other errors
    }


  def pluginExists(name: String): Boolean = Bukkit.getPluginManager.getPlugin(name) != null


  implicit class StringUtils(str: String) {
    def colored(implicit info: PluginInfo): String =
      ChatColor.translateAlternateColorCodes('&',
        str.replace("&p", info.primaryColor.toString).replace("&s", info.secondaryColor.toString)
      )

    def colorsRemoved(implicit info: PluginInfo): String =
      ChatColor.stripColor(str.colored)
  }

  implicit class CommandSenderUtils(sender: CommandSender) {
    def sendColored(message: String)(implicit info: PluginInfo): Unit =
      sender.sendMessage(("&s" + message).colored)

    def sendError(message: String)(implicit info: PluginInfo): Unit =
      sender.sendMessage(ErrorPrefix.colored + message.colored)

    def sendErrorRaw(message: String)(implicit info: PluginInfo): Unit =
      sender.sendMessage(ErrorPrefix.colored + message)
  }

}
