package mineconomy.core.di

import kotlinx.coroutines.CoroutineScope
import mineconomy.api.MineconomyApi
import mineconomy.core.amm.ExchangeService
import mineconomy.core.amm.PoolRepository
import mineconomy.core.amm.PriceHistoryRepository
import mineconomy.core.db.AccountRepository
import mineconomy.core.economy.MineconomyApiImpl
import org.koin.dsl.module

fun coreModule(scope: CoroutineScope) = module {
    single { AccountRepository() }
    single { PoolRepository() }
    single { PriceHistoryRepository() }
    single { MineconomyApiImpl(get(), scope) }
    single<MineconomyApi> { get<MineconomyApiImpl>() }
    single { ExchangeService(get(), get(), get()) }
}
