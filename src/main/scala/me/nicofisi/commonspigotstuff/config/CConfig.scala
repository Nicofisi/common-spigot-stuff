package me.nicofisi.commonspigotstuff.config

import java.io.File
import java.nio.file.Files

import me.nicofisi.commonspigotstuff.PluginInfo
import me.nicofisi.commonspigotstuff._
import org.bukkit.configuration.file.YamlConfiguration

import scala.collection.JavaConverters._
import scala.io.Source

abstract class CConfig(currentConfigVersion: Int, implicit val info: PluginInfo) {
  val configFile = new File(info.plugin.getDataFolder, "config.yml")
  var yaml: YamlConfiguration = _

  def loadDefaultValues(): Unit

  def configDefaults: Map[String, Any]

  def parseConfig(map: Map[String, AnyRef]): Unit

  def afterReload(): Unit

  private def handleVersionIncrease(oldConfig: YamlConfiguration,
                                    newConfig: YamlConfiguration, fromVersion: Int): Unit = {}


  final def reloadConfig(): Unit = {
    loadDefaultValues()

    if (!configFile.exists()) {
      yaml = new YamlConfiguration()
      setDefaultsInConfig(yaml)
      yaml.set("config-version", currentConfigVersion)
      yaml.options().header(configHeader)
      yaml.save(configFile)
    } else {
      require(configFile.isFile, "config.yml must be a regular file")
      yaml = YamlConfiguration.loadConfiguration(configFile)

      val savedConfigVersion = yaml.getInt("config-version")
      if (savedConfigVersion != currentConfigVersion) {
        val oldConfigsDir = new File(info.plugin.getDataFolder, "old-configs")
        if (!oldConfigsDir.exists()) {
          oldConfigsDir.mkdirs()
        }
        if (savedConfigVersion > currentConfigVersion) {
          val pluginName = info.plugin.getName
          logWarning(s"It appears that the highest config file version understandable by this version of $pluginName")
          logWarning("is lower than the one in your current config file. Did you perhaps downgrade the plugin")
          logWarning("or manually change the config version in the config file? Please never do that again.")
          logWarning("To fix the issue you need to delete/move the config.yml, restart the server for the config")
          logWarning(s"to be recreated, and manually fill it in again. Sorry, but that's not a fault of $pluginName!")
          throw new IllegalStateException()
        } else {
          logInfo(s"The config is going to be updated from v$savedConfigVersion to v$currentConfigVersion now")
          var updatedConfig = yaml
          (savedConfigVersion until currentConfigVersion).foreach { fromVersion =>
            logInfo(s"Updating the config from v$savedConfigVersion# to v$currentConfigVersion#...")
            val configToUpdate = new YamlConfiguration()
            handleVersionIncrease(updatedConfig, configToUpdate, fromVersion)
            updatedConfig = configToUpdate
          }
          yaml = updatedConfig
          setDefaultsInConfig(yaml)
          yaml.options().header(configHeader)
          yaml.set("config-version", currentConfigVersion)
          Files.move(configFile.toPath, new File(oldConfigsDir, s"config-${System.currentTimeMillis()}.yml").toPath)
          yaml.save(configFile)
          logInfo("The config has been successfully updated")
        }
      }
      parseConfig(yaml.getValues(true).asScala.toMap)
    }

    afterReload()
  }

  final def setDefaultsInConfig(yaml: YamlConfiguration): Unit = {
    configDefaults.filterNot { case (key, _) => yaml.contains(key) }.foreach { case (key, value) =>
      yaml.set(key, value)
    }
  }

  final def configHeader: String = Source.fromResource("config-header.md", getClass.getClassLoader).toString.trim
}
