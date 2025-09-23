import com.matthewprenger.cursegradle.CurseArtifact
import com.matthewprenger.cursegradle.CurseProject
import com.matthewprenger.cursegradle.CurseRelation
import com.matthewprenger.cursegradle.Options
import org.ajoberstar.grgit.Grgit
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.kohsuke.github.GHReleaseBuilder
import org.kohsuke.github.GitHub

buildscript {
    dependencies {
        classpath("org.kohsuke:github-api:${project.property("github_api_version") as String}")
    }
}

plugins {
    id("maven-publish")
    id("dev.architectury.loom")
    id("org.ajoberstar.grgit")
    id("org.jetbrains.kotlin.jvm")
    id("com.matthewprenger.cursegradle")
    id("com.modrinth.minotaur")
}

operator fun Project.get(property: String): String {
    return property(property) as String
}

version = project["mod_version"]
group = project["maven_group"]

fun String.capitalize(): String {
    return replaceFirstChar {
        if (it.isLowerCase())
            it.titlecase()
        else
            it.toString()
    }
}

val systemEnvironment: Map<String, String> = System.getenv()
val buildReleaseName = "${name.split("-").joinToString(" ") { it.capitalize() }} ${(version as String).split("+")[0]}"
val buildReleaseType = (version as String).split("+")[0].split("-").let { if(it.size > 1) if(it[1] == "BETA" || it[1] == "ALPHA") it[1] else "ALPHA" else "RELEASE" }
val buildReleaseFile = layout.buildDirectory.file("libs/${base.archivesName.get()}-${version}.jar").get()
val buildGameVersion = (version as String).split("+")[1].let{ if(!project["minecraft_version"].contains("-") && project["minecraft_version"].startsWith(it)) project["minecraft_version"] else "$it-Snapshot"}

fun getChangeLog(): String {
    return "A changelog can be found at https://github.com/lucaargolo/$name/commits/"
}

fun getBranch(): String {
    systemEnvironment["GITHUB_REF"]?.let { branch ->
        return branch.substring(branch.lastIndexOf("/") + 1)
    }
    val grgit = try {
        extensions.getByName("grgit") as Grgit
    }catch (_: Exception) {
        return "unknown"
    }
    val branch = grgit.branch.current().name
    return branch.substring(branch.lastIndexOf("/") + 1)
}

loom {
    forge {
        mixinConfig("kibe.mixins.json")
    }
    accessWidenerPath.set(file("src/main/resources/kibe.accesswidener"))
}

repositories {
    maven {
        name = "Fabric"
        url = uri("https://maven.fabricmc.net/")
    }
    maven {
        name = "Ladysnake Mods"
        url = uri("https://maven.ladysnake.org/releases")
    }
    maven {
        name = "JitPack"
        url = uri("https://jitpack.io")
    }
    maven {
        name = "Dashloader"
        url = uri("https://oskarstrom.net/maven")
    }
    maven {
        name = "TerraformersMC"
        url = uri("https://maven.terraformersmc.com/releases")
    }
    maven {
        name = "Shedaniel"
        url = uri("https://maven.shedaniel.me/")
    }
    maven {
        name = "NeoForge"
        url = uri("https://maven.neoforged.net/releases/")
    }
    maven {
        name = "Architectury"
        url = uri("https://maven.architectury.dev/" )
    }
    maven {
        name = "Kotlin for Forge"
        setUrl("https://thedarkcolour.github.io/KotlinForForge/")
    }
    maven {
        name = "Forgified Fabric API"
        url = uri("https://maven.su5ed.dev/releases")
    }
    mavenLocal()
}

dependencies {
    minecraft("com.mojang:minecraft:${project["minecraft_version"]}")
    mappings("net.fabricmc:yarn:${project["yarn_mappings"]}:v2")
    forge("net.neoforged:forge:${project["neoforge_version"]}")

    modImplementation("dev.su5ed.sinytra.fabric-api:fabric-api:${project["fabric_version"]}")
    implementation("thedarkcolour:kotlinforforge:4.10.0")

    modImplementation("dev.emi:trinkets:${project["trinkets_version"]}")

    modImplementation("io.github.ladysnake:PlayerAbilityLib:${project["pal_version"]}")
    include("io.github.ladysnake:PlayerAbilityLib:${project["pal_version"]}")

    annotationProcessor("io.github.llamalad7:mixinextras-common:0.3.6")
    compileOnly("io.github.llamalad7:mixinextras-common:0.3.6")

    implementation("io.github.llamalad7:mixinextras-forge:0.3.6")
    include("io.github.llamalad7:mixinextras-forge:0.3.6")
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    inputs.property("version", project.version)

    from(sourceSets["main"].resources.srcDirs) {
        include("META-INF/mods.toml")
        expand(mutableMapOf("version" to project.version))
    }

}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.withType<KotlinCompile> {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
}

java {
    withSourcesJar()
}

tasks.jar {
    from("LICENSE")
}

//Github publishing
tasks.register("github") {
    dependsOn(tasks.remapJar)
    group = "upload"

    onlyIf { systemEnvironment.containsKey("GITHUB_TOKEN") }

    doLast {
        val github = GitHub.connectUsingOAuth(systemEnvironment["GITHUB_TOKEN"])
        val repository = github.getRepository(systemEnvironment["GITHUB_REPOSITORY"])

        val releaseBuilder = GHReleaseBuilder(repository, version as String)
        releaseBuilder.name(buildReleaseName)
        releaseBuilder.body(getChangeLog())
        releaseBuilder.commitish(getBranch())

        val ghRelease = releaseBuilder.create()
        ghRelease.uploadAsset(file(buildReleaseFile), "application/java-archive")
    }
}

//Curseforge publishing
curseforge {
    systemEnvironment["CURSEFORGE_API_KEY"]?.let { apiKey = it }

    project(closureOf<CurseProject> {
        id = project["curseforge_id"]
        changelog = getChangeLog()
        releaseType = buildReleaseType.lowercase()
        addGameVersion(buildGameVersion)
        addGameVersion("Fabric")

        mainArtifact(file(buildReleaseFile), closureOf<CurseArtifact> {
            displayName = buildReleaseName
            relations(closureOf<CurseRelation> {
                embeddedLibrary("pal")
                optionalDependency("roughly-enough-items")
                requiredDependency("fabric-api")
                requiredDependency("fabric-language-kotlin")
            })
        })

        afterEvaluate {
            uploadTask.dependsOn("remapJar")
        }

    })

    options(closureOf<Options> {
        forgeGradleIntegration = false
    })
}

//Modrinth publishing
modrinth {
    systemEnvironment["MODRINTH_TOKEN"]?.let { token.set(it) }

    projectId.set(project["modrinth_id"])
    changelog.set(getChangeLog())

    versionNumber.set(version as String)
    versionName.set(buildReleaseName)
    versionType.set(buildReleaseType.lowercase())

    uploadFile.set(tasks.remapJar.get())

    gameVersions.add(project["minecraft_version"])
    loaders.add("fabric")

    dependencies {
        required.project("fabric-api")
    }
}
tasks.modrinth.configure {
    group = "upload"
}