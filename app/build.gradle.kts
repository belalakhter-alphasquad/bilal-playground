plugins {
    application
}

application {
    mainClass.set("playground.app.Main") 
}


repositories {
    mavenCentral()
}

val generatedDir = file("${buildDir}/generated/src/main/java")
val codecGeneration = configurations.create("codecGeneration")

dependencies {
    "codecGeneration"("uk.co.real-logic:sbe-tool:1.29.0")
    implementation("org.agrona:agrona:1.20.0")
    implementation("io.aeron:aeron-cluster:1.43.0")
    implementation("io.aeron:aeron-driver:1.43.0")
    implementation("io.aeron:aeron-samples:1.43.0")
    implementation("org.slf4j:slf4j-api:2.1.0-alpha1")   
    implementation("com.google.code.gson:gson:2.8.9")
   implementation("ch.qos.logback:logback-classic:1.4.0") 
    implementation("org.slf4j:slf4j-api:2.1.0-alpha1")
    implementation("org.jline:jline:3.20.0")

}


sourceSets {
    main {
        java {
            srcDir(generatedDir)
        }
    }
}


tasks.register("generateCodecs", JavaExec::class) {
    group = "sbe"
    val codecsFile = "src/main/java/playground/app/sbe/protocol-codecs.xml"
    val sbeFile = "src/main/java/playground/app/sbe/sbe.xsd"
    inputs.files(codecsFile, sbeFile)
    outputs.dir(generatedDir)
    classpath = codecGeneration
    mainClass.set("uk.co.real_logic.sbe.SbeTool")
    args = listOf(codecsFile)
    systemProperties["sbe.output.dir"] = generatedDir
    systemProperties["sbe.target.language"] = "Java"
    systemProperties["sbe.validation.xsd"] = sbeFile
    systemProperties["sbe.validation.stop.on.error"] = "true"
    outputs.dir(generatedDir)
}


tasks.named<Jar>("jar") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    manifest {
        attributes(
            "Main-Class" to "playground.app.Main",
            "Add-Opens" to "java.base/sun.nio.ch",
            "Implementation-Title" to "Bilal's Playground",
            "Implementation-Version" to project.version
        )
    }
    archiveBaseName.set("app")
    archiveVersion.set("1.0.0")
    from(sourceSets.main.get().output)

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

tasks.named<JavaExec>("run") {
  dependsOn("generateCodecs", "jar")
    mainClass.set("playground.app.Main") 
    classpath = files(tasks.getByName<Jar>("jar").archiveFile.get())
    jvmArgs("--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED")
}



 
tasks.named<JavaCompile>("compileJava") {
    dependsOn("generateCodecs")
}

tasks.named("build") {
    dependsOn("generateCodecs")
    dependsOn("jar")
}




tasks.register<JavaExec>("app") {
    dependsOn("generateCodecs", "jar")
    mainClass.set("playground.app.Main") 
    classpath = files(tasks.getByName<Jar>("jar").archiveFile.get())
    jvmArgs("--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED")
}

