async function getConfig() {
  const resp = await fetch('/api/config');
  const config = (await resp.json()).config;
  return config;
}

async function putConfig(config: string) {
  const resp = await fetch('/api/config', {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ config }),
  });
  return resp.status;
}

async function postTest(buildToolName: string, repoName: string, cache: boolean, push: boolean) {
  const resp = await fetch('/api/test', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ buildToolName, repoName, cache, push }),
  });
  return resp.status;
}

async function deletePod(name: string) {
  const resp = await fetch(`/api/pod/${name}`, { method: 'DELETE' });
  return resp.status;
}

const api = {
  getConfig,
  putConfig,
  postTest,
  deletePod,
};

export default api;
