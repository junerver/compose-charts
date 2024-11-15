import com.android.build.gradle.LibraryExtension
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.net.URI


plugins {
    alias(libs.plugins.android.library)
//    alias(libs.plugins.jetbrains.dokka)
    alias(libs.plugins.detekt.gradle.plugin)
//    id("maven-publish")
//    id("signing")
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

group = getProperty("GROUP_ID")
version = getProperty("COMPOSE_CHARTS_VERSION")

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions.freeCompilerArgs.addAll(
        listOf(
            "-Xexpect-actual-classes",
            "-Xconsistent-data-class-copy-visibility"
        )
    )

    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
        publishLibraryVariants("release")
    }

    jvm("desktop")

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "hooks"
            isStatic = true
        }
    }
    applyDefaultHierarchyTemplate()
    sourceSets {
        commonMain.dependencies {
            implementation(compose.ui)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.material3)
        }

        val commonJvmAndroid by creating {
            dependsOn(commonMain.get())
        }

        androidMain.get().dependsOn(commonJvmAndroid)
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }

        val desktopMain by getting {
            dependsOn(commonJvmAndroid)
        }

        commonTest.dependencies {
//            implementation(libs.kotlin.test)
//            implementation(libs.kotlin.test.junit)
        }
        androidInstrumentedTest.dependencies {
            implementation(libs.compose.ui.test)
            implementation(libs.compose.ui.test.junit4)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui.test.manifest)
        }
    }
}

android {
    namespace = "me.bytebeats.views.charts"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        lint {
            targetSdk = 34
        }
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")

    if (project.plugins.hasPlugin(libs.plugins.android.library.get().pluginId)) {
        val libExt = checkNotNull(project.extensions.findByType(LibraryExtension::class.java))
        val libMainSourceSet = libExt.sourceSets.getByName("main")

        from(libMainSourceSet.java.srcDirs)
    } else {
        val sourceSetExt =
            checkNotNull(project.extensions.findByType(SourceSetContainer::class.java))
        val mainSourceSet = sourceSetExt.getByName("main")

        from(mainSourceSet.java.srcDirs)
    }
}

tasks.withType(GenerateModuleMetadata::class).configureEach {
    dependsOn(sourcesJar)
}

//tasks.dokkaHtml {
//    outputDirectory.set(layout.buildDirectory.dir("dokka"))
//    moduleName.set(getProperty("MODULE_NAME"))
//    dokkaSourceSets {
//        configureEach {
//            suppress = false
//            offlineMode = false
//            includeNonPublic = false
//            skipDeprecated = true
//            skipEmptyPackages = true
//            noStdlibLink = true
//            noJdkLink = true
//            noAndroidSdkLink = false
//            jdkVersion = JavaVersion.VERSION_1_8.ordinal + 1
//        }
//    }
//}

//val dokkaHtml by tasks.getting(DokkaTask::class)
//
//val javadocJar by tasks.registering(Jar::class) {
//    dependsOn(dokkaHtml)
//    archiveClassifier.set("javadoc")
//    from(dokkaHtml.outputDirectory)
//}

fun Project.getProperty(key: String?, default: String? = null): String {
    checkPropertyKey(key)
    return properties[key]?.toString() ?: System.getProperty(key!!, default)
}

fun checkPropertyKey(key: String?) {
    if (key == null) {
        throw NullPointerException("key can't be null")
    }
    if (key.isBlank()) {
        throw IllegalArgumentException("key can't be blank")
    }
}

fun Project.checkSigningKey(signingKey: String?) {
    checkPropertyKey(signingKey)
    signingKey?.let { key ->
        if (hasProperty(key).not() && System.getProperties().containsKey(key).not()) {
            throw IllegalStateException("$signingKey has to be declared in local.properties or ~/.gradle/gradle.properties")
        }
    }
}

fun Project.getRepoUrl(): URI {
    val isSnapshot = getProperty("COMPOSE_CHARTS_VERSION").contains("SNAPSHOT")
    val releaseUrl = getProperty(
        "RELEASES_REPO_URL",
        "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
    )
    val snapshotUrl = getProperty(
        "SNAPSHOTS_REPO_URL",
        "https://s01.oss.sonatype.org/content/repositories/snapshots/"
    )
    return uri(if (isSnapshot) snapshotUrl else releaseUrl)
}

