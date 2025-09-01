import xyz.jpenilla.runtask.RunExtension

plugins {
    id("java")
    id("xyz.jpenilla.run-paper") version "2.0.0"
    id("com.gradleup.shadow") version "8.3.0"
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://ci.ender.zone/plugin/repository/everything/")
    }

    maven {
        url = uri("https://repo.aikar.co/content/groups/aikar/")
    }

    maven {
        url = uri("https://repo.dmulloy2.net/nexus/repository/public/")
    }

    maven {
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }

    maven {
        url = uri("https://jcenter.bintray.com")
    }

    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }

    maven {
        url = uri("https://jitpack.io")
    }

    maven {
        url = uri("https://nexus.wesjd.net/repository/thirdparty/")
    }

    maven {
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }

    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    compileOnly("net.kyori:adventure-api:4.17.0")
    compileOnly("net.kyori:adventure-platform-bukkit:4.3.3")
    compileOnly("com.google.code.gson:gson:2.11.0")
    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("me.clip:placeholderapi:2.11.6")

    implementation("dev.jorel:commandapi-bukkit-shade-mojang-mapped:10.1.2")
}



tasks.shadowJar {
    relocate("dev.jorel.commandapi", "net.crashcraft.crashpayment.commandapi")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks {
    runServer {
        minecraftVersion("1.21.4")
        dependsOn(shadowJar)

        pluginJars(shadowJar.get().archiveFile.filter { it.asFile.name.contains("-all") })
        // args("-add-plugin=${outputs.files.files.filter { it.name.contains("-all")}}")
    }
}

extensions.configure<RunExtension> {
    disablePluginJarDetection()
}

group = "net.crashcraft"
version = "1.0.3"
description = "CrashPayment"