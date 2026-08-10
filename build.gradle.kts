plugins {
	kotlin("jvm") version "2.3.21"
	kotlin("plugin.spring") version "2.3.21"
	id("org.springframework.boot") version "4.1.0"
	id("io.spring.dependency-management") version "1.1.7"
	`jvm-test-suite`
}

group = "com.rodgalan"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-flyway")
	implementation("org.springframework.boot:spring-boot-starter-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-restclient")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.flywaydb:flyway-database-postgresql")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("tools.jackson.module:jackson-module-kotlin")
	runtimeOnly("org.postgresql:postgresql")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

testing {
	suites {
		val test by getting(JvmTestSuite::class) {
			useJUnitJupiter()
			dependencies {
				implementation("org.jetbrains.kotlin:kotlin-test-junit5")
				runtimeOnly("org.junit.platform:junit-platform-launcher")
			}
		}

		register<JvmTestSuite>("integrationTest") {
			useJUnitJupiter()
			dependencies {
				implementation(project())
				implementation("org.springframework.boot:spring-boot-starter-flyway-test")
				implementation("org.springframework.boot:spring-boot-starter-jdbc-test")
				implementation("org.springframework.boot:spring-boot-starter-restclient-test")
				implementation("org.springframework.boot:spring-boot-starter-webmvc-test")
				implementation("org.jetbrains.kotlin:kotlin-test-junit5")
				runtimeOnly("org.junit.platform:junit-platform-launcher")
			}
		}
	}
}
