import { Config, ConfigPrimitive, ConfigTypes, SubConfig } from './types';
import logger from '../utilities';
/* eslint-disable @typescript-eslint/no-use-before-define */

let hasBeenFound = false;
let maxDepth = 20;

function findParents(splittedPath: string[]) {
  const regMatchNegInt = /^-\d+$/;
  return splittedPath.reduce<string[]>((fullPath, entry) => {
    const isNegative = entry.match(regMatchNegInt);
    if (isNegative) {
      const amount = parseInt(entry.substr(1), 10);
      const negatives = new Array(amount).fill('-1');
      return [...fullPath, ...negatives];
    }
    return [...fullPath, entry];
  }, []);
}

function handleParents(prefix: string, splittedPath: string[]) {
  const splitted = prefix.split('.');
  const added = <string[]>[];
  splittedPath.reverse().forEach((path) => (path === '-1' ? splitted.pop() : added.unshift(path)));
  return [...splitted, ...added];
}

function handleSetValue(splittedPath: string[], template: string, m: string, original: Config) {
  return splittedPath.reduce<ConfigTypes>((s1: any, e1) => {
    const r = s1[e1];
    if (typeof r === 'string' && r.startsWith('{{') && r.endsWith('}}')) return template;

    if (e1.startsWith('[') && e1.endsWith(']')) {
      const ind = parseInt(e1.substr(1, e1.length - 2), 10);
      return s1[ind];
    }

    if (s1[e1] === undefined) {
      logger.error(`invalid template '${m}'`);
      process.exit(1);
    }
    return s1[e1];
  }, original);
}

function handleString(found: string[], prefix: string, m: string, original: Config) {
  return found.reduce((s, e) => {
    const key = e.substr(2, e.length - 4);
    let splittedPath = key.split('.');

    splittedPath = findParents(splittedPath);
    if (splittedPath.includes('-1')) {
      splittedPath = handleParents(prefix, splittedPath);
    }

    const value = handleSetValue(splittedPath, e, m, original);
    if (s === e) return value;
    return (<any>s).replaceAll(e, value);
  }, m);
}

function traversePrimitive(prefix: string, m: ConfigPrimitive, original: Config): any {
  if (typeof m !== 'string') return m;
  const regex = /{{(.*?)}}/g;
  const found = m.match(regex);
  if (!found) return m;

  hasBeenFound = true;
  return handleString(found, prefix, m, original);
}

function traverseArray(prefix: string, m: SubConfig[], original: Config): SubConfig[] {
  return m.map((e, i: number) => {
    const name = `${prefix}${prefix === '' ? '' : '.'}[${i}]`;
    if (Array.isArray(e)) return traverseArray(name, e, original);
    if (typeof e === 'object' && e) return traverseMap(name, e as SubConfig, original);
    return traversePrimitive(name, e, original);
  });
}

function traverseMap(prefix: string, m: SubConfig, original: Config): SubConfig | null {
  if (!m) return null;

  return Object.entries(m).reduce((s, [key, value]) => {
    const name = `${prefix}${prefix === '' ? '' : '.'}${key}`;
    if (Array.isArray(value)) return { ...s, [key]: traverseArray(name, value as any, original) };
    if (typeof value === 'object' && value) {
      return { ...s, [key]: traverseMap(name, value, original) };
    }
    return { ...s, [key]: traversePrimitive(name, value, original) };
  }, m);
}

export default function fillConfigReferences(config: Config): Config {
  const b = <Config>traverseMap('', config, config);
  if (hasBeenFound) {
    maxDepth -= 1;
    if (maxDepth === 0) throw Error('Too deeeep');
    hasBeenFound = false;
    return fillConfigReferences(b);
  }
  return b;
}
