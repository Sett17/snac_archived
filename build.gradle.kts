plugins {
  kotlin("multiplatform") version "1.7.10"
  kotlin("plugin.serialization") version "1.6.21"
  application
}

group = "de.okedikka"
version = "1.0"

repositories {
  mavenCentral()
  maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
  maven("https://jitpack.io")
}

val ktor_version = "2.0.3"

kotlin {
  jvm {
    compilations.all {
      kotlinOptions.jvmTarget = "11"
    }
    withJava()
  }
  js(IR) {
    binaries.executable()
    browser {
      commonWebpackConfig {
        cssSupport.enabled = true
      }
    }
  }
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.3")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
        implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
      }
    }
    val commonTest by getting
    val jvmMain by getting {
      dependencies {
        implementation("com.github.nwillc.ksvg:ksvg:master-SNAPSHOT")
        implementation("io.ktor:ktor-server-cio:$ktor_version")
        implementation("io.ktor:ktor-server-status-pages:$ktor_version")
        implementation("io.ktor:ktor-server-compression:$ktor_version")
        implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
        implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
        implementation("io.ktor:ktor-server-call-logging:$ktor_version")
        implementation("io.ktor:ktor-server-html-builder-jvm:$ktor_version")
        implementation("io.ktor:ktor-server-sessions:$ktor_version")
        implementation("io.ktor:ktor-server-caching-headers:$ktor_version")
        implementation("io.ktor:ktor-server-auth:$ktor_version")
        implementation("io.ktor:ktor-server-auth-jwt:$ktor_version")
        implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.2")
        implementation("org.postgresql:postgresql:42.4.0")
        implementation("ch.qos.logback:logback-classic:1.2.11")
        implementation("com.sksamuel.hoplite:hoplite-core:2.3.1")
        implementation("com.sksamuel.hoplite:hoplite-yaml:2.3.1")
      }
    }
    val jvmTest by getting
    val jsMain by getting {
      dependencies {
        implementation("io.ktor:ktor-client-core:$ktor_version")
        implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
        implementation("io.ktor:ktor-client-js:$ktor_version")
        implementation("io.ktor:ktor-client-json:$ktor_version")
        implementation("io.ktor:ktor-client-auth:$ktor_version")
        implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
        implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.7.2")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.6.3")
      }
    }
    val jsTest by getting
  }
}

application {
  mainClass.set("de.okedikka.application.ServerKt")
}

tasks.named<Copy>("jvmProcessResources") {
  val jsBrowserDistribution = tasks.named("jsBrowserDistribution")
  from(jsBrowserDistribution)
}

distributions {
  main {
    contents {
      from("config-template.yaml", "README.md")
    }
  }
}

tasks.named<JavaExec>("run") {
  dependsOn(tasks.named<Jar>("jvmJar"))
  classpath(tasks.named<Jar>("jvmJar"))
}