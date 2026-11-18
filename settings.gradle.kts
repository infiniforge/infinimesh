pluginManagement {
    resolutionStrategy.eachPlugin {
        if (requested.id.id.startsWith("com.android.")) useModule("com.android.tools.build:gradle:8.9.2")
    }
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}
rootProject.name = "InfiniMesh"
val dependencyRoot = file("libraries")
System.setProperty("infini.dependencies", dependencyRoot.canonicalPath)
includeBuild("libraries/libinfinistream")
includeBuild("libraries/libinfinemesh")
includeBuild("libraries/libinfiniselect")
