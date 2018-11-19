import PgpKeys._
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._
import xerial.sbt.Sonatype._

lazy val `xgboost-jvm` =
  project
    .in(file("."))
    .aggregate(
      xgboost4j
    )
    .settings(settings ++ notToPublish)
    .settings(
      releaseProcess := Seq[ReleaseStep](
        checkSnapshotDependencies,
        inquireVersions,
        releaseStepCommandAndRemaining("+clean"),
        releaseStepCommandAndRemaining("+doc"),
        releaseStepCommandAndRemaining("+test"),
        setReleaseVersion,
        commitReleaseVersion,
        tagRelease,
        releaseStepCommandAndRemaining("+publishSigned"),
        setNextVersion,
        commitNextVersion,
        releaseStepCommand("sonatypeReleaseAll"),
        pushChanges
      )
    )

lazy val xgboost4j =
  project
    .in(file("xgboost/jvm-packages/xgboost4j"))
    .settings(settings ++ toPublish)
    .settings(
      crossScalaVersions += "2.12.7"
    )
    .settings(
      libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-actor"   % akkaVersion.value,
        "com.typesafe.akka" %% "akka-testkit" % akkaVersion.value % Test,
        "junit"             %  "junit"        % "4.11"            % Test
      )
    )

lazy val settings =
  Seq(
    akkaVersion                      := (if (isScala211.value) "2.3.11" else "2.4.20"),
    crossScalaVersions               := Seq("2.11.12"),
    envVars                         ++= Map("LC_ALL" -> "", "LC_NUMERIC" -> "C", "USE_OPENMP" -> "OFF"),
    isScala211                       := (scalaBinaryVersion.value == "2.11"),
    javacOptions                    ++= Seq("-source", "1.7", "-target", "1.7"),
    licenses                         := Seq("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt")),
    organization                     := "io.findify",
    scalaVersion                     := "2.11.12",
    scalacOptions                   ++= Seq("-deprecation", "-encoding", "UTF-8", "-feature"),
    scalacOptions                    += (if (isScala211.value) "-target:jvm-1.7" else "-target:jvm-1.8"),
    scalacOptions in (Compile, doc) ++= (if (isScala211.value) Nil else Seq("-no-java-comments")), // https://github.com/scala/scala-dev/issues/249#issuecomment-255863118
    testOptions                      += Tests.Argument(TestFrameworks.ScalaTest, "-oDF")
  ) ++ Seq(
    baseDirectory in (Test, test) := (baseDirectory in ThisBuild).value / "xgboost" / "jvm-packages",
    fork          in (Test, test) := true
  ) ++ Seq(
    libraryDependencies ++= Seq(
      "com.esotericsoftware.kryo" %  "kryo"            % "4.02",
      "commons-logging"           %  "commons-logging" % "1.2",
      "org.scalatest"             %% "scalatest"       % "3.0.0" % Test
    ),
    sonatypeProjectHosting := Some(
      GitHubHosting("findify", "xgboost4j4s", "roman@findify.io")
    )
  )

lazy val notToPublish =
  Seq(
    publish         := {},
    publishArtifact := false,
    publishLocal    := {},
    publishSigned   := {}
  )

lazy val toPublish =
  Seq(
    publishMavenStyle          := true,
    publishSignedConfiguration := publishSignedConfiguration.value.withOverwrite(isSnapshot.value),
    publishTo                  := sonatypePublishTo.value
  )

lazy val akkaVersion = settingKey[String]("akka version")
lazy val isScala211  = settingKey[Boolean]("whether or not scalaBinaryVersion is 2.11")

lazy val sparkVersion = "2.3.0"
