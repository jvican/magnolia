import com.typesafe.sbt.pgp.PgpKeys.publishSigned

lazy val core = crossProject
  .in(file("core"))
  .settings(buildSettings: _*)
  .settings(publishSettings: _*)
  .settings(scalaMacroDependencies: _*)
  .settings(moduleName := "magnolia")

lazy val coreJVM = core.jvm
lazy val coreJS = core.js

lazy val examples = crossProject
  .in(file("examples"))
  .settings(buildSettings: _*)
  .settings(publishSettings: _*)
  .settings(moduleName := "magnolia-examples")
  .dependsOn(core)

lazy val examplesJVM = examples.jvm
lazy val examplesJS = examples.js

lazy val tests = project
  .in(file("tests"))
  .settings(buildSettings: _*)
  .settings(unmanagedSettings)
  .settings(moduleName := "magnolia-tests")
  .settings(
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full),
    libraryDependencies ++= Seq(
      "com.fommil" %% "deriving-macro" % "0.9.0",
      "com.fommil" %% "scalaz-deriving" % "0.9.0"
    )
  )
  .dependsOn(examplesJVM)

lazy val benchmarks = project
  .in(file("benchmarks"))
  .settings(buildSettings: _*)
  .settings(moduleName := "magnolia-benchmarks")
  .dependsOn(examplesJVM)

lazy val buildSettings = Seq(
  organization := "com.propensive",
  scalaVersion := "2.12.4",
  name := "magnolia",
  version := "0.6.1",
  scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-Ywarn-value-discard",
    "-Ywarn-dead-code",
    "-Ywarn-nullary-unit",
    "-Ywarn-numeric-widen",
    "-Ywarn-inaccessible",
    "-Ywarn-adapted-args"
  ),
  scmInfo := Some(
    ScmInfo(url("https://github.com/propensive/magnolia"),
            "scm:git:git@github.com:propensive/magnolia.git")
  )
)

lazy val publishSettings = Seq(
  homepage := Some(url("http://magnolia.work/")),
  licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
  autoAPIMappings := true,
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ =>
    false
  },
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  pomExtra := (
    <developers>
      <developer>
        <id>propensive</id>
        <name>Jon Pretty</name>
        <url>https://github.com/propensive/magnolia/</url>
      </developer>
    </developers>
  )
)

import java.io.File

lazy val unmanagedSettings = unmanagedBase := {
  val path = baseDirectory.value / "lib"
  val scalaPartialVersion = CrossVersion partialVersion scalaVersion.value
  scalaPartialVersion.collect {
    case (2, y) if y == 11 => new File(path + "-2.11")
    case (2, y) if y >= 12 => new File(path + "-2.12")
  }.toList
}

lazy val scalaMacroDependencies: Seq[Setting[_]] = Seq(
  libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value
)
