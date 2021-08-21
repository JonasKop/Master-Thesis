import fs from 'fs';
import yaml from 'js-yaml';
import { Config } from './types';
import checkEntry from './validate';
import fillConfigReferences from './parse';
import logger from '../utilities';
import requiredEntries from './requiredEntries.json';

// Fills all references in the config and checks if all required entries are set.
function loadConfig(config: Config) {
  const filledConfig = fillConfigReferences(config);

  const correctConfig = requiredEntries.reduce((status, entry) => {
    const entryIsSet = checkEntry(entry, config);
    if (!entryIsSet) logger.error(`Config value must be set: '${entry}'`);
    return status && entryIsSet;
  }, true);

  if (!correctConfig) {
    logger.error('Some variables are missing in the configuration');
    process.exit(1);
  }

  return filledConfig;
}

// Reads a config file and tries to load its correct entries
function readConfig(): Config {
  const configFile = process.env.CONFIG_FILE || 'config.yaml';
  const data = fs.readFileSync(configFile, 'utf-8');
  const config = <Config>yaml.load(data);
  return loadConfig(config);
}

export default readConfig;
