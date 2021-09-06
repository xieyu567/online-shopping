import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

lazy val server = (project in file("server")).settings(commonSettings).settings(
    scalaJSProjects := Seq(client),
    Assets / pipelineStages := Seq(scalaJSPipeline),
    pipelineStages := Seq(digest, gzip),
    // triggers scalaJSPipeline when using compile or continuous compilation
    Compile / compile := ((Compile / compile) dependsOn scalaJSPipeline).value,
    libraryDependencies ++= Seq(
        "com.vmunier" %% "scalajs-scripts" % "1.2.0",
        "com.typesafe.play" %% "play-slick" % "5.0.0",
        "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0",
        "com.dripower" %% "play-circe" % "2814.2",
        "com.h2database" % "h2" % "1.4.196",
        "org.postgresql" % "postgresql" % "42.2.23",
        guice,
        "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % "test"
    )
).enablePlugins(PlayScala)
    .disablePlugins(PlayFilters)
    .dependsOn(sharedJvm)
// Compile the project before generating Eclipse files, so that generated .scala or .class files for views and routes are present

lazy val client = (project in file("client")).settings(commonSettings).settings(
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
        "org.scala-js" %%% "scalajs-dom" % "1.2.0"
    )
).enablePlugins(ScalaJSPlugin, ScalaJSWeb)
    .dependsOn(sharedJs)

lazy val shared = crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .in(file("shared"))
    .settings(commonSettings)
lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

lazy val commonSettings = Seq(
    scalaVersion := "2.12.14",
    organization := "com.effe"
)

// loads the server project at sbt startup
onLoad in Global := (onLoad in Global).value andThen { s: State => "project server" :: s }
