package eventDemo.configuration

import eventDemo.business.command.action.ICantPlay
import eventDemo.business.command.action.IWantToJoinTheGame
import eventDemo.business.command.action.IWantToPlayCard
import eventDemo.business.command.action.IamReadyToPlay
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf

/**
 * Configure all actions
 */
fun Module.configureActions() {
  singleOf(::IWantToPlayCard)
  singleOf(::IamReadyToPlay)
  singleOf(::IWantToJoinTheGame)
  singleOf(::ICantPlay)
}
