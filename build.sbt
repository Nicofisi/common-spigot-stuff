lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      scalaVersion := "2.12.6",
      organization := "me.nicofisi"
    )),
    name := "common-spigot-stuff",
    version := "2.1.2",

    resolvers += "spigot-repo" at "https://hub.spigotmc.org/nexus/content/repositories/snapshots/",

    assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false),

    libraryDependencies ++= Seq(
      "org.spigotmc" % "spigot-api" % "1.12.2-R0.1-SNAPSHOT" % "provided" intransitive(),
      "com.softwaremill.scalamacrodebug" %% "macros" % "0.4.1", // for debug()
      "org.jetbrains" % "annotations" % "16.0.2" // @Nullable, @NonNull, etc
    )
  )
