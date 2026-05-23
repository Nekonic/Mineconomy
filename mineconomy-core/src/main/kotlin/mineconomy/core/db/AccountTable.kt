package mineconomy.core.db

import org.jetbrains.exposed.sql.Table

object AccountTable : Table("accounts") {
    val uuid    = varchar("uuid", 36)
    val balance = long("balance").default(0L)
    override val primaryKey = PrimaryKey(uuid)
}