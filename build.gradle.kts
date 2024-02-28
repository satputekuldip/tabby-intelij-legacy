plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm") version "1.9.22"
  id("org.jetbrains.intellij") version "1.17.2"
  id("org.jetbrains.changelog") version "2.2.0"
  id("io.ktor.plugin") version "2.3.8"
}

group = "com.tabbyml"
version = "1.4.0-dev"

repositories {
  mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
  version.set("2021.2.1")
  type.set("IC") // Target IDE Platform

  plugins.set(listOf(/* Plugin Dependencies */))
}

dependencies {
  implementation("io.ktor:ktor-server-core")
  implementation("io.ktor:ktor-server-netty")
}
java {
  sourceCompatibility = JavaVersion.VERSION_16
  targetCompatibility = JavaVersion.VERSION_16
}

tasks {
  // Set the JVM compatibility versions
  withType<JavaCompile> {
    sourceCompatibility = "11"
    targetCompatibility = "11"
  }
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
  }

  patchPluginXml {
    sinceBuild.set("212")
    untilBuild.set("233.*")
    changeNotes.set(provider {
      changelog.renderItem(
        changelog.getLatest(),
        org.jetbrains.changelog.Changelog.OutputType.HTML
      )
    })
  }

  val copyNodeScripts by register<Copy>("copyNodeScripts") {
    dependsOn(prepareSandbox)
    from("node_scripts")
    into("build/idea-sandbox/plugins/intellij-tabby/node_scripts")
  }

  buildSearchableOptions {
    dependsOn(copyNodeScripts)
  }

  runIde {
    dependsOn(copyNodeScripts)
  }

  signPlugin {
    certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
    privateKey.set(System.getenv("PRIVATE_KEY"))
    password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
  }

  publishPlugin {
    token.set(System.getenv("PUBLISH_TOKEN"))
    channels.set(listOf("alpha"))
  }
}
