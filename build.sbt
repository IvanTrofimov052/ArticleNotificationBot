enablePlugins(
  JavaAppPackaging,
  DockerPlugin
)

ThisBuild / scalaVersion := "3.3.1"
ThisBuild / version      := "0.1.0-SNAPSHOT"

Compile / compile / scalacOptions ++= Seq(
  "-Werror",
  "-Wunused:all",
  "-Wvalue-discard",
  "-unchecked"
)

Docker / packageName := "reminder-bot"
dockerBaseImage      := "eclipse-temurin:17"

lazy val root = (project in file("."))
  .settings(
    name                                           := "reminder",
    libraryDependencies += "io.github.apimorphism" %% "telegramium-core"          % "9.801.0",
    libraryDependencies += "io.github.apimorphism" %% "telegramium-high"          % "9.801.0",
    libraryDependencies += "com.github.pureconfig" %% "pureconfig-generic-scala3" % "0.17.8",
    libraryDependencies += "org.tpolecat"          %% "doobie-core"               % "1.0.0-RC2",
    libraryDependencies += "org.tpolecat"          %% "doobie-hikari"             % "1.0.0-RC2",
    libraryDependencies += "org.tpolecat"          %% "doobie-postgres"           % "1.0.0-RC2",
    libraryDependencies += "org.typelevel"         %% "log4cats-core"             % "2.7.0",
    libraryDependencies += "org.typelevel"         %% "log4cats-slf4j"            % "2.7.0",
    libraryDependencies += "tf.tofu"               %% "tofu"                      % "0.13.2",
    libraryDependencies += "tf.tofu"               %% "tofu-logging"              % "0.13.6",
    libraryDependencies += "com.softwaremill.sttp.client4" %% "core"          % "4.0.0-M19",
    libraryDependencies += "com.softwaremill.sttp.client4" %% "cats"          % "4.0.0-M19",
    libraryDependencies += "io.circe"                      %% "circe-parser"  % "0.14.10",
    libraryDependencies += "io.circe"                      %% "circe-generic" % "0.14.10",
    libraryDependencies += "org.scalactic"                 %% "scalactic"     % "3.2.19",
    libraryDependencies += "org.scalatest"                 %% "scalatest"     % "3.2.19" % "test"
  )
