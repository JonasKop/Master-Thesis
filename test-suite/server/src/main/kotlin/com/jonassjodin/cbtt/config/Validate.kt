package com.jonassjodin.cbtt.config

class InvalidConfigException(message: String) : Exception(message)


fun validateConfig(conf: Config) {
    validateWorkdir(conf.workdir)
    conf.repositories.forEach(::validateRepository)
    conf.buildTools.forEach(::validateBuildTool)
}

fun validateWorkdir(workdir: String) {
    if (workdir.isEmpty()) throw InvalidConfigException("workdir must be set")
}

fun validateRepository(repo: Repository) {
    if (repo.name.isEmpty()) throw InvalidConfigException("repositories[].name must be set")
    if (repo.url.isEmpty()) throw InvalidConfigException("repositories[].url must be set")
    if (repo.dir.isEmpty()) throw InvalidConfigException("repositories[].dir must be set")
}

fun validateBuildTool(tool: BuildTool) {
    if (tool.name.isEmpty()) throw InvalidConfigException("buildTools[].name must be set")
    if (tool.image.isEmpty()) throw InvalidConfigException("buildTools[].image must be set")
    if (tool.command.isEmpty() || tool.command.contains("")) throw InvalidConfigException("buildTools[].command.[] must be set")
}

