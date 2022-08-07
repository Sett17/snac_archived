package Database

import (
	"github.com/jackc/pgtype"
	"github.com/jackc/pgx/v4"
	"time"
)

type Snippet struct {
	Id          string    `json:"id"`
	Title       string    `json:"title"`
	Description string    `json:"description"`
	Content     string    `json:"content"`
	Tags        []string  `json:"tags"`
	Timestamp   time.Time `json:"timestamp"`
}

type snippetDb struct {
	Id          string
	Title       string
	Description pgtype.Text
	Content     pgtype.Text
	Tags        pgtype.TextArray
	Timestamp   time.Time
}

func rowToSnippet(row pgx.Rows) (snippet Snippet) {
	var snippetDb snippetDb
	err := row.Scan(&snippetDb.Id, &snippetDb.Title, &snippetDb.Content, &snippetDb.Timestamp, &snippetDb.Tags, &snippetDb.Description)
	if err != nil {
		panic(err)
	}
	snippet.Id = snippetDb.Id
	snippet.Title = snippetDb.Title
	snippet.Content = snippetDb.Content.String
	snippet.Timestamp = snippetDb.Timestamp
	tags := make([]string, snippetDb.Tags.Dimensions[0].Length)
	for i := range tags {
		tags[i] = snippetDb.Tags.Elements[i].String
	}
	snippet.Tags = tags
	snippet.Description = snippetDb.Description.String
	return
}
