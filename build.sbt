lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := """Assignment2""",
    version := "1.0",
    scalaVersion := "2.13.1",
    libraryDependencies ++= Seq(
      guice,
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings"
    )
  )
