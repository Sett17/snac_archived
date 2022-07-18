fun Int.toByteArray(): ByteArray {
  val b = ByteArray(4)
  for (i in 0..3) b[i] = (this shr i * 8).toByte()
  return b
}

fun ByteArray.toInt(): Int {
  require(size == 4)
  return (this[3].toInt() shl 24) or
          (this[2].toInt() and 0xff shl 16) or
          (this[1].toInt() and 0xff shl 8) or
          (this[0].toInt() and 0xff)
}
