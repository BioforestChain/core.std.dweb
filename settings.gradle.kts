enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        /// 添加 amper 依赖
        maven("https://packages.jetbrains.team/maven/p/amper/amper")
        maven("https://www.jetbrains.com/intellij-repository/releases")
        maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies")
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    // apply the plugin:
    id("org.jetbrains.amper.settings.plugin").version("0.4.0")
}

class PlatformFactory {
    val osName = System.getProperty("os.name")
    val osArch = System.getProperty("os.arch")

    init {
        println("platform-os-name=$osName")
        println("platform-os-arch=$osArch")
    }

    val isMac = osName.startsWith("Mac")
    val isWindows = osName.startsWith("Windows")
    val isLinux = osName.startsWith("Linux")

    /// 寄存器宽度
    val is64 = listOf("amd64", "x86_64", "aarch64").contains(osArch)
    val is32 = listOf("x86", "arm").contains(osArch)

    /// 指令集
    val isX86 = setOf("x86", "i386", "amd64", "x86_64").contains(osArch)
    val isArm = listOf("aarch64", "arm").contains(osArch)
}

val platform = PlatformFactory()

class FeaturesFactory {
    private val props = java.util.Properties().also { properties ->
        rootDir.resolve("local.properties").apply {
            if (exists()) {
                inputStream().use { properties.load(it) }
            }
        }
    }

    private val disabled = props.getProperty("app.disable", "")
        .split(",")
        .map { it.trim().lowercase() };

    private val enabled = props.getProperty("app.experimental.enabled", "")
        .split(",")
        .map { it.trim().lowercase() };

    class Bool(val enabled: Boolean) {
        val disabled = !enabled
    }

    val androidApp = Bool(!disabled.contains("android"))
    val iosApp = Bool(platform.isMac && !disabled.contains("ios"));
    val desktopApp = Bool(!disabled.contains("desktop"));
    val extlibs = Bool(androidApp.enabled || iosApp.enabled || desktopApp.enabled)

    init {
        println("androidApp.enabled=${androidApp.enabled}")
        println("iosApp.enabled=${iosApp.enabled}")
        println("desktopApp.enabled=${desktopApp.enabled}")
        println("libs.enabled=${extlibs.enabled}")
    }
}

val features = FeaturesFactory()

rootProject.name = "subsoil"

/**
 * 导入依赖
 */
File(rootDir, "./modules").listFiles { file -> file.isDirectory }
    ?.forEach { dir ->
        if (File(dir, "module.yaml").exists()) {
            include(":modules/${dir.name}")
        }
    }

/**导入app*/
fun includeApp(dirName: String) {
    include(dirName)
    project(":$dirName").projectDir = file("app/$dirName")
}

/**
 * 导入app
 */
includeApp("androidApp")
includeApp("iosApp")

if (features.extlibs.enabled) {
    File(
        rootDir,
        "/Volumes/developer/waterbang/deno/dweb_browser/toolkit/dweb_browser_libs/rust_library"
    ).listFiles { file -> file.isDirectory }
        ?.forEach { dir ->
            if (File(dir, "build.gradle.kts").exists()) {
                if (dir.name == "biometrics" && features.desktopApp.disabled) {
                    return@forEach
                }
                include(dir.name)
                project(":${dir.name}").apply {
                    name = "lib_${dir.name}"
                    projectDir = file(dir)
                    buildFileName = "build-mobile.gradle.kts"
                }
            }
        }
}

