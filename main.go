package main

import (
	"GoSnac/Config"
	"GoSnac/Database"
	"github.com/gin-contrib/gzip"
	"github.com/gin-gonic/gin"
	"strconv"
)

func main() {
	Config.LoadConfig()
	Database.Connect()

	r := gin.Default()
	r.Use(CacheHeaders())
	r.Use(gzip.Gzip(gzip.DefaultCompression)) // seems to be the best for my use case (quick test in python)

	r.Static("/assets", "./static")
	r.StaticFile("/", "./static/index.html")
	r.StaticFile("/login", "./static/login.html")
	r.POST("/login", login)
	r.GET("/authorized", isAuthorized)
	Api(r)

	r.Run("0.0.0.0:" + strconv.Itoa(Config.Cfg.Snac.Port))
}
