package main

import (
	"GoSnac/Database"
	"fmt"
	"github.com/gin-gonic/gin"
)

func Api(r *gin.Engine) {
	api := r.Group("/api")
	api.Use(Auther())

	api.GET("/snippet/:id", getSnippet)
	api.POST("/snippet/:id", updateSnippet)
	api.DELETE("/snippet/:id", deleteSnippet)
	api.POST("/new", newSnippet)
	api.GET("/tag/:tag", getTag)
	api.GET("/tags", allTags)
	api.GET("/search", search)
}

func allTags(c *gin.Context) {
	c.JSON(200, Database.GetTags())
}

func getSnippet(c *gin.Context) {
	id := c.Param("id")
	c.JSON(200, Database.GetSnippet(id))
}

func updateSnippet(c *gin.Context) {
	var snippet Database.Snippet
	c.BindJSON(&snippet)
	c.JSON(200, Database.UpdateSnippet(snippet))
}

func deleteSnippet(c *gin.Context) {
	id := c.Param("id")
	Database.DeleteSnippet(id)
	c.String(200, "Snippet deleted")
}

func newSnippet(c *gin.Context) {
	var snippet Database.Snippet
	c.BindJSON(&snippet)
	c.JSON(200, Database.NewSnippet(snippet))
}

func getTag(c *gin.Context) {
	tag := c.Param("tag")
	c.JSON(200, Database.GetTag(tag))
}

func search(c *gin.Context) {
	query := c.Query("q")
	fmt.Println(query)
	c.JSON(200, Database.Search(query))
}
