package Database

import (
	"database/sql"
	"github.com/lib/pq"
	"time"
)

type Snippet struct {
	Id          string         `json:"id"`
	Title       string         `json:"title"`
	Description string         `json:"description"`
	Content     string         `json:"content"`
	Tags        pq.StringArray `json:"tags"`
	Timestamp   time.Time      `json:"timestamp"`
}

type snippetDb struct {
	Id          string
	Title       string
	Description sql.NullString
	Content     sql.NullString
	Tags        pq.StringArray
	Timestamp   time.Time
}

func rowToSnippet(row *sql.Rows) (snippet Snippet) {
	var snippetDb snippetDb
	err := row.Scan(&snippetDb.Id, &snippetDb.Title, &snippetDb.Content, &snippetDb.Timestamp, &snippetDb.Tags, &snippetDb.Description)
	if err != nil {
		panic(err)
	}
	snippet.Id = snippetDb.Id
	snippet.Title = snippetDb.Title
	snippet.Content = snippetDb.Content.String
	snippet.Timestamp = snippetDb.Timestamp
	snippet.Tags = snippetDb.Tags
	snippet.Description = snippetDb.Description.String
	return
}
