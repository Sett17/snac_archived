package Cache

import (
	"context"
	"fmt"
	"github.com/jellydator/ttlcache/v3"
	"snac/Config"
	"snac/Database"
)

type Key struct {
	Type  string
	Extra string
}

func Tags() []Database.TagOverview {
	return C.Get(Key{"tags", ""}).Value().([]Database.TagOverview)
}

func GetSnippet(id string) Database.Snippet {
	return C.Get(Key{"snippet", id}).Value().(Database.Snippet)
}

func SetSnippet(snippet Database.Snippet) Database.Snippet {
	C.Set(Key{"snippet", snippet.Id}, snippet, Config.Cfg.Cache.Expiration)
	go func() {
		C.Delete(Key{"tags", ""})
		for _, tag := range snippet.Tags {
			C.Delete(Key{"tag", tag})
		}
	}()
	return GetSnippet(snippet.Id)
}

func GetTag(tag string) []Database.SnippetOverview {
	return C.Get(Key{"tag", tag}).Value().([]Database.SnippetOverview)
}

func DeleteSnippet(id string) {
	Database.DeleteSnippet(id)
	go func() {
		C.Delete(Key{"snippet", id})
		C.Delete(Key{"tags", ""})
	}()
}

var C *ttlcache.Cache[Key, interface{}]

func Init() {
	loader := ttlcache.LoaderFunc[Key, interface{}](
		func(c *ttlcache.Cache[Key, interface{}], key Key) (item *ttlcache.Item[Key, interface{}]) {

			var val interface{}
			switch key.Type {
			case "tags":
				val = Database.GetTags()
			case "snippet":
				val = Database.GetSnippet(key.Extra)
			case "tag":
				val = Database.GetTag(key.Extra)
			}
			item = c.Set(key, val, Config.Cfg.Cache.Expiration)
			return
		},
	)

	C = ttlcache.New[Key, interface{}](
		ttlcache.WithLoader[Key, interface{}](loader),
	)

	C.OnInsertion(func(c context.Context, item *ttlcache.Item[Key, interface{}]) {
		if item.Key().Type == "snippet:" {
			snippet := item.Value().(Database.Snippet)
			if snippet.Id == "_____" {
				Database.NewSnippet(snippet)
			} else {
				Database.UpdateSnippet(snippet)
			}
		}
	})

	go C.Start()
	fmt.Println("Cache started with ttl:", Config.Cfg.Cache.Expiration)
	return
}
