name := "TC_Smartsheet_Docusign"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
  "com.smartsheet" % "smartsheet-sdk-java" % "1.0.4",
  "commons-io" % "commons-io" % "2.4"
)     

play.Project.playJavaSettings
