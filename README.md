[![Build Status](https://github.com/malliina/play-live-reload/workflows/Test/badge.svg)](https://github.com/malliina/play-live-reload/actions)

# play-live-reload

Live reloading of Play Framework web apps.

Minimizes the time you have to wait for the app to reload and refresh after a code change, providing a smoother
developer experience.

## Usage

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

To release a new version to Bintray, run:

    sbt release
    
This will push a new tag to version control, which triggers this [GitHub Action](.github/workflows/release.yml) that 
releases a new version.

## Prior art

Li Haoyi's [workbench](https://github.com/lihaoyi/workbench):

- Is specific to Scala.js
- Uses libraries I'm not interested in at this time
