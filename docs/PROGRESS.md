# Mineconomy 개발 진행 현황

> 마지막 업데이트: 2026-05-23 / 최신 태그: v0.1.4

---

## 완료된 작업

### 인프라 / 빌드
- [x] Gradle Kotlin DSL 멀티모듈 프로젝트 세팅 (`settings.gradle.kts`, `libs.versions.toml`)
- [x] 모듈 3개 구성: `mineconomy-api` → `mineconomy-core` → `mineconomy-gui`
- [x] Paper 26.1.2 (JVM 25 요구) + Kotlin 2.2.0 (max JVM target 24) 호환 workaround
  - toolchain = 25, compile target = 24, dependency resolution attributes 강제 25
- [x] Shadow JAR 출력 경로 `dist/` 고정 (`Mineconomy-1.0.0-SNAPSHOT.jar` ~11MB)
- [x] `relocate()` 비활성화 (shadow 8.3.x + Kotlin 2.2.x `@Metadata(int[])` 버그 회피)
- [x] `.gitignore` — `dist/`, `build/`, `.gradle/`, `.idea/`, `*.class`

### GitHub Actions CI/CD
- [x] 태그(`v*`) push 시 자동 빌드 + GitHub Release 생성
- [x] `resource-pack` Git submodule 정상 등록 (v0.1.4에서 수정)
- [x] Release artifact: `Mineconomy-*.jar` + `Mineconomy-Resource-Pack.zip`

### 코드 골격
- [x] `mineconomy.api.MineconomyApi` — 잔액 조회 / 이체 인터페이스
- [x] `mineconomy.api.VaultEconomyAdapter` — Vault Economy 구현체 (String deprecated 메서드 포함)
- [x] `mineconomy.core.MineconomyPlugin` — 진입점 플러그인 (stub)
- [x] `mineconomy.gui.MineconomyGuiPlugin` — 배포 진입점 플러그인 (stub)
- [x] `plugin.yml` — `api-version: '26.1'`, main: `mineconomy.gui.MineconomyGuiPlugin`

---

## 미구현 (구현 순서 권장)

### Phase 1 — DB / DI 기반
- [ ] **Koin 모듈 설정** (`mineconomy-core`) — DI 컨테이너 초기화
- [ ] **HikariCP + Exposed 연결** — `DatabaseModule.kt`, `application.conf` 또는 `config.yml`
- [ ] **계좌 테이블** — `AccountTable`, `AccountRepository`
- [ ] **MineconomyApi 실구현체** — `MineconomyApiImpl` (DB 기반 잔액/이체)

### Phase 2 — 기본 경제 커맨드
- [ ] **Brigadier 커맨드 등록** — `/balance`, `/pay` (레거시 `CommandExecutor` 금지)
- [ ] **Vault 연결** — `onEnable()`에서 `VaultEconomyAdapter` 등록

### Phase 3 — AMM 거래소
- [ ] **CPMM 엔진** (`mineconomy.core.amm`) — `x * y = k` 공식, 유동성 풀
- [ ] **수수료 계산** — `amount * 25 / 1000` (버림, Long 전용)
- [ ] **거래소 커맨드** — `/exchange buy`, `/exchange sell`

### Phase 4 — 주식 시장
- [ ] **vAMM 주식 모델** (`mineconomy.core.stock`) — 가상 AMM 기반 가격 결정
- [ ] **기업 생성 / M&A** — 지분 시스템
- [ ] **공매도** — 증거금 계산, 청산 로직

### Phase 5 — 금융
- [ ] **은행 / 예금 / 대출** — 신용등급별 이자율
- [ ] **GUI 구현** (`mineconomy-gui`) — 체스트 UI + 커스텀 모델 데이터 차트

### Phase 6 — 파생상품 (v2)
- [ ] **선물 / 옵션** — 만기, 정산 로직
- [ ] **리소스팩 배포** — `resource-pack/` submodule 최신화

---

## 알려진 기술 부채

| 항목 | 내용 |
|------|------|
| `relocate()` 비활성화 | `com.gradleup.shadow` + Kotlin 2.1+ `@Metadata(int[])` 버그. shadow 플러그인 픽스 릴리즈 후 재활성화 필요 |
| `MineconomyApiImpl` 미구현 | `VaultEconomyAdapter`가 인터페이스만 참조하고 실제 DB 연동 없음 |

---

## 기술 스택 요약

| 항목 | 버전 / 내용 |
|------|------------|
| Paper API | 26.1.2.build.65-stable (JVM 25) |
| Kotlin | 2.2.0 (JVM target 24) |
| Exposed | 0.61.0 |
| HikariCP | 6.2.1 |
| MySQL connector | 9.3.0 |
| Koin | 4.1.0 |
| Shadow plugin | 8.3.5 |
| 통화 단위 | 원(₩), Long 전용 |
| DB 스레드 | `Dispatchers.IO` 코루틴 |
| Bukkit API | main thread 전용 |
