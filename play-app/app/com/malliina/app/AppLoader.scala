package com.malliina.app

import controllers.{AppHtml, AssetsComponents, Home}
import play.api.ApplicationLoader.Context
import play.api.{BuiltInComponentsFromContext, Mode}
import play.api.routing.Router
import play.filters.HttpFiltersComponents
import router.Routes

class AppLoader extends DefaultApp(new AppComponents(_))

class AppComponents(context: Context)
    extends BuiltInComponentsFromContext(context)
    with HttpFiltersComponents
    with AssetsComponents {
  val html = AppHtml(isHotReloaded = environment.mode == Mode.Dev)
  val home = new Home(html, controllerComponents, assets)
  override val router: Router = new Routes(httpErrorHandler, home)
}
