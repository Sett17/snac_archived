package Config

import (
	"fmt"
	"gopkg.in/yaml.v2" // in v3 I cant use strict from outside for some dumb reason
	"os"
	"time"
)

type Config struct {
	Database struct {
		Host     string `yaml:"host"`
		Db       string `yaml:"db"`
		User     string `yaml:"user"`
		Password string `yaml:"password"`
		SslMode  string `yaml:"sslmode"`
	} `yaml:"database"`
	Cache struct {
		//Enabled    bool          `yaml:"enabled"`
		Expiration time.Duration `yaml:"expiration"`
	} `yaml:"cache"`
	Snac struct {
		Port     int    `yaml:"port"`
		Secret   string `yaml:"secret"`
		Password string `yaml:"password"`
	} `yaml:"snac"`
}

var Cfg Config

func LoadConfig() {
	f, err := os.ReadFile("config.yaml")
	if err != nil {
		panic(err)
	}
	err = yaml.UnmarshalStrict(f, &Cfg)
	if err != nil {
		panic(err)
	}
	if Cfg.Database.Host == "" {
		panic("Database.Host is empty")
	}
	if Cfg.Database.Db == "" {
		panic("Database.Db is empty")
	}
	if Cfg.Database.User == "" {
		panic("Database.User is empty")
	}
	if Cfg.Database.Password == "" {
		panic("Database.Password is empty")
	}
	if Cfg.Database.SslMode == "" {
		panic("Database.SslMode is empty")
	}
	if Cfg.Cache.Expiration == 0 {
		panic("Cache.Expiration can't be 0")
	}
	if Cfg.Snac.Port == 0 {
		panic("Snac.Port is empty")
	}
	if Cfg.Snac.Secret == "" {
		panic("Snac.Secret is empty")
	}
	if Cfg.Snac.Password == "" {
		panic("Snac.Password is empty")
	}
	fmt.Println("Config successfully loaded")
}
