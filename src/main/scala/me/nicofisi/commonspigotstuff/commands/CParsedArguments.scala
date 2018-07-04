package me.nicofisi.commonspigotstuff
package commands

case class CParsedArguments(args: List[Option[_]]) {

  def apply[A](index: Int): Option[A] = {
    args(index).asInstanceOf[Option[A]]
  }

  def get[A](index: Int): A = {
    args(index).get.asInstanceOf[A]
  }

  def getOpt[A](index: Int): Option[A] = {
    args(index).asInstanceOf[Option[A]]
  }

  def anyWasSpecified: Boolean = args.exists(arg => {
    arg.nonEmpty && arg.get != false
  })
}

