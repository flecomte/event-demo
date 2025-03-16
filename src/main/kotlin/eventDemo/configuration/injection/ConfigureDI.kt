package eventDemo.configuration.injection

import org.koin.dsl.module

val appKoinModule =
  module {
    configureDIBusiness()
    configureDIInfrastructure()
    configureDILibs()
    configureDICommandActions()
  }
