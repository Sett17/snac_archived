package main

import (
	"GoSnac/Config"
	"fmt"
	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v4"
)

func login(c *gin.Context) {
	password := c.PostForm("password")
	if password == "" {
		c.String(400, "Password is required")
		return
	}
	if password != Config.Cfg.Snac.Password {
		c.String(401, "Password is incorrect")
	} else {
		token, err := jwt.NewWithClaims(jwt.SigningMethodHS256, jwt.MapClaims{
			"password": password,
		}).SignedString([]byte(Config.Cfg.Snac.Secret))
		if err != nil {
			c.String(500, "Error generating token")
		} else {
			c.SetCookie("token", token, 60*60*24, "/", "", false, false)
			c.String(200, "OK")
		}
	}
}

func isAuthorized(c *gin.Context) {
	cookie, err := c.Cookie("token")
	if err != nil {
		c.String(200, "false")
		return
	}
	if checkJWT(cookie) {
		c.String(200, "true")
	} else {
		c.String(200, "false")
	}
}

func checkJWT(cookie string) bool {
	token, err := jwt.Parse(cookie, func(token *jwt.Token) (interface{}, error) {
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, fmt.Errorf("unexpected signing method: %v", token.Header["alg"])
		}
		return []byte(Config.Cfg.Snac.Secret), nil
	})
	if err != nil {
		return false
	}

	if claims, ok := token.Claims.(jwt.MapClaims); ok && token.Valid {
		if claims["password"] == Config.Cfg.Snac.Password {
			return true
		}
		return false
	}
	return false
}

func Auther() gin.HandlerFunc {
	return func(c *gin.Context) {
		cookie, err := c.Cookie("token")
		if err != nil {
			c.String(401, "Unauthorized")
			c.Abort()
			return
		}
		if checkJWT(cookie) {
			c.Next()
		} else {
			c.String(401, "Unauthorized")
			c.Abort()
		}
	}
}
