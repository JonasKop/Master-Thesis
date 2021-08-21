package config

type SecurityContext struct {
	SecurityAnnotations map[string]string `yaml:"securityAnnotations"`
	Privileged          bool              `yaml:"privileged"`
	UserID              int               `yaml:"userID"`
}

type BuildTool struct {
	Name            string          `yaml:"name"`
	Image           string          `yaml:"image"`
	Command         []string        `yaml:"command"`
	SecurityContext SecurityContext `yaml:"securityContext"`
}

type Config struct {
	Workdir      string       `yaml:"workdir"`
	Repositories []Repository `yaml:"repositories"`
	BuildTools   []BuildTool  `yaml:"buildTools"`
}
