# Mineconomy

Minecraft Paper 경제 플러그인. 상세 기획은 `docs/CONCEPT.md` 참고.

## 빌드 / 실행

```bash
./gradlew build                        # 전체 빌드
./gradlew :mineconomy-core:build       # 코어만 빌드
./gradlew :mineconomy-gui:build        # GUI만 빌드
./gradlew test                         # 테스트 실행
./gradlew shadowJar                    # 배포용 fat jar 생성
```

## 모듈 구조

```
mineconomy-api    # 외부 연동 인터페이스, Vault 호환
mineconomy-core   # AMM 엔진, 주식, 금융, DB, 스케줄러
mineconomy-gui    # 체스트 UI, 차트 렌더링
resource-pack/    # Git submodule
```

의존 방향: `gui → core → api` (역방향 금지)

## 코드 스타일

- 
- 
- 언어: Kotlin, Java 25
- 금액: 항상 `Long` (`Double`/`Float` 금지)
- 수수료 계산: `amount * 25 / 1000` (버림)
- 로깅: `plugin.getSLF4JLogger()` (`getLogger()` 금지)
- 패키지: `mineconomy.[module].[feature]`

## 스레드 규칙

- Bukkit API는 main thread에서만 호출
- DB 작업은 `Dispatchers.IO` 코루틴으로 처리
- main thread 복귀: `Bukkit.getScheduler().runTask()`

## 주요 기술 스택

- Paper 26.1.x / Kotlin / Gradle Kotlin DSL
- DB: MySQL + HikariCP + Exposed DSL
- DI: Koin
- 커맨드: Brigadier (레거시 `CommandExecutor` 금지)
- GUI: 커스텀 모델 데이터 방식 직접 구현

## 참고

- 기획 전체: `docs/CONCEPT.md`
- Paper API: https://jd.papermc.io/paper/1.21/
- Exposed: https://jetbrains.github.io/Exposed/
