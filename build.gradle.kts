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

version = project["mod_version"].split("+")[0]
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
val fileClassifier = "${project["mod_loader"]}-${project["minecraft_version"]}"

val buildReleaseName = "${name.split("-").joinToString(" ") { it.capitalize() }} $version"
val buildReleaseType = (version as String).split("-").let { if(it.size > 1) if(it[1] == "BETA" || it[1] == "ALPHA") it[1] else "ALPHA" else "RELEASE" }
val buildReleaseFile = layout.buildDirectory.file("libs/${base.archivesName.get()}-$version-$fileClassifier.jar").get()
val buildSourcesFile = layout.buildDirectory.file("libs/${base.archivesName.get()}-$version-$fileClassifier-sources.jar").get()
val buildGameVersion = project["mod_version"].split("+")[1].let{ if(!project["minecraft_version"].contains("-") && project["minecraft_version"].startsWith(it)) project["minecraft_version"] else "$it-Snapshot"}

println("===============[ BUILD DATA ]===============")
println("- Release name: $buildReleaseName")
println("- Release type: $buildReleaseType")
println("- Release file: $buildReleaseFile")
println("- Sources file: $buildSourcesFile")
println("- Game version: $buildGameVersion")
println("============================================")

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

val generatedResources = file("src/main/generated")

sourceSets {
    main {
        resources.srcDir(generatedResources)
    }
}

loom {
    runs.create("data") {
        data()
        programArgs("--all", "--mod", "kibe")
        programArgs("--output", generatedResources.absolutePath)
    }
    accessWidenerPath.set(file("src/main/resources/kibe.accesswidener"))
}

repositories {
    maven {
        name = "Fabric"
        url = uri("https://maven.fabricmc.net/")
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
    maven {
        name = "Illusive Soulworks maven"
        url = uri("https://maven.theillusivec4.top/")
    }
    flatDir {
        dirs("libs")
    }
    mavenLocal()
}

dependencies {
    minecraft("com.mojang:minecraft:${project["minecraft_version"]}")
    neoForge("net.neoforged:neoforge:${project["neoforge_version"]}")
    mappings(loom.layered {
        mappings("net.fabricmc:yarn:${project["yarn_mappings"]}:v2")
        mappings("dev.architectury:yarn-mappings-patch-neoforge:${project["yarn_mappings_patch_version"]}")
        mappings(file("fix_datagen.tiny"))
    })

    modImplementation("org.sinytra.forgified-fabric-api:forgified-fabric-api:${project["fabric_version"]}")
    modImplementation("thedarkcolour:kotlinforforge-neoforge:${project["kotlin_forge_version"]}")
    forgeRuntimeLibrary("org.jetbrains.kotlin:kotlin-stdlib:2.2.20")
    forgeRuntimeLibrary("org.jetbrains.kotlin:kotlin-reflect:2.2.20")
    forgeRuntimeLibrary("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    forgeRuntimeLibrary("org.jetbrains.kotlinx:kotlinx-serialization-core:1.9.0")
    forgeRuntimeLibrary("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    modCompileOnly("top.theillusivec4.curios:curios-neoforge:${project["curios_version"]}:api")
    modRuntimeOnly("top.theillusivec4.curios:curios-neoforge:${project["curios_version"]}")

    modImplementation("blank:pal-neoforge:${project["pal_version"]}")
    include("blank:pal-neoforge:${project["pal_version"]}")
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    inputs.property("version", project.version)

    from(sourceSets["main"].resources.srcDirs) {
        include("META-INF/neoforge.mods.toml")
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

tasks.remapSourcesJar {
    archiveClassifier.set("$fileClassifier-sources")
}

tasks.remapJar {
    archiveClassifier.set(fileClassifier)
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
        addGameVersion(project["mod_loader"].capitalize())

        mainArtifact(file(buildReleaseFile), closureOf<CurseArtifact> {
            displayName = buildReleaseName
            relations(closureOf<CurseRelation> {
                embeddedLibrary("pal")
                if(project["mod_loader"] == "fabric") {
                    requiredDependency("fabric-api")
                    requiredDependency("fabric-language-kotlin")
                }else{
                    requiredDependency("forgified-fabric-api")
                    requiredDependency("kotlin-for-forge")
                }
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
    loaders.add(project["mod_loader"])

    dependencies {
        if(project["mod_loader"] == "fabric")
            required.project("fabric-api")
        else
            required.project("forgified-fabric-api")
    }
}

tasks.modrinth.configure {
    group = "upload"
}