plugins {
    id("java")
}

group = "org.example"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    //implementation(libs.spring.web)
    testImplementation(libs.log4j.core)
    testImplementation(libs.log4j.api)
    //testImplementation(libs.spring.core)
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation(libs.junit.jupiter)
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}