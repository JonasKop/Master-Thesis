import { createContext } from 'react';
import { Config } from './types';

export interface ConfigContext {
  configFile: string;
  config: Config;
  setConfig: (str: string) => void;
}

export const configContext = createContext<ConfigContext>({
  configFile: '',
  config: {} as Config,
  setConfig: (_) => {},
});
