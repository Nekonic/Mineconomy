dependencies {
    compileOnly(libs.paper.api)

    api(project(":mineconomy-api"))

    // DB
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.hikari)
    runtimeOnly(libs.mysql.connector)

    // DI
    implementation(libs.koin.core)

    // 코루틴 (Paper 번들 버전과 호환 맞춤)
    compileOnly(libs.coroutines.core)
}
