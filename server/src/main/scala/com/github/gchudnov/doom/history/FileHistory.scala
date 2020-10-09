package com.github.gchudnov.doom.history
import com.github.gchudnov.doom.probes.ProbeResponse
import zio.Task

/**
 * A History Service that stores prove response on the filesystem.
 */
final class FileHistory() extends History.Service {
  override def store(pr: ProbeResponse): Task[Unit] = ???
}
