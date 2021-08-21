export interface PushStatus {
  push: string;
  noPush: string;
}

export interface Command {
  setup: string;
  noCache?: PushStatus;
  localCache?: PushStatus;
  remoteCache?: PushStatus;
}

export interface SecurityContext {
  seccomp?: string;
  apparmor?: string;
  privileged?: boolean;
  userID?: number;
}

export interface BuildTool {
  name: string;
  image: string;
  tag: string;
  command: Command;
  securityContext?: SecurityContext;
  env?: Record<string, string>;
  localCacheDir?: string;
}

export interface Repository {
  name: string;
  tags: string[];
  url: string;
  dir: string;
}

export interface Job {
  repo: Repository;
  buildTool: BuildTool;
  localCache: boolean;
  remoteCache: boolean;
  push: boolean;
}

export interface Config {
  workdir: string;
  repositories: Repository[];
  buildTools: BuildTool[];
}
