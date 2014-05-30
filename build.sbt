import android.Keys._

android.Plugin.androidBuild

name := "teacher-journal"

scalaVersion := "2.11.0"

proguardCache in Android ++= Seq(
  ProguardCache("org.scaloid") % "org.scaloid"
)

proguardOptions in Android ++= Seq("-dontobfuscate", "-dontoptimize")

incOptions := incOptions.value.withNameHashing(true)

libraryDependencies += "org.scaloid" %% "scaloid" % "3.4-10"

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
