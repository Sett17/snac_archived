package main

import (
	"github.com/labstack/echo/v4"
	"snac/Cache"
	"snac/Database"
)

func Api(e *echo.Echo) {
	api := e.Group("/api")
	api.Use(Auther())

	api.GET("/snippet/:id", getSnippet)
	api.POST("/snippet/:id", updateSnippet)
	api.DELETE("/snippet/:id", deleteSnippet)
	api.POST("/new", newSnippet)
	api.GET("/tag/:tag", getTag)
	api.GET("/tags", allTags)
	api.GET("/search", search)
}

func allTags(c echo.Context) error {
	//c.JSON(200, Database.GetTags())
	c.JSON(200, Cache.Tags())
	return nil
}

func getSnippet(c echo.Context) error {
	id := c.Param("id")
	//c.JSON(200, Database.GetSnippet(id))
	c.JSON(200, Cache.GetSnippet(id))
	return nil
}

func updateSnippet(c echo.Context) error {
	var snippet Database.Snippet
	c.Bind(&snippet)
	//c.JSON(200, Database.UpdateSnippet(snippet))
	c.JSON(200, Cache.SetSnippet(snippet))
	return nil
}

func deleteSnippet(c echo.Context) error {
	id := c.Param("id")
	//Database.DeleteSnippet(id)
	go Cache.DeleteSnippet(id)
	c.String(200, "Snippet deleted")
	return nil
}

func newSnippet(c echo.Context) error {
	var snippet Database.Snippet
	c.Bind(&snippet)
	//c.JSON(200, Database.NewSnippet(snippet))
	c.JSON(200, Cache.SetSnippet(snippet))
	return nil
}

func getTag(c echo.Context) error {
	tag := c.Param("tag")
	//c.JSON(200, Database.GetTag(tag))
	c.JSON(200, Cache.GetTag(tag))
	return nil
}

func search(c echo.Context) error {
	query := c.QueryParam("q")
	c.JSON(200, Database.Search(query))
	return nil
}
