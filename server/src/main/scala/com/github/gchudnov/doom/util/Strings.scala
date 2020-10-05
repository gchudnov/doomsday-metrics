package com.github.gchudnov.doom.util

object Strings {

  /**
   * Unquotes the string.
   * NOTE: the spaces should be already trimmed.
   */
  def unquote(value: String): String =
    if ((value.indexOf("\"") == 0) && (value.lastIndexOf("\"") == value.length - 1))
      value.substring(1, value.length - 1)
    else
      value
}
