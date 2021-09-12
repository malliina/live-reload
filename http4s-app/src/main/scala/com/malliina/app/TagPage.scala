package com.malliina.app

import scalatags.Text

case class TagPage(tags: Text.TypedTag[String]):
  override def toString = tags.toString()

object TagPage:
  val DocTypeTag = "<!DOCTYPE html>"
