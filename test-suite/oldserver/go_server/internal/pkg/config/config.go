package config

import (
	"github.com/pkg/errors"
	"gopkg.in/yaml.v2"
	"io/ioutil"
	"os"
)

func Load() (*Config, error) {
	configFile := os.Getenv("CONFIG_FILE")
	if len(configFile) == 0 {
		configFile = "config.yaml"
	}

	data, err := ioutil.ReadFile(configFile)
	if err != nil {
		return nil, errors.Wrap(err, "No config file available")
	}

	var conf Config
	err = yaml.Unmarshal(data, &conf)
	if err != nil {
		return nil, errors.Wrap(err, "Unmarshalling config file")
	}

	if err := validateConfig(conf); err != nil {
		return nil, errors.Wrap(err, "validating config")
	}
	return &conf, nil
}

func validateConfig(conf Config) error {
	if conf.Workdir == "" {
		return errors.New("workdir must be set")
	}
	for i, repo := range conf.Repositories {
		if err := validateRepository(repo); err != nil {
			return errors.Wrapf(err, "repository nr %d is invalid", i)
		}
	}

	for i, tool := range conf.BuildTools {
		if err := validateBuildTool(tool); err != nil {
			return errors.Wrapf(err, "build tool nr %d is invalid", i)
		}
	}
	return nil
}

func validateRepository(repo Repository) error {
	if repo.Name == "" {
		return errors.New("name must be set")
	}
	if repo.URL == "" {
		return errors.New("url must be set")
	}
	if repo.Dir == "" {
		return errors.New("dir must be set")
	}
	return nil
}

func validateBuildTool(tool BuildTool) error {
	if tool.Name == "" {
		return errors.New("name must be set")
	}
	if tool.Image == "" {
		return errors.New("image must be set")
	}
	if len(tool.Command) == 0 {
		return errors.New("command must be set")
	}
	return nil
}
