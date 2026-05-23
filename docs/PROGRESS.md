# Mineconomy 개발 진행 현황

> 마지막 업데이트: 2026-05-23 / 최신 태그: v0.2.0

---

## 완료된 작업

### 인프라 / 빌드
- [x] Gradle Kotlin DSL 멀티모듈 프로젝트 세팅 (`settings.gradle.kts`, `libs.versions.toml`)
- [x] 모듈 3개 구성: `mineconomy-api` → `mineconomy-core` → `mineconomy-gui`
- [x] Paper 26.1.2 (JVM 25 요구) + Kotlin 2.2.0 (max JVM target 24) 호환 workaround
- [x] Shadow JAR 출력 경로 `dist/` 고정
- [x] `relocate()` 비활성화 (shadow 8.3.x + Kotlin 2.2.x `@Metadata(int[])` 버그 회피)
- [x] `.gitignore` — `dist/`, `build/`, `.gradle/`, `.idea/`, `*.class`

### GitHub Actions CI/CD
- [x] 태그(`v*`) push 시 자동 빌드 + GitHub Release 생성
- [x] `mineconomy-resource-pack` Git submodule 정상 등록
- [x] Release artifact: `Mineconomy-*.jar` + `Mineconomy-Resource-Pack.zip`

### Phase 1 — DB / DI 기반 ✅
- [x] Koin DI 모듈 (`mineconomy.core.di.CoreModule`)
- [x] HikariCP + Exposed + MariaDB/MySQL 연결 (`MineconomyGuiPlugin.initDatabase()`)
- [x] DB 자동 생성 (`CREATE DATABASE IF NOT EXISTS`)
- [x] `AccountTable` + `AccountRepository` (uuid, balance)
- [x] `MineconomyApiImpl` — 인메모리 캐시, deposit/withdraw/transfer
- [x] 플레이어 생명주기: `AsyncPlayerPreLoginEvent` (로드) / `PlayerQuitEvent` (언로드+저장)
- [x] 사망 패널티: 잔액 3% 소실 (`PlayerDeathEvent`)

### Phase 2 — 기본 경제 커맨드 ✅
- [x] Brigadier `/balance` — 본인 잔액 조회
- [x] Brigadier `/pay <플레이어> <금액>` — 이체 (수수료 2.5%)
- [x] Vault `Economy` 어댑터 등록 (`VaultHook`)
- [x] NPC 시스템 — `Interaction` + `TextDisplay` + `ItemDisplay` 조합 (Citizens 불필요)
- [x] `/meco npc place <type>` — NPC 배치 + config.yml 저장

### Phase 3 — AMM 거래소 ✅
- [x] **CPMM 엔진** (`mineconomy.core.amm.AmmEngine`)
  - `x·y = k` 수식, 수수료 2.5% (버림)
  - `calcBuy` / `calcSell` / `calcBuyCost` (역산) / `executeBuy` / `executeSell`
- [x] **유동성 풀 DB** (`PoolTable`, `PoolRepository`) — 아이템별 독립 풀
- [x] **풀 시딩** — 서버 최초 시작 시 14종 기본 풀 생성 (이미 있으면 스킵)
- [x] **거래소 GUI** (`ExchangeGui`) — 체스트 UI (Listener 패턴)
  - 리스트 화면 (54슬롯): 14종 아이템 + 현재 가격 표시
  - 거래 화면 (27슬롯): 수량 선택 (×1/8/16/32/64) + 구매/판매 버튼
  - 구매: 화폐 차감 → 아이템 지급 (남으면 발밑 드롭)
  - 판매: 인벤토리에서 아이템 회수 → 화폐 지급
  - 거래 후 현재 풀 가격 자동 갱신
- [x] **거래소 NPC 연결** — `NpcType.EXCHANGE` → `ExchangeGui.open(player)`

---

## 미구현 (구현 순서 권장)

### Phase 4 — 주식 시장
- [ ] **기업 생성** — 설립 비용 ₩500,000, IPO 공모 기간
- [ ] **vAMM 주식 모델** (`mineconomy.core.stock`) — 가상 유동성 풀 기반 주가
- [ ] **보통주 / 우선주** — 의결권, 배당 우선권
- [ ] **주주총회** — 배당/자사주매입/합병 안건 의결
- [ ] **상장폐지** — 시총 5% 미만 + 유예기간 1주
- [ ] **거래소 기업** — 수수료 수익 적립, 자동 상장
- [ ] **서킷브레이커** — ±30% 상하한가, ±20% 전체 서킷

### Phase 5 — 금융 / 은행 GUI
- [ ] **대출** — 일반/담보, 신용등급별 금리
- [ ] **신용등급** — A+~D, 분기 재산정
- [ ] **은행 GUI** — `NpcType.BANK` → 체스트 UI
- [ ] **주식 차트** — 커스텀 모델 데이터 방식

### Phase 6 — 파생상품 (v2)
- [ ] **선물 / 옵션** — 만기, 차액 정산
- [ ] **공매도** — 증거금, 강제 청산, 숏스퀴즈
- [ ] **M&A** — 지분 매집, 포이즌 필

---

## 알려진 기술 부채

| 항목 | 내용 |
|------|------|
| `relocate()` 비활성화 | `com.gradleup.shadow` + Kotlin 2.1+ `@Metadata(int[])` 버그. 픽스 릴리즈 후 재활성화 필요 |
| 거래소 수수료 미분배 | `feeAccumulated`에 적립 중. Phase 4 거래소 기업 구현 후 연동 필요 |
| LP 예치 미구현 | 현재 서버 소유 유동성만. Phase 4 이후 플레이어 LP 예치 허용 예정 |

---

## 기술 스택 요약

| 항목 | 버전 / 내용 |
|------|------------|
| Paper API | 26.1.2.build.65-stable (JVM 25) |
| Kotlin | 2.2.0 (JVM target 24) |
| Exposed | 0.61.0 |
| HikariCP | 6.2.1 |
| MariaDB connector | 3.5.3 |
| MySQL connector | 9.3.0 |
| Koin | 4.1.0 |
| Shadow plugin | 8.3.5 |
| 통화 단위 | 원(₩), Long 전용 |
| 수수료 | `amount * 25 / 1000` (버림) |
| DB 스레드 | `Dispatchers.IO` 코루틴 |
| Bukkit API | main thread 전용 |
