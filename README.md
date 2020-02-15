[![Build Status](https://github.com/malliina/play-live-reload/workflows/Test/badge.svg)](https://github.com/malliina/play-live-reload/actions)

# play-live-reload

Reloads the browser after a code change in your Play Framework web app.

## Usage

1. Add to `project/plugins.sbt`:

        addSbtPlugin("com.malliina" % "play-live-reload" % "0.0.27")

1. Enable `PlayLiveReloadPlugin` instead of `PlayScala` in build.sbt:

        val app = project
          .in(file("."))
          .enablePlugins(PlayLiveReloadPlugin)

1. Inject the JavaScript URL found in value `com.malliina.live.LiveReload.script` to the HTML of your web page.

1. When developing with `sbt ~run`, changes to source code will reload the web page after recompilation.

This plugin also opens the browser at http://localhost:9000 when the app initially starts in dev mode. Toggle this
setting with key `openBrowserOnStart`:

    openBrowserOnStart := true

## Example app

Folder [play-app](play-app) contains an example Play app with live reloading enabled.

## Releasing a new version

To release a new version to Maven Central, run:

    sbt release
    
This will push a new tag to version control, which triggers this [GitHub Action](.github/workflows/release.yml) that 
pushes artifacts to Maven Central.

## Prior art

Li Haoyi's [workbench](https://github.com/lihaoyi/workbench):

- Is specific to Scala.js
- Uses libraries I'm not interested in at this time
