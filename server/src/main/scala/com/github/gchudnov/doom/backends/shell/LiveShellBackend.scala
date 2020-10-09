package com.github.gchudnov.doom.backends.shell

import zio.Task

import scala.collection.mutable.ListBuffer
import scala.sys.process.{ ProcessLogger, _ }

final class LiveShellBackend() extends ShellBackend.Service {

  override def exec(cmd: String): Task[ShellResult] =
    Task {
      val (procLogger, err, out) = buildProcessLogger()
      val n                      = cmd ! procLogger

      def toString(bs: ListBuffer[String]): String = bs.mkString("\n")

      ShellResult(code = n, err = toString(err), out = toString(out))
    }

  private def buildProcessLogger(): (ProcessLogger, ListBuffer[String], ListBuffer[String]) = {
    val os = ListBuffer.empty[String]
    val es = ListBuffer.empty[String]
    val procLogger = ProcessLogger(
      str => os.addOne(str),
      err => es.addOne(err)
    )
    (procLogger, es, os)
  }

}
