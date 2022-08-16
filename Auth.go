package main

import (
	"fmt"
	"github.com/golang-jwt/jwt/v4"
	"github.com/labstack/echo/v4"
	"net/http"
	"snac/Config"
)

func login(c echo.Context) error {
	password := c.FormValue("password")
	if password == "" {
		c.String(400, "Password is required")
		return nil
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
			c.SetCookie(&http.Cookie{
				Name:   "token",
				Value:  token,
				MaxAge: 60 * 60 * 24,
				Path:   "/",
			})
			c.String(200, "OK")
		}
	}
	return echo.ErrInternalServerError
}

func isAuthorized(c echo.Context) error {
	cookie, err := c.Cookie("token")
	if err != nil {
		c.String(200, "false")
		return nil
	}
	if checkJWT(cookie) {
		c.String(200, "true")
	} else {
		c.String(200, "false")
	}
	return nil
}

func checkJWT(cookie *http.Cookie) bool {
	token, err := jwt.Parse(cookie.Value, func(token *jwt.Token) (interface{}, error) {
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

func Auther() echo.MiddlewareFunc {
	return func(next echo.HandlerFunc) echo.HandlerFunc {
		return func(c echo.Context) error {
			cookie, err := c.Cookie("token")
			if err != nil {
				c.String(401, "Unauthorized")
				return echo.ErrUnauthorized
			}
			if checkJWT(cookie) {
				return next(c)
			} else {
				c.String(401, "Unauthorized")
				return echo.ErrUnauthorized
			}
		}
	}
}
