pluginManagement {
    operator fun Settings.get(property: String): String {
        return org.gradle.api.internal.plugins.DslObject(this).asDynamicObject.getProperty(property) as String
    }

    repositories {
        gradlePluginPortal()
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        maven {
            name = "NeoForge"
            url = uri("https://maven.neoforged.net/releases/")
        }
        maven {
            name = "Architectury"
            url = uri("https://maven.architectury.dev/" )
        }
        mavenLocal()
    }

    plugins {
        id("org.jetbrains.kotlin.jvm") version settings["kotlin_version"]
        id("dev.architectury.loom") version settings["loom_version"]
        id ("org.ajoberstar.grgit") version settings["grgit_version"]
        id ("com.matthewprenger.cursegradle") version settings["cursegradle_version"]
        id ("com.modrinth.minotaur") version settings["modrinth_version"]
    }
}

rootProject.name = "kibe"