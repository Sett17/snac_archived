package de.okedikka.application

import Snippet
import kotlinx.datetime.Instant
import java.sql.ResultSet

fun Snippet.Companion.from(rs: ResultSet): Snippet {
  return Snippet(
    rs.getString("id"),
    rs.getString("title"),
    rs.getString("description") ?: "",
    rs.getString("content") ?: "",
    (rs.getArray("tags").array as Array<String>).toList(),
    Instant.fromEpochMilliseconds(rs.getTimestamp("timestamp").time)
  )
}