
plugins {
    application
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(libs.guava)

    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

application {
    mainClass = "dev.ogblackdiamond.proxyportals.ProxyPortals"
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = application.mainClass
    }
}
