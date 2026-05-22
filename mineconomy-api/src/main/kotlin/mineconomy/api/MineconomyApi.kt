package mineconomy.api

/**
 * Mineconomy 외부 연동 API.
 * 외부 플러그인은 이 인터페이스를 통해 잔액 조회/이체 등을 수행한다.
 */
interface MineconomyApi {

    /** 플레이어 UUID 기준 잔액 조회 (단위: 원) */
    fun getBalance(uuid: java.util.UUID): Long

    /** [amount] 원을 [from] → [to] 로 이체. 잔액 부족 시 false 반환 */
    fun transfer(from: java.util.UUID, to: java.util.UUID, amount: Long): Boolean
}