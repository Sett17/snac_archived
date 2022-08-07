package Database

import (
	"github.com/jackc/pgx/v4"
)

type SnippetOverview struct {
	Id    string `json:"id"`
	Title string `json:"title"`
}

func rowToSnippetOverview(row pgx.Rows) (snippetOverview SnippetOverview) {
	err := row.Scan(&snippetOverview.Id, &snippetOverview.Title)
	if err != nil {
		panic(err)
	}
	return
}
