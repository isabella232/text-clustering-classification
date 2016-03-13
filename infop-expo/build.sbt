name := """infop-expo"""

version := "1.0-SNAPSHOT"

maintainer := "Sayantam Dey <sayantam.dey@3pillarglobal.com>"

packageSummary := "Text Analytics Expo Debian Package"

packageDescription := """Packaged text analytics expo with code and data."""

lazy val root = (project in file(".")).enablePlugins(PlayScala, DebianPlugin, JavaServerAppPackaging, LauncherJarPlugin)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
resolvers += Resolver.url("Typesafe Ivy releases", url("https://repo.typesafe.com/typesafe/ivy-releases"))(Resolver.ivyStylePatterns)

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

daemonUser in Linux := "vagrant"

fork in run := false
