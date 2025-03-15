package eventDemo.configuration

import eventDemo.app.command.action.ICantPlay
import eventDemo.app.command.action.IWantToJoinTheGame
import eventDemo.app.command.action.IWantToPlayCard
import eventDemo.app.command.action.IamReadyToPlay
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
