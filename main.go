package main

import (
	"embed"
	"github.com/labstack/echo/v4"
	"github.com/labstack/echo/v4/middleware"
	"net/http"
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

	e := echo.New()

	e.Use(middleware.LoggerWithConfig(middleware.LoggerConfig{}))
	e.Use(middleware.Recover())
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
