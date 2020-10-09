package com.github.gchudnov.doom.backends.shell

/**
 * The result of command execution
 * @param code return code
 * @param err the contents of stderr
 * @param out the contents of stdout
 */
final case class ShellResult(code: Int, err: String, out: String)

object ShellResult {
  def toError(sr: ShellResult): Throwable =
    new ShellException(s"shell function returned non-zero code: ${sr.code}; stdout: ${sr.out}; stderr: ${sr.err}")
}
