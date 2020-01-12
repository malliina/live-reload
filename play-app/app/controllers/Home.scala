package controllers

import controllers.Assets.Asset
import play.api.mvc._

class Home(comps: ControllerComponents, assets: AssetsBuilder) extends AbstractController(comps) {

  def index = Action(Ok(AppTags.index("Moi! :)))")))

  def versioned(path: String, file: Asset): Action[AnyContent] =
    assets.versioned(path, file)
}
