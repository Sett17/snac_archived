package Database

import "database/sql"

type TagOverview struct {
	Name  string `json:"tag"`
	Count int    `json:"count"`
}

func rowToTagOverview(row *sql.Rows) (tagOverview TagOverview) {
	err := row.Scan(&tagOverview.Name, &tagOverview.Count)
	if err != nil {
		panic(err)
	}
	return
}
