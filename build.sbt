import android.Keys._

android.Plugin.androidBuild

name := "teacher-journal"

scalaVersion := "2.11.0"

proguardCache in Android ++= Seq(
  ProguardCache("org.scaloid") % "org.scaloid"
)

proguardOptions in Android ++= Seq(
  "-dontobfuscate",
  "-dontoptimize",
  "-dontwarn java.beans.*",
  "-dontwarn javax.transaction.*"
)

incOptions := incOptions.value.withNameHashing(true)

libraryDependencies ++= Seq(
  "org.scaloid" %% "scaloid" % "3.4-10",
  "com.jsuereth" %% "scala-arm" % "1.4",
  "net.sf.opencsv" % "opencsv" % "2.3"
)

scalacOptions in Compile ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen"
)

run <<= run in Android

install <<= install in Android
