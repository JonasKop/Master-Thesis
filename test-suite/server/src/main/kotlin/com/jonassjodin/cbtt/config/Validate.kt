package com.jonassjodin.cbtt.config

class InvalidConfigException(message: String) : Exception(message)

fun validateConfig(conf: Config) {
    validateWorkdir(conf.workdir)
    validateResources(conf.resources)
    conf.repositories.forEach(::validateRepository)
    conf.buildTools.forEach(::validateBuildTool)
}

fun validateResources(res: ResourceRequirements) {
    if (res.limits.memory.isEmpty()) throw InvalidConfigException("resources.limits.memory must be set")
    if (res.limits.cpu.isEmpty()) throw InvalidConfigException("resources.limits.cpu must be set")
    if (res.requests.memory.isEmpty()) throw InvalidConfigException("resources.limits.memory must be set")
    if (res.requests.cpu.isEmpty()) throw InvalidConfigException("resources.limits.cpu must be set")
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
    if (tool.command.setup.isEmpty()) throw InvalidConfigException("buildTools[].command.setup must be set")
    if (tool.command.remoteCache?.push == null &&
        tool.command.remoteCache?.noPush == null &&
        tool.command.localCache?.push == null &&
        tool.command.localCache?.noPush == null &&
        tool.command.noCache?.push == null &&
        tool.command.noCache?.noPush == null
    ) {
        throw InvalidConfigException("one of buildTools[].command.(cache.push, cache.noPush, noCache.push, noCache.noPush")
    }

    if ((tool.command.localCache?.noPush != null || tool.command.localCache?.push != null) && tool.localCacheDir == null) {
        throw InvalidConfigException("since local cached is used, buildTools[].localCacheDir must be set")
    }
}
