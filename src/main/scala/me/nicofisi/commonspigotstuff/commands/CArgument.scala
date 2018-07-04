package me.nicofisi.commonspigotstuff
package commands

import org.bukkit.command.CommandSender

case class CArgument[A](cType: CType[A], name: String,
                        defValue: PartialFunction[CommandSender, Option[(A, String)]] // TODO ugly
                        = new PartialFunction[CommandSender, Option[(A, String)]] {
                          override def isDefinedAt(x: CommandSender): Boolean = false

                          override def apply(v1: CommandSender): Option[(A, String)] = None
                        },
                        isRequired: Boolean = true)
