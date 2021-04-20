export interface BuildTool {
  name: string;
  image: string;
  command: {
    setup: string;
    cache?: {
      push: string;
      noPush: string;
    };
    noCache?: {
      push: string;
      noPush: string;
    };
  };
  securityContext: {
    seccomp: string | null;
    apparmor: string | null;
    privileged: boolean | null;
    userID: number | null;
  } | null;
  env: Record<string, string> | null;
}

export interface Repository {
  name: string;
  url: string;
  dir: string;
  jib: boolean;
}

export interface Job {
  repo: Repository;
  buildTool: BuildTool;
  cache: boolean;
  push: boolean;
}

export interface Config {
  workdir: string;
  repositories: Repository[];
  buildTools: BuildTool[];
}
