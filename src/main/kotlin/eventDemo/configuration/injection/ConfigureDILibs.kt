package eventDemo.configuration.injection

import eventDemo.business.command.command.GameCommand
import eventDemo.libs.command.CommandRunnerController
import eventDemo.libs.command.CommandStreamChannel
import eventDemo.libs.event.VersionBuilder
import eventDemo.libs.event.VersionBuilderLocal
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind

fun Module.configureDILibs() {
  single {
    CommandStreamChannel<GameCommand>(CommandRunnerController())
  }
  singleOf(::VersionBuilderLocal) bind VersionBuilder::class
}
