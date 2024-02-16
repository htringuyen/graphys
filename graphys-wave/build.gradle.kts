
plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":utilities"))

    //implementation(libs.spring.web)
    //implementation(libs.spring.core)
    testImplementation(libs.spring.context)

    implementation(libs.reactor.core)

    implementation(files("/usr/local/share/java/wfdb.jar"))

    implementation(libs.log4j.core)
    implementation(libs.log4j.api)

    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(libs.spring.test)

}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {

}

/*tasks.named<>("exec") {
    systemProperty("java.library.path", "/usr/local/lib")
}*/

tasks.named<Test>("test") {
    useJUnitPlatform()
    systemProperty("java.library.path", "/usr/local/lib")
}



