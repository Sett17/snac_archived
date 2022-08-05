package main

import (
	"github.com/gin-gonic/gin"
	"strings"
)

func CacheHeaders() gin.HandlerFunc {
	return func(c *gin.Context) {
		// cant properly get contenttype here before next, without doing ALOT of other stuff
		switch url := c.Request.URL.String(); true {
		case strings.Contains(url, "api") || strings.Contains(url, "authorized") || strings.Contains(url, "login"):
			c.Header("Cache-Control", "max-age=2")
		case strings.Contains(url, "ttf"):
			c.Header("Cache-Control", "max-age=30000000")
		default:
			c.Header("Cache-Control", "max-age=604800")
		}
		c.Next()
	}
}
