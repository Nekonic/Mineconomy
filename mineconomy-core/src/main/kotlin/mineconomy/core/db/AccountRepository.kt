package mineconomy.core.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert
import java.util.UUID

class AccountRepository {

    suspend fun getBalance(uuid: UUID): Long = withContext(Dispatchers.IO) {
        transaction {
            AccountTable.selectAll()
                .where { AccountTable.uuid eq uuid.toString() }
                .singleOrNull()
                ?.get(AccountTable.balance)
                ?: 0L
        }
    }

    suspend fun setBalance(uuid: UUID, balance: Long): Unit = withContext(Dispatchers.IO) {
        transaction {
            AccountTable.upsert {
                it[AccountTable.uuid] = uuid.toString()
                it[AccountTable.balance] = balance
            }
        }
    }
}