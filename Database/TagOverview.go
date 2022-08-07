package Database

import (
	"github.com/jackc/pgx/v4"
)

type TagOverview struct {
	Name  string `json:"tag"`
	Count int    `json:"count"`
}

func rowToTagOverview(row pgx.Rows) (tagOverview TagOverview) {
	err := row.Scan(&tagOverview.Name, &tagOverview.Count)
	if err != nil {
		panic(err)
	}
	return
}
