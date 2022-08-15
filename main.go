package main

import (
	"embed"
	"github.com/gin-contrib/gzip"
	"github.com/gin-gonic/gin"
	"net/http"
	"snac/Config"
	"snac/Database"
	"strconv"
)

//go:embed static
var static embed.FS

func main() {
	Config.LoadConfig()
	Database.Connect()
	defer Database.Close()

	r := gin.Default()
	r.Use(CacheHeaders())
	r.Use(gzip.Gzip(gzip.DefaultCompression)) // seems to be the best for my use case (quick test in python)

	//r.Static("/static", "./static")
	r.GET("/static/*filepath", func(c *gin.Context) {
		staticServer := http.FileServer(http.FS(static))
		staticServer.ServeHTTP(c.Writer, c.Request)
	})
	r.StaticFileFS("/", "./static/", http.FS(static))
	r.StaticFileFS("/login", "./static/login.html", http.FS(static))
	r.POST("/login", login)
	r.GET("/authorized", isAuthorized)
	Api(r)

	r.Run("0.0.0.0:" + strconv.Itoa(Config.Cfg.Snac.Port))
}
