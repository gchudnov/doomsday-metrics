package com.github.gchudnov.doom

import java.io.File

case class DoomConfig(
  probeFile: File = new File("."),
  isDebug: Boolean = false
)
