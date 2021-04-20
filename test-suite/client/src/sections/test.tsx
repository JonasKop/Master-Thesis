import { Select, Button, useToast, useBoolean } from '@chakra-ui/react';
import { ChangeEvent, useCallback, useContext, useEffect, useState } from 'react';
import api from '../lib/api';
import styled from 'styled-components';
import { appContext } from '../lib/queue';

const Container = styled.div`
  height: 100%;
  width: 100%;
  display: grid;
  justify-items: center;
`;

type TestType =
  | 'cache and push'
  | 'no cache and push'
  | 'cache and no push'
  | 'no cache and no push';

const CACHE_AND_PUSH: TestType = 'cache and push';
const NO_CACHE_AND_PUSH: TestType = 'no cache and push';
const CACHE_AND_NO_PUSH: TestType = 'cache and no push';
const NO_CACHE_AND_NO_PUSH: TestType = 'no cache and no push';

function Test() {
  const { config } = useContext(appContext);
  const toast = useToast();
  const [buildTool, setBuildTool] = useState(config.buildTools[0].name);
  const [repo, setRepo] = useState(config.repositories[0].name);
  const [isLoading, { on, off }] = useBoolean(false);
  const genTests = useCallback(() => {
    const bt = config.buildTools.filter((e) => e.name === buildTool)[0];
    return [
      ...(typeof bt.command.cache?.push === 'string' ? [CACHE_AND_PUSH as TestType] : []),
      ...(typeof bt.command.cache?.noPush === 'string' ? [CACHE_AND_NO_PUSH] : []),
      ...(typeof bt.command.noCache?.push === 'string' ? [NO_CACHE_AND_PUSH] : []),
      ...(typeof bt.command.noCache?.noPush === 'string' ? [NO_CACHE_AND_NO_PUSH] : []),
    ].filter((e) => e);
  }, [buildTool, config.buildTools]);
  const [tests, setTests] = useState<TestType[]>(genTests());

  useEffect(() => {
    setTests(genTests());
  }, [genTests]);

  const [test, setTest] = useState(tests[0]);

  function changeTool(e: ChangeEvent<HTMLSelectElement>) {
    setBuildTool(e.target.value);
  }

  useEffect(() => {
    setTest(tests[0]);
  }, [tests]);

  async function onClick() {
    const isCache = test === CACHE_AND_PUSH || test === CACHE_AND_NO_PUSH;
    const isPush = test === CACHE_AND_PUSH || test === NO_CACHE_AND_PUSH;
    on();
    await api.postTest(buildTool, repo, isCache, isPush);
    toast({
      title: 'Added to queue',
      status: 'success',
      duration: 1000,
    });
    off();
  }

  return (
    <Container>
      <h1>Run a test job</h1>
      <Select value={buildTool} onChange={changeTool}>
        {config.buildTools.map((e) => (
          <option key={e.name} value={e.name}>
            {e.name}
          </option>
        ))}
      </Select>
      <Select value={repo} onChange={(e) => setRepo(e.target.value)}>
        {config.repositories.map((e) => (
          <option key={e.name} value={e.name}>
            {e.name}
          </option>
        ))}
      </Select>
      <Select value={test} onChange={(e) => setTest(e.target.value as TestType)}>
        {tests.map((e) => (
          <option key={e} value={e}>
            {e}
          </option>
        ))}
      </Select>
      <Button isLoading={isLoading} onClick={onClick}>
        Add to queue
      </Button>
    </Container>
  );
}

export default Test;
