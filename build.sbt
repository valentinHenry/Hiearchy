ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name                                   := "hierarchy",
    idePackagePrefix                       := Some("fr.valentinhenry"),
    libraryDependencies += "com.chuusai"    % "shapeless_2.13"      % "2.4.0-M1",
    libraryDependencies += "org.typelevel" %% "cats-core"           % "2.9.0",
    libraryDependencies += "org.typelevel" %% "log4cats-slf4j"      % "2.5.0",
    libraryDependencies += "ch.qos.logback" % "logback-classic"     % "1.4.6",
    libraryDependencies += "org.slf4j"      % "slf4j-api"           % "2.0.5",
    libraryDependencies += "org.typelevel" %% "munit-cats-effect-3" % "1.0.7" % "test",
    addCompilerPlugin("org.typelevel" % "kind-projector"     % "0.13.2" cross CrossVersion.full),
    addCompilerPlugin("com.olegpy"   %% "better-monadic-for" % "0.3.1")
  )

