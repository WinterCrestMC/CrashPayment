plugins {
    `java-library`
    `maven-publish`
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
    api(libs.net.kyori.adventure.api)
    api(libs.net.kyori.adventure.platform.bukkit)
    api(libs.com.google.code.gson.gson)
    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")
    compileOnly(libs.com.github.milkbowl.vaultapi)
    compileOnly("me.clip:placeholderapi:2.11.6")
}

group = "net.crashcraft"
version = "1.0.2"
description = "CrashPayment"

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}
