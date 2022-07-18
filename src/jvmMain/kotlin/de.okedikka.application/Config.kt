package de.okedikka.application

data class Database(val url: String, val user: String, val password: String)
data class Snac(val port: Int, val secret: String, val password: String)
data class Config(val database: Database, val snac: Snac)
