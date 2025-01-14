import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.ByteArrayOutputStream

fun File.resolveArchPath(target: KonanTarget): File? {
    return resolve(
        if (target is KonanTarget.IOS_ARM64) "ios-arm64" else "ios-arm64_x86_64-simulator"
    )
}

fun org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget.configureIos(frameworks: List<String>) {
    compilations.getByName("main") {

        frameworks.forEach {
            val xcPath =
                projectDir.resolve("src/nativeInterop/cinterop/xcframeworks/$it.xcframework")
                    .resolveArchPath(
                        konanTarget,
                    )
            println("xcPath: $xcPath")

            val xc = cinterops.create(it) {
                defFile("src/nativeInterop/cinterop/$it.def")

                compilerOpts("-framework", it, "-F$xcPath/")
                extraOpts += listOf("-compiler-option", "-fmodules")
            }

            println("xc:$xc")

            this@configureIos.binaries.all {
                linkerOpts(
                    "-framework", it, "-F$xcPath/",// "-rpath", "$xcPath", "-ObjC"
                )
            }
        }
    }
}
tasks.register("cinteropSync") {
    dependsOn(
        "cinteropDwebPlatformIosKitIosArm64",
        "cinteropDwebPlatformIosKitIosSimulatorArm64",
        "cinteropDwebPlatformIosKitIosX64",
        "commonizeCInterop",
        "copyCommonizeCInteropForIde",
        "commonize",
        "transformNativeMainCInteropDependenciesMetadataForIde",
        "transformAppleMainCInteropDependenciesMetadataForIde",
        "transformIosMainCInteropDependenciesMetadataForIde",
        "transformNativeTestCInteropDependenciesMetadataForIde",
        "transformAppleTestCInteropDependenciesMetadataForIde",
        "transformIosTestCInteropDependenciesMetadataForIde",
        "prepareKotlinIdeaImport",
    )
}

/// 项目一开始要做sync的时候
gradle.projectsEvaluated {
    println("beforeProject: $name")

    if (OperatingSystem.current().isMacOsX) {
        val output = ByteArrayOutputStream()

        exec {
            commandLine("which", "deno")
            standardOutput = output
            isIgnoreExitValue = true
        }

        val denoBinPath = output.toString().trim()
        val userHome = System.getProperty("user.home")
        val deno = listOf(
            denoBinPath, "$userHome/.deno/bin/deno", "/opt/homebrew/bin/deno"
        ).filter { it.isNotEmpty() }.firstOrNull { file(it).exists() } ?: "deno"

        exec {
            workingDir = projectDir.resolve("./src/nativeInterop/cinterop")
            commandLine(deno, "run", "-A", "./build.ts")
        }
    }
}
