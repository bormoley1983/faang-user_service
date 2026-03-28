plugins {
    java
    jacoco
    id("org.springframework.boot") version "4.0.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jsonschema2pojo") version "1.2.1"
}

group = "faang.school"
version = "1.0"

val javaVersion = 25

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2025.1.1")
        mavenBom("org.testcontainers:testcontainers-bom:2.0.3")
    }
}

dependencies {
    /**
     * Spring boot starters
     */
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-restclient")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("org.springframework.kafka:spring-kafka")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.1")

    /**
     * Database
     */
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.liquibase:liquibase-core")
    
    /**
     * Quartz Scheduler
     */
    implementation("org.quartz-scheduler:quartz:2.3.2")


    /**
     * Amazon S3
     */
    implementation(platform("software.amazon.awssdk:bom:2.41.27"))
    implementation("software.amazon.awssdk:s3")     
    implementation("software.amazon.awssdk:url-connection-client")


     /**
     * Utils & Logging
     */ 
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    implementation("org.mapstruct:mapstruct:1.6.3")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

    implementation("org.codehaus.janino:janino:3.1.11")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv")
    
    /**
     * Test Containers
     */
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.testcontainers:junit-jupiter:1.21.4")
    testImplementation("org.testcontainers:postgresql:1.21.4")
    testImplementation("org.testcontainers:kafka:1.21.4")
    testImplementation("com.redis:testcontainers-redis:2.2.4")

    /**
     * Awaitility
     */
    testImplementation("org.awaitility:awaitility:4.3.0")

    /**
     * Thumbnail generation
     */
    implementation("net.coobird:thumbnailator:0.4.20")

    /**
     * Kafka Events
     */
    implementation("io.github.narol01:kafkaEvent:1.0.3")
}

jsonSchema2Pojo {
    setSource(files("src/main/resources/json"))
    targetDirectory = layout.buildDirectory.dir("generated-sources/js2p").get().asFile
    targetPackage = "com.json.student"
    setSourceType("jsonschema")
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
}

tasks.bootJar {
    archiveFileName.set("service.jar")
}

jacoco {
    toolVersion = "0.8.14"
}

tasks.named<Test>("test") {
    useJUnitPlatform {
        excludeTags("integration")
    }
    testLogging.showStandardStreams = true
    finalizedBy(tasks.named("jacocoTestReport"))
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(false)
        csv.required.set(false)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco"))
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            element = "CLASS"
            includes = listOf("school.faang.user_service.service.*")
            limit {
                counter = "INSTRUCTION"
                value = "COVEREDRATIO"
                minimum = "0.70".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}
