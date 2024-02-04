plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":utilities"))

    implementation(libs.spring.web)
    implementation(libs.spring.core)
    implementation(libs.spring.context)

    implementation(libs.reactor.core)

    implementation(libs.log4j.core)
    implementation(libs.log4j.api)

    testImplementation(libs.junit.jupiter)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    //mainClass.set("")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}


