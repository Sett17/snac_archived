package Database

import "database/sql"

type SnippetOverview struct {
	Id    string `json:"id"`
	Title string `json:"title"`
}

func rowToSnippetOverview(row *sql.Rows) (snippetOverview SnippetOverview) {
	err := row.Scan(&snippetOverview.Id, &snippetOverview.Title)
	if err != nil {
		panic(err)
	}
	return
}