//afterEvaluate {
//    publishing {
//        publications {
//            // 1. configure repositories
//            repositories {
//                maven {
//                    name = project.getProperty("REPO_NAME")
//                    url = project.getRepoUrl()
//
//                    credentials {
//                        username = project.getProperty("ossrhUsername", "")
//                        password = project.getProperty("ossrhPassword", "")
//                    }
//                }
//            }
//
//            // 2. configure publication
//            val publicationName = project.getProperty("PUBLICATION_NAME", "release")
//            create<MavenPublication>(publicationName) {
//
//                if (project.plugins.hasPlugin(libs.plugins.android.library.get().pluginId)) {
//                    from(components["release"])
//                } else {
//                    from(components["java"])
//                }
//
//                artifact(sourcesJar.get())
//                artifact(javadocJar.get())
//
//                pom {
//                    groupId = project.getProperty("GROUP_ID")
//                    artifactId = project.getProperty("COMPOSE_CHARTS_ARTIFACT_ID")
//                    version = project.getProperty("COMPOSE_CHARTS_VERSION")
//                    inceptionYear = project.getProperty("COMPOSE_CHARTS_INCEPTION_YEAR")
//
//                    name = project.getProperty("MODULE_NAME")
//                    description = project.getProperty("COMPOSE_CHARTS_DESCRIPTION")
//                    url = project.getProperty("COMPOSE_CHARTS_URL")
//
//                    packaging = project.getProperty("COMPOSE_CHARTS_PACKAGING")
//
//                    scm {
//                        url = project.getProperty("SCM_URL")
//                        connection = project.getProperty("SCM_CONNECTION")
//                        developerConnection = project.getProperty("SCM_DEVELOPER_CONNECTION")
//                    }
//
//                    organization {
//                        name = project.getProperty("ORGANIZATION_NAME", "")
//                        url = project.getProperty("ORGANIZATION_URL", "")
//                    }
//
//                    developers {
//                        developer {
//                            id = project.getProperty("DEVELOPER_ID")
//                            name = project.getProperty("DEVELOPER_NAME")
//                            url = project.getProperty("DEVELOPER_URL")
//                            email = project.getProperty("DEVELOPER_EMAIL")
//                        }
//                    }
//
//                    licenses {
//                        license {
//                            name = project.getProperty("LICENSE_NAME")
//                            url = project.getProperty("LICENSE_URL")
//                            distribution = project.getProperty("LICENCE_DIST")
//                        }
//                    }
//
//                    issueManagement {
//                        system = project.getProperty("ISSUE_SYSTEM")
//                        url = project.getProperty("ISSUE_URL")
//                    }
//
//                    contributors {
//                        contributor {
//                            name = project.getProperty("CONTRIBUTOR_NAME")
//                            email = project.getProperty("CONTRIBUTOR_EMAIL")
//                            url = project.getProperty("CONTRIBUTOR_URL")
//                            roles.set(listOf("Master", "Maintainer", "Developer"))
//                            timezone = project.getProperty("CONTRIBUTOR_TIMEZONE")
//                        }
//                    }
//
//                    ciManagement {
//                        system = project.getProperty("CI_SYSTEM")
//                        url = project.getProperty("CI_URL")
//                    }
//
//                    distributionManagement {
//                        downloadUrl = getProperty("RELEASES_REPO_URL")
//                    }
//                }
//            }
//
//            // 3. sign the artifacts
//            signing {
//                // Choose one of both ways to sign the aar
//                // Signing with gpg
//                // and with its signing.keyId & signing.password & signing.secretKeyRingFile
//                // declared in local.properties or ~/.gradle/gradle.properties
////                checkSigningKey("signing.keyId")
////                checkSigningKey("signing.password")
////                checkSigningKey("signing.secretKeyRingFile")
//                sign(publishing.publications.getByName(publicationName))
//                // or signing with CI/CD
//                // and with its signingKeyId & signingKeyPassword & signingKey
//                // declared in local.properties or ~/.gradle/gradle.properties
////                checkSigningKey("signingKeyId")
////                checkSigningKey("signingKey")
////                checkSigningKey("signingKeyPassword")
////                val signingKeyId = getProperty("signingKeyId")
////                val signingKey = getProperty("signingKey")
////                val signingKeyPassword = getProperty("signingKeyPassword")
////                useInMemoryPgpKeys(signingKeyId, signingKey, signingKeyPassword)
////                sign(publishing.publications.getByName(publicationName))
//            }
//        }
//    }
//}
