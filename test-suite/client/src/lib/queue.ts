import {createContext, useEffect, useState} from 'react';
import {Config, Job} from './types';
import {useBoolean} from '@chakra-ui/react';
import api from './api';
import yaml from 'js-yaml';

export interface Pod {
  name: string;
  status: string;
}

export interface State {
  pods: Pod[];
  queue: Job[];
}

export interface AppContext {
  state: State | null;
  setState: (repos: State) => void;

  configFile: string;
  setConfigFile: (str: string) => void;

  config: Config;
  setConfig: (str: Config) => void;
}

export const appContext = createContext<AppContext>({} as AppContext);

export function useAppContext(): [AppContext, boolean] {
  const [state, setState] = useState<State | null>(null);
  const [configFile, setConfigFile] = useState<string>('');
  const [config, setConfig] = useState<Config>({} as Config);
  const [isLoading, {off}] = useBoolean(true);

  useEffect(() => {
    (async () => {
      const conf = await api.getConfig();
      setConfigFile(conf);
      setConfig(yaml.load(conf) as Config);
      off();
    })();
  }, [off]);

  return [
    {
      configFile,
      setConfigFile,
      config,
      setConfig,
      state,
      setState,
    },
    isLoading,
  ];
}
