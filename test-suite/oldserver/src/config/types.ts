export interface Config extends SubConfig {
  workdir: string;
  tests: {
    name: string;
    repo: string;
    dir: string;
    credentials?: string;
  }[];
  services: {
    name: string;
    image: string;
    cmd: string;
    args: string[];
  }[];
}

export type ConfigPrimitive = number | boolean | string | null;

export type ConfigTypes = ConfigPrimitive | SubConfig | ConfigTypes[];

export interface SubConfig {
  [key: string]: ConfigTypes | ConfigTypes[];
}
