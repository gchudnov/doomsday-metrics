package com.github.gchudnov.doom.util

import java.io.File

import scala.io.Source
import scala.util.Using
import scala.util.control.Exception.allCatch

object Files {

  def string(file: File): Either[Throwable, String] =
    allCatch.either {
      Using.resource(Source.fromFile(file)) { file =>
        file.getLines().mkString("\n").trim()
      }
    }

}
