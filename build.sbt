import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

lazy val server = (project in file("server")).settings(commonSettings).settings(
    scalaJSProjects := Seq(client),
    Assets / pipelineStages := Seq(scalaJSPipeline),
    pipelineStages := Seq(digest, gzip),
    // triggers scalaJSPipeline when using compile or continuous compilation
    Compile / compile := ((Compile / compile) dependsOn scalaJSPipeline).value,
    swaggerDomainNameSpaces := Seq("models"),
    libraryDependencies ++= Seq(
        "com.vmunier" %% "scalajs-scripts" % "1.2.0",
        "com.typesafe.play" %% "play-slick" % "5.0.0",
        "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0",
        "com.dripower" %% "play-circe" % "2814.2",
        "io.circe" %%% "circe-generic" % "0.14.1",
        "io.circe" %%% "circe-parser" % "0.14.1",
        "com.h2database" % "h2" % "1.4.200",
        "com.github.dwickern" %% "swagger-play2.8" % "3.1.0",
        "io.swagger" % "swagger-core" % "1.6.2",
        "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.11.1",
        "org.webjars" % "swagger-ui" % "3.51.2",
        "org.postgresql" % "postgresql" % "42.2.23",
        guice,
        "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % "test"
    )
).enablePlugins(PlayScala, SwaggerPlugin)
    .disablePlugins(PlayFilters)
    .dependsOn(sharedJvm)
// Compile the project before generating Eclipse files, so that generated .scala or .class files for views and routes are present

lazy val client = (project in file("client")).settings(commonSettings).settings(
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
        "org.scala-js" %%% "scalajs-dom" % "1.1.0",
        "com.lihaoyi" %%% "scalatags" % "0.9.4",
        "org.querki" %%% "jquery-facade" % "2.0",
        "io.circe" %%% "circe-core" % "0.14.1",
        "io.circe" %%% "circe-generic" % "0.14.1",
        "io.circe" %%% "circe-parser" % "0.14.1",
    ),
    jsDependencies ++= Seq(
        "org.webjars" % "jquery" % "3.6.0" / "jquery.js" minified "jquery.min.js",
        "org.webjars" % "notifyjs" % "0.4.2" / "notify.js"
    )
).enablePlugins(ScalaJSPlugin, ScalaJSWeb, JSDependenciesPlugin)
    .dependsOn(sharedJs)

lazy val shared = crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .in(file("shared"))
    .settings(commonSettings)
lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

lazy val commonSettings = Seq(
    scalaVersion := "2.12.14",
    organization := "com.effe",
)

// loads the server project at sbt startup
onLoad in Global := (onLoad in Global).value andThen { s: State => "project server" :: s }
