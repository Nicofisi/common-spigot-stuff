package me.nicofisi.commonspigotstuff

import java.util.UUID

import org.bukkit.{Bukkit, GameMode, OfflinePlayer, World}
import org.bukkit.entity.Player

import scala.collection.JavaConverters._
import scala.util.Try

trait CType[A] {
  type ParseFailReason = String
  type ParseResult = Either[A, ParseFailReason]

  def parse(string: String)(implicit info: PluginInfo): ParseResult

  /** Returns a list of suggestions to send to the player when they press the tab key with the given
    * string typed, in a place where an object of this type is expected
    *
    * @param mustStartWith the string; implementations are allowed to return an empty list when
    *                      the length of this string is lower than some given small number
    */
  def tabSuggestions(mustStartWith: String): List[String] = Nil
}

object TypeBoolean extends CType[Boolean] {
  override def parse(string: String)(implicit info: PluginInfo): ParseResult = string match {
    case "yes" | "true" | "y" | "t" => Left(true)
    case "no" | "false" | "n" | "f" => Left(false)
    case _ => Right(s"&sYou typed &p$string in a place where only &pyes &sor &pno &sshould be used".colored)
  }
}

object TypeGameMode extends CType[GameMode] {
  override def parse(string: String)(implicit info: PluginInfo): ParseResult = string match {
    case "0" | "survival" | "s" => Left(GameMode.SURVIVAL)
    case "1" | "creative" | "c" => Left(GameMode.CREATIVE)
    case "2" | "adventure" | "a" => Left(GameMode.ADVENTURE)
    case "3" | "spectator" | "sp" => Left(GameMode.SPECTATOR)
    case _ => Right(s"&sGame mode called &p$string &sdoesn't exist")
  }

  override def tabSuggestions(mustStartWith: String): List[String] =
    List("survival", "creative", "adventure", "spectator").filter(_.startsWith(mustStartWith.toLowerCase))
}

object TypeInt extends CType[Int] {
  override def parse(string: String)(implicit info: PluginInfo): ParseResult =
    Try(Left(string.toInt)).getOrElse(
      if (Try(BigInt(string)).isSuccess)
        Right(s"&sThe number &p$string &sis too big, it should be at most ${Int.MaxValue}")
      else
        Right(s"&p$string &sis not an integer")
    )
}

object TypeLong extends CType[Long] {
  override def parse(string: String)(implicit info: PluginInfo): ParseResult =
    Try(Left(string.toLong)).getOrElse(
      if (Try(BigInt(string)).isSuccess)
        Right(s"&sThe number &p$string &sis too big, it should be at most ${Long.MaxValue}")
      else
        Right(s"&p$string &sis not an integer")
    )
}

object TypeDouble extends CType[Double] {
  override def parse(string: String)(implicit info: PluginInfo): ParseResult =
    Try(Left(string.toDouble)).getOrElse(
      Right(s"&p$string &scouldn't be interpreted as a floating-point number")
    )
}

object TypeOfflinePlayer extends CType[OfflinePlayer] {
  override def parse(string: String)(implicit info: PluginInfo): ParseResult = {
    // noinspection ScalaDeprecation
    Option(Bukkit.getOfflinePlayer(string)) // deprecated but not going to be removed, and we do need it here
      .orElse(Try(UUID.fromString(string)).toOption.flatMap(uuid => Some(Bukkit.getOfflinePlayer(uuid))))
      .map(Left(_)).getOrElse(Right(s"&sNo player named &p$string &scould be found"))
  }

  // TODO find a way faster way
  // than Bukkit.getOfflinePlayers.asScala.map(_.getName).filter(_.startsWith(mustStartWith))
  override def tabSuggestions(mustStartWith: String): List[String] = Nil
}

object TypePlayer extends CType[Player] {
  override def parse(string: String)(implicit info: PluginInfo): ParseResult = {
    Option(Bukkit.getPlayerExact(string))
      .orElse(Try(UUID.fromString(string)).toOption.flatMap(uuid => Some(Bukkit.getPlayer(uuid))))
      .map(Left(_)).getOrElse {
      val players = Bukkit.getOnlinePlayers.asScala.filter(_.getName.toLowerCase.startsWith(string))
      if (players.isEmpty)
        Right(s"&sNo online player could be found whose name starts with &p$string")
      else if (players.size > 1)
        Right(s"&sThere are currently a few players online whose names start with &p$string")
      else
        Left(players.head)
    }
  }

  override def tabSuggestions(mustStartWith: String): List[String] =
    Bukkit.getOnlinePlayers.asScala.toList.map(_.getName).filter(_.toLowerCase.startsWith(mustStartWith.toLowerCase))
}

object TypeString extends CType[String] {
  override def parse(string: String)(implicit info: PluginInfo): ParseResult = Left(string)
}

object TypeWorld extends CType[World] {
  override def parse(string: String)(implicit info: PluginInfo): ParseResult = {
    Option(Bukkit.getWorld(string)).map(Left(_)).getOrElse {
      val worlds = Bukkit.getWorlds.asScala.filter(_.getName.toLowerCase.startsWith(string))
      if (worlds.isEmpty)
        Right(s"&sThere aren't any loaded worlds whose names start with &p$string")
      else if (worlds.size > 1)
        Right(s"&sThere are currently a few loaded worlds whose names start with &p$string")
      else
        Left(worlds.head)
    }
  }

  override def tabSuggestions(mustStartWith: String): List[String] =
    Bukkit.getWorlds.asScala.toList.map(_.getName).filter(_.toLowerCase.startsWith(mustStartWith.toLowerCase))
}
