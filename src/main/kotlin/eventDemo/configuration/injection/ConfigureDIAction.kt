package eventDemo.configuration.injection

import eventDemo.domain.command.action.ICantPlay
import eventDemo.domain.command.action.IWantToJoinTheGame
import eventDemo.domain.command.action.IWantToPlayCard
import eventDemo.domain.command.action.IamReadyToPlay
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf

/**
 * Configure all actions
 */
fun Module.configureDICommandActions() {
  singleOf(::IWantToPlayCard)
  singleOf(::IamReadyToPlay)
  singleOf(::IWantToJoinTheGame)
  singleOf(::ICantPlay)
}
