[![Build Status](https://github.com/malliina/play-hot-reload/workflows/Test/badge.svg)](https://github.com/malliina/play-hot-reload/actions)

# play-hot

Hot reloading of Play Framework web apps.

Minimizes the time you have to wait for the app to reload and refresh after a code change, providing a smoother
developer experience.

## Usage

1. Enable `PlayHotReloadPlugin` instead of `PlayScala` in build.sbt:

        val app = project
          .in(file("."))
          .enablePlugins(PlayHotReloadPlugin)

1. Load [socket.js](src/main/resources/socket.js) from the HTML of your web page.

1. When developing with `sbt ~run`, changes to source code will reload the web page after recompilation.

This plugin also opens the browser at http://localhost:9000 when the app initially starts in dev mode. Toggle this
setting with key `openBrowserOnStart`:

    openBrowserOnStart := true

## Example app

Folder [play-app](play-app) contains an example Play app with hot reloading enabled.

## Prior art

Li Haoyi's [workbench](https://github.com/lihaoyi/workbench):

- Is specific to Scala.js
- Uses libraries I'm not interested in at this time
