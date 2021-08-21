import { isLocal } from './index';

const prefix = isLocal ? '' : '/api';

async function request(url: string, options: RequestInit = {}): Promise<Response> {
  const resp = await fetch(`${prefix}${url}`, {
    ...options,
    headers: {
      ...(options.headers || {}),
      Authorization: `Basic ${localStorage.auth}`,
    },
  });
  if (url !== '/login' && resp.status === 401) {
    localStorage.removeItem('auth');
    window.location.reload();
  }
  return resp;
}

const encodeBody = (body: Record<string, unknown>) => ({
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(body),
});

const api = {
  get: (url: string, options: RequestInit = {}): Promise<Response> => request(url, { ...options, method: 'GET' }),
  delete: (url: string, options: RequestInit = {}): Promise<Response> => request(url, { ...options, method: 'DELETE' }),
  post: (url: string, body: Record<string, unknown>, options: RequestInit = {}): Promise<Response> =>
    request(url, { ...options, method: 'POST', ...encodeBody(body) }),
  put: (url: string, body: Record<string, unknown>, options: RequestInit = {}): Promise<Response> =>
    request(url, { ...options, method: 'PUT', ...encodeBody(body) }),
};

export async function getConfig() {
  const resp = await api.get('/config');
  return (await resp.json()).config;
}

export async function putConfig(config: string) {
  const resp = await api.put('/config', { config });
  return resp.status;
}

export async function postTest(
  buildToolName: string,
  repoName: string,
  localCache: boolean,
  remoteCache: boolean,
  push: boolean
) {
  const resp = await api.post('/test', { buildToolName, repoName, localCache, remoteCache, push });
  return resp.status;
}

export async function deletePod(name: string) {
  const resp = await api.delete(`/pod/${name}`);
  return resp.status;
}

export async function deleteRepo(name: string) {
  const resp = await api.delete(`/repo/${name}`);
  return resp.status;
}

export async function login(username: string, password: string) {
  const resp = await api.post('/login', { username, password });
  return resp.status;
}
