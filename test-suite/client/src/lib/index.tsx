export function capitalize(str: string) {
  return str && str.charAt(0).toUpperCase() + str.slice(1);
}

export const isLocal = window.location.host.startsWith('localhost');

export const BASE_URL = isLocal ? 'localhost:8080' : window.location.host;
export const BASE_HTTP_URL = isLocal ? 'http://localhost:8080' : `https://${window.location.host}/api`;
export const BASE_WS_URL = isLocal ? 'ws://localhost:8080' : `wss://${window.location.host}/api`;

