package com.github.gchudnov.doom.probes

import zio.{ Fiber, UIO }

sealed trait ProbeId {
  def cancel(): UIO[Unit]
}

final class LiveProbeId(fiber: Fiber.Runtime[Nothing, Long]) extends ProbeId {
  def cancel(): UIO[Unit] =
    fiber.interruptFork
}

object ProbeId {
  def apply(fiber: Fiber.Runtime[Nothing, Long]) = new LiveProbeId(fiber)
}
