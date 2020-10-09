package com.github.gchudnov.doom.probes.reader

class ProbeReaderException(message: String) extends RuntimeException(message)

final class ProbeReaderAttributeException(attrName: String, m: Map[String, Object]) extends ProbeReaderException(s"Cannot get '${attrName}' from ${m}")
