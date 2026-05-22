import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.shadow)     apply false
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    group   = "mineconomy"
    version = "1.0.0-SNAPSHOT"

    // JDK 25 toolchain: Paper 26 API 다운로드 및 실행용
    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
        }
    }

    // 의존성 해석을 JVM 25로 강제 (Paper 26 API가 JVM 25 요구)
    configurations.matching { it.isCanBeResolved }.configureEach {
        attributes {
            attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 25)
        }
    }

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://jitpack.io")
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget = JvmTarget.fromTarget("24")
            freeCompilerArgs.add("-Xjvm-default=all")
        }
    }

    tasks.withType<JavaCompile> {
        sourceCompatibility = "24"
        targetCompatibility = "24"
    }
}
