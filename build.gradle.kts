/*
 * This file was generated by the Gradle 'init' task.
 */

val skwVersion = "4.5b198"
val lughlibVersion = "4.0.4"

plugins {
    java
    `maven-publish`
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }
    maven("https://repository.jboss.org/nexus/content/groups/public-jboss")
    maven("https://repository.jboss.org/nexus/content/repositories")
    maven("https://repository.jboss.org/nexus/content/repositories/thirdparty-releases")
    maven {
        url = uri("http://sankhyatec.mgcloud.net.br/api/v4/projects/173/packages/maven")
        isAllowInsecureProtocol =  true
        name = "GitLab"
        metadataSources {
            artifact()
            mavenPom()
        }
        credentials(HttpHeaderCredentials::class.java) {
            name = "Private-Token"
            value = "YzDkSQZrWVnXYzG1RMQN"
        }
        authentication {
            create<HttpHeaderAuthentication>("header")
        }
    }

    flatDir {
        dirs("lib")
    }
}

extensions.findByName("buildScan")?.withGroovyBuilder {
    setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
    setProperty("termsOfServiceAgree", "yes")
}

dependencies {
    implementation("org.jboss.spec.javax.ejb", "jboss-ejb-api_3.2_spec", "1.0.1.Final")

    implementation("br.com.lughconsultoria", "lugh-lib", lughlibVersion)
    implementation("br.com.lughconsultoria", "lugh-lib-annotation", lughlibVersion)
    annotationProcessor("br.com.lughconsultoria", "lugh-lib-processor", lughlibVersion)

    implementation("br.com.sankhya.extensions:snkwext:1.0")

    implementation("org.jdom", "jdom", "1.1.3")
    implementation("br.com.sankhya", "mge-modelcore", skwVersion)
    implementation("org.apache.commons", "commons-lang3", "3.0")
    implementation("br.com.sankhya", "jape", skwVersion)
    implementation("br.com.sankhya", "dwf", skwVersion)
    implementation("br.com.sankhya", "mgecom-model", skwVersion)
    implementation("br.com.sankhya", "sanutil", skwVersion)
    implementation("br.com.sankhya", "sanws", skwVersion)
    implementation("br.com.sankhya", "mgeworkflow-model", skwVersion)
    implementation("br.com.sankhya", "mge-param", skwVersion)
    implementation("br.com.sankhya", "mge-modulemgr", skwVersion)
    implementation("br.com.sankhya", "mgefin-model", skwVersion)
    implementation("br.com.sankhya", "mgeprod-model", skwVersion)

    implementation("com.google.code.gson:gson:2.8.5")
    implementation("org.json:json:20190722")
    implementation("javax.servlet:servlet-api:2.5")
    implementation("org.apache.httpcomponents:httpclient:4.5.13")
}

group = "br.com.goup.snkintegrations"
version = "1.0.31-SNAPSHOT"
description = "Sankhya Custom Events Programmer"
java.sourceCompatibility = JavaVersion.VERSION_1_8

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}
