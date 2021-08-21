import { Config, ConfigPrimitive, ConfigTypes, SubConfig } from './types';
/* eslint-disable @typescript-eslint/no-use-before-define */

// Matches an entry with its type and traverses it with the correct function
function matchType(entry: ConfigTypes, xs: string[]) {
  if (Array.isArray(entry) && xs[0] === '[]') return checkIfSetArray(xs, entry);
  if (typeof entry === 'object') return traverseMap(xs, <SubConfig>entry);
  return checkIfSetPrimitive(entry);
}

// Checks if a primitive is set
function checkIfSetPrimitive(m: ConfigPrimitive): boolean {
  return m !== undefined && m !== null;
}

// Recursively traverses an array to check if an entry is set
function checkIfSetArray([, ...xs]: string[], li: ConfigTypes[]): boolean {
  return li.reduce<boolean>((status, entry) => status && matchType(entry, xs), true);
}

// Recursively traverses a map to check if an entry is set
function traverseMap([x, ...xs]: string[], m: SubConfig): boolean {
  if (m === undefined || m === null) return false;
  const entry = m[x];
  return matchType(entry, xs);
}

// Checks if an entry is set in the config file
export default function checkEntry(path: string, config: Config): boolean {
  const splitted = path.split('.');
  return traverseMap(splitted, config);
}
