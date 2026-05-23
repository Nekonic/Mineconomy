dependencies {
    compileOnly(libs.paper.api)

    api(project(":mineconomy-api"))

    // DB
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.hikari)
    runtimeOnly(libs.mysql.connector)
    runtimeOnly(libs.mariadb.connector)

    // DI
    implementation(libs.koin.core)

    implementation(libs.coroutines.core)
}
