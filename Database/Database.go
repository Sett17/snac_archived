package Database

import (
	"GoSnac/Config"
	"database/sql"
	"fmt"
	"strings"
)

var db *sql.DB

func Connect() {
	connStr := "postgres://" +
		Config.Cfg.Database.User + ":" +
		Config.Cfg.Database.Password + "@" +
		Config.Cfg.Database.Host + "/" +
		Config.Cfg.Database.Db + "?sslmode=" + Config.Cfg.Database.SslMode
	ldb, err := sql.Open("postgres", connStr)
	if err != nil {
		panic(err)
	}
	db = ldb
	fmt.Println("Connected to database")
}

func GetAll() (snippets []Snippet) {
	rows, err := db.Query("SELECT * FROM snippets ORDER BY title ASC")
	if err != nil {
		panic(err)
	}
	defer rows.Close()
	for rows.Next() {
		snippets = append(snippets, rowToSnippet(rows))
	}
	return
}

func GetTags() (tags []TagOverview) {
	rows, err := db.Query("SELECT unnest(tags) as name, count(*) FROM snippets GROUP BY name ORDER BY name")
	if err != nil {
		panic(err)
	}
	defer rows.Close()
	for rows.Next() {
		tags = append(tags, rowToTagOverview(rows))
	}
	return
}

func GetTag(tag string) (snippets []SnippetOverview) {
	rows, err := db.Query("SELECT id, title FROM snippets WHERE $1 = any(tags) ORDER BY title", tag)
	if err != nil {
		panic(err)
	}
	defer rows.Close()
	for rows.Next() {
		snippets = append(snippets, rowToSnippetOverview(rows))
	}
	return
}

func GetSnippet(id string) (snippet Snippet) {
	rows, err := db.Query("SELECT * FROM snippets WHERE id = $1", id)
	if err != nil {
		panic(err)
	}
	defer rows.Close()
	for rows.Next() {
		snippet = rowToSnippet(rows)
	}
	return
}

func UpdateSnippet(snippet Snippet) (resSnippet Snippet) {
	rows, err := db.Query("UPDATE snippets SET title = $1, description = $2, content = $3, tags = $4 WHERE id = $5 RETURNING *",
		snippet.Title, snippet.Description, snippet.Content, snippet.Tags, snippet.Id)
	if err != nil {
		panic(err)
	}
	defer rows.Close()
	for rows.Next() {
		resSnippet = rowToSnippet(rows)
	}
	return
}

func NewSnippet(snippet Snippet) (resSnippet Snippet) {
	rows, err := db.Query("INSERT INTO snippets (title, description, content, tags) VALUES ($1, $2, $3, $4) RETURNING *",
		snippet.Title, snippet.Description, snippet.Content, snippet.Tags)
	if err != nil {
		panic(err)
	}
	defer rows.Close()
	for rows.Next() {
		resSnippet = rowToSnippet(rows)
	}
	return
}

func DeleteSnippet(id string) {
	_, err := db.Exec("DELETE FROM snippets WHERE id = $1", id)
	if err != nil {
		panic(err)
	}
}

func Search(query string) (snippets []SnippetOverview) {
	rows, err := db.Query("SELECT id, title FROM snippets WHERE title ~* $1 OR description ~* $2 OR $3 = any(tags) ORDER BY title",
		query, query, strings.ToUpper(query))
	if err != nil {
		panic(err)
	}
	defer rows.Close()
	for rows.Next() {
		snippets = append(snippets, rowToSnippetOverview(rows))
	}
	return
}
