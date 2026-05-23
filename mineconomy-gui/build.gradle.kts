plugins {
    alias(libs.plugins.shadow)
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.vault.api)

    implementation(project(":mineconomy-core"))
    runtimeOnly(libs.mysql.connector)
    runtimeOnly(libs.mariadb.connector)

    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.hikari)
    implementation(libs.koin.core)
    implementation(libs.coroutines.core)
}

tasks {
    processResources {
        filesMatching("plugin.yml") {
            expand("version" to project.version)
        }
    }

    shadowJar {
        archiveBaseName.set("Mineconomy")
        archiveClassifier.set("")
        archiveVersion.set(project.version.toString())
        destinationDirectory.set(rootProject.layout.projectDirectory.dir("dist"))

        dependencies {
            exclude(dependency("org.jetbrains:annotations"))
        }

        // NOTE: relocate()는 shadow 플러그인이 Kotlin 2.1 @Metadata(int[]) 처리를
        //       지원하는 버전이 나오면 추가할 것.
        //   relocate("org.jetbrains.exposed", "mineconomy.libs.exposed")
        //   relocate("com.zaxxer.hikari",     "mineconomy.libs.hikari")
        //   relocate("org.koin",              "mineconomy.libs.koin")
        //   relocate("com.mysql",             "mineconomy.libs.mysql")

        mergeServiceFiles()
    }

    assemble {
        dependsOn(shadowJar)
    }

    jar {
        enabled = false
    }
}