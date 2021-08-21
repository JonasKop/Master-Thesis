import { Select, Button, useBoolean, RadioGroup, Stack, Radio, useToast, Heading } from '@chakra-ui/react';
import { useContext, useEffect, useState } from 'react';
import { appContext } from '../lib/queue';
import { BuildTool, Command } from '../lib/types';
import { postTest } from '../lib/api';
import { capitalize } from '../lib';
import Area from '../components/area';
import { colors } from '../lib/style';

function getFirstCache(cmd: Command): string {
  if (cmd.noCache) return 'no';
  if (cmd.localCache) return 'local';
  return 'remote';
}

interface CurrentTool {
  buildToolName: string;
  localCache: boolean;
  remoteCache: boolean;
  push: boolean;
}

interface BTTestProps {
  buildTool: BuildTool;
  setCurrentTool: (tool: CurrentTool) => void;
}

function BTTest({ buildTool, setCurrentTool }: BTTestProps) {
  const [cache, setCache] = useState('no');
  const [push, setPush] = useState('no');

  useEffect(() => {
    setCache(getFirstCache(buildTool.command));
  }, [buildTool.command]);

  useEffect(() => {
    setCurrentTool({
      buildToolName: buildTool.name,
      localCache: cache === 'local',
      remoteCache: cache === 'remote',
      push: push === 'yes',
    });
  }, [buildTool.name, cache, push, setCurrentTool]);

  const caches = [
    ...(buildTool.command.noCache ? ['no'] : []),
    ...(buildTool.command.localCache ? ['local'] : []),
    ...(buildTool.command.remoteCache ? ['remote'] : []),
  ].filter((e) => e);
  return (
    <>
      <Heading fontSize="s">Cache</Heading>
      <RadioGroup onChange={setCache} value={cache}>
        <Stack direction="row">
          {caches.map((e) => (
            <Radio key={e} value={e}>
              {capitalize(e)}
            </Radio>
          ))}
        </Stack>
      </RadioGroup>
      <Heading fontSize="s">Push</Heading>
      <RadioGroup onChange={setPush} value={push}>
        <Stack direction="row">
          <Radio value="no">No</Radio>
          <Radio value="yes">Yes</Radio>
        </Stack>
      </RadioGroup>
    </>
  );
}

function Test() {
  const { config } = useContext(appContext);
  const toast = useToast();
  const [buildTool, setBuildTool] = useState(config.buildTools[0].name);
  const [repo, setRepo] = useState(config.repositories[0].name);
  const currentRepo = config.repositories.find((e) => e.name === repo)!!;
  const currentBt = config.buildTools.find((e) => e.name === buildTool)!!;

  const [currentTool, setCurrentTool] = useState<CurrentTool>({
    buildToolName: currentBt.name,
    localCache: false,
    remoteCache: false,
    push: false,
  });

  const [isLoading, { on, off }] = useBoolean(false);

  async function onClick() {
    on();
    await postTest(currentTool.buildToolName, repo, currentTool.localCache, currentTool.remoteCache, currentTool.push);
    toast({
      title: 'Added to queue',
      status: 'success',
      duration: 1000,
    });
    off();
  }

  return (
    <Area justifySelf="center" alignSelf="flex-start" maxWidth="500px">
      <Heading fontSize="xl">Run a test job</Heading>
      <Select value={repo} onChange={(e) => setRepo(e.target.value)}>
        {config.repositories.map((e) => (
          <option key={e.name} value={e.name}>
            {e.name}
          </option>
        ))}
      </Select>
      <Select value={buildTool} onChange={(e) => setBuildTool(e.target.value)}>
        {config.buildTools
          .filter((e) => {
            if (!currentRepo.tags || !e.tag) return true;
            return currentRepo.tags.includes(e.tag);
          })
          .map((e) => (
            <option key={e.name} value={e.name}>
              {e.name}
            </option>
          ))}
      </Select>
      <BTTest buildTool={config.buildTools.find((e) => e.name === buildTool)!!} setCurrentTool={setCurrentTool} />
      <Button colorScheme="cbtt" color={colors.accent} isLoading={isLoading} onClick={onClick}>
        Add to queue
      </Button>
    </Area>
  );
}

export default Test;
