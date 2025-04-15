package eventDemo.configuration.injection

import eventDemo.libs.event.VersionBuilder
import eventDemo.libs.event.VersionBuilderLocal
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind

fun Module.configureDILibs() {
  singleOf(::VersionBuilderLocal) bind VersionBuilder::class
}
