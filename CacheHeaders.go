package main

import (
	"github.com/labstack/echo/v4"
	"strings"
)

func CacheHeaders() echo.MiddlewareFunc {
	return func(next echo.HandlerFunc) echo.HandlerFunc {
		return func(c echo.Context) error {
			switch url := c.Request().RequestURI; true {
			case strings.Contains(url, "api") || strings.Contains(url, "authorized") || strings.Contains(url, "login"):
				c.Response().Header().Set("Cache-Control", "max-age=2")
			case strings.Contains(url, "ttf"):
				c.Response().Header().Set("Cache-Control", "max-age=30000000")
			default:
				c.Response().Header().Set("Cache-Control", "max-age=604800")
			}
			return next(c)
		}
	}
}
