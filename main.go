package main

import (
	"github.com/gin-contrib/gzip"
	"github.com/gin-gonic/gin"
	"snac/Config"
	"snac/Database"
	"strconv"
)

func main() {
	Config.LoadConfig()
	Database.Connect()
	defer Database.Close()

	r := gin.Default()
	//r.Use(CacheHeaders())
	r.Use(gzip.Gzip(gzip.DefaultCompression)) // seems to be the best for my use case (quick test in python)

	r.Static("/assets", "./static")
	r.StaticFile("/", "./static/index.html")
	r.StaticFile("/login", "./static/login.html")
	r.POST("/login", login)
	r.GET("/authorized", isAuthorized)
	Api(r)
	Render(r)

	r.Run("0.0.0.0:" + strconv.Itoa(Config.Cfg.Snac.Port))
}
