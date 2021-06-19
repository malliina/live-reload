package com.malliina.app

import cats.effect.IO
import org.http4s.dsl.Http4sDsl
import org.http4s.scalatags.ScalatagsInstances
import org.http4s.syntax

trait Implicits extends syntax.AllSyntaxBinCompat with Http4sDsl[IO] with ScalatagsInstances
