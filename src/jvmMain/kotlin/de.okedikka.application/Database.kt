package de.okedikka.application

import Snippet
import SnippetOverview
import TagOverview
import io.ktor.util.*
import org.slf4j.LoggerFactory
import java.sql.DriverManager
import kotlin.system.measureTimeMillis

object DB {
  private val logger = LoggerFactory.getLogger("Database")
  private val conn = DriverManager.getConnection(config.database.url, config.database.user, config.database.password)

  init {
    logger.debug("Connected to ${config.database.url}")
  }

  val all: List<Snippet>
    get() {
      val res = mutableListOf<Snippet>()
      measureTimeMillis {
        conn.prepareStatement(
          """
          SELECT *
          FROM snippets
          ORDER BY title ASC
        """.trimIndent().also { logger.debug(it.replace("\n", " ")) }
        ).executeQuery().run {
          while (next()) {
            res.add(Snippet.from(this))
          }
        }
      }.also { logger.debug("Last query took ${it/1e3}s") }
      return res
    }
  val tags: List<TagOverview>
    get() {
      val res = mutableListOf<TagOverview>()
      measureTimeMillis {
        conn.prepareStatement(
          """
          SELECT unnest(tags) as name, count(*)
          FROM snippets
          GROUP BY name
          ORDER BY name
        """.trimIndent().also { logger.debug(it.replace("\n", " ")) }
        ).executeQuery().run {
          while (next()) {
            res.add(TagOverview(getString("name"), getInt("count")))
          }
        }
      }.also { logger.debug("Last query took ${it/1e3}s") }
      return res
    }

  fun getTag(tag: String): List<SnippetOverview> {
    val res = mutableListOf<SnippetOverview>()
    measureTimeMillis {
      conn.prepareStatement(
        """
        SELECT id, title
        FROM snippets
        WHERE ? = any(tags)
        ORDER BY title
      """.trimIndent().also { logger.debug(it.replace("\n", " ")) }
      ).apply {
        setString(1, tag.toUpperCasePreservingASCIIRules())
      }.executeQuery().run {
        while (next()) {
          res.add(SnippetOverview(getString("id"), getString("title")))
        }
      }
    }.also { logger.debug("Last query took ${it/1e3}s") }
    return res
  }

  fun getSnippet(id: String): Snippet? {
    measureTimeMillis {
      conn.prepareStatement(
        """
        SELECT *
        FROM snippets
        WHERE id = ?
      """.trimIndent().also { logger.debug(it.replace("\n", " ")) }
      ).apply {
        setString(1, id)
      }.executeQuery().run {
        if (next()) {
          return Snippet.from(this)
        }
      }
    }.also { logger.debug("Last query took ${it/1e3}s") }
    return null
  }

  fun updateSnippet(snippet: Snippet): Snippet? {
    measureTimeMillis {
      conn.prepareStatement(
        """
        UPDATE snippets
        SET title = ?, description = ?, content = ?, tags = ?
        WHERE id = ?
        RETURNING *
      """.trimIndent().also { logger.debug(it.replace("\n", " ")) }
      ).apply {
        setString(1, snippet.title)
        setString(2, snippet.description)
        setString(3, snippet.content)
        setArray(4, conn.createArrayOf("text", snippet.tags.toTypedArray()))
        setString(5, snippet.id)
      }.executeQuery().run {
        if (next()) {
          return Snippet.from(this)
        }
      }
    }.also { logger.debug("Last query took ${it/1e3}s") }
    return null
  }

  fun newSnippet(snippet: Snippet): Snippet? {
    measureTimeMillis {
      conn.prepareStatement(
        """
        INSERT INTO snippets (title, description, content, tags)
        VALUES (?, ?, ?, ?)
        RETURNING *
      """.trimIndent().also { logger.debug(it.replace("\n", " ")) }
      ).apply {
        setString(1, snippet.title)
        setString(2, snippet.description)
        setString(3, snippet.content)
        setArray(4, conn.createArrayOf("text", snippet.tags.toTypedArray()))
      }.executeQuery().run {
        if (next()) {
          return Snippet.from(this)
        }
      }
    }.also { logger.debug("Last query took ${it/1e3}s") }
    return null
  }

  fun deleteSnippet(id: String) {
    measureTimeMillis {
      conn.prepareStatement(
        """
        DELETE FROM snippets
        WHERE id = ?
      """.trimIndent().also { logger.debug(it.replace("\n", " ")) }
      ).apply {
        setString(1, id)
      }.executeUpdate()
    }.also { logger.debug("Last query took ${it/1e3}s") }
  }
}