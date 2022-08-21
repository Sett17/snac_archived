package main

import (
	"embed"
	"github.com/labstack/echo/v4"
	"github.com/labstack/echo/v4/middleware"
	"net/http"
	"snac/Cache"
	"snac/Config"
	"snac/Database"
	"strconv"
)

//go:embed static/*
var static embed.FS

func main() {
	Config.LoadConfig()
	Database.Connect()
	defer Database.Close()
	Cache.Init()

	e := echo.New()
	e.HideBanner = true
	e.Logger.Output()

	e.Use(middleware.LoggerWithConfig(middleware.LoggerConfig{
		Format: "[${time_rfc3339}] ${remote_ip}\t${method} ${status} ${latency_human}\t${host}${path}\n",
	}))
	e.Use(middleware.Gzip())

	e.Use(CacheHeaders())

	var contentHandler = echo.WrapHandler(http.FileServer(http.FS(static)))
	var contentRewrite = middleware.Rewrite(map[string]string{"/*": "/static/$1"})
	e.GET("/*", contentHandler, contentRewrite)

	e.GET("/login", contentHandler, middleware.Rewrite(map[string]string{"/*": "/static/login.html"}))
	e.POST("/login", login)
	e.GET("/authorized", isAuthorized)

	Api(e)

	e.Logger.Fatal(e.Start(":" + strconv.Itoa(Config.Cfg.Snac.Port)))
}
