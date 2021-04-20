import AceEditor from 'react-ace';
import 'ace-builds/src-noconflict/mode-yaml';
import 'ace-builds/src-noconflict/theme-monokai';
import 'ace-builds/src-noconflict/ext-language_tools';
import styled from 'styled-components';
import { Config as ConfigType } from '../lib/types';
import { Button, useBoolean, useToast } from '@chakra-ui/react';
import { useContext, useEffect, useState } from 'react';
import yaml from 'js-yaml';
import api from '../lib/api';
import { appContext } from '../lib/queue';

const Container = styled.div`
  height: 100%;
  width: 100%;
  display: grid;
  grid-template-rows: 1fr min-content;
  grid-gap: 10px;
  justify-items: center;
`;

function Config() {
  const toast = useToast();
  const { config, configFile, setConfigFile, setConfig } = useContext(appContext);
  const [value, setValue] = useState(configFile);
  const [isLoading, { on, off }] = useBoolean(false);

  useEffect(() => {
    api.getConfig();
  }, []);
  async function handleClick() {
    on();
    const status = await api.putConfig(value);
    if (status >= 400) {
      off();
      toast({
        title: 'Invalid configuration',
        status: 'error',
        duration: 1000,
      });
    } else {
      setConfigFile(value);
      setConfig(yaml.load(value) as ConfigType);
      off();
      toast({
        title: 'Saved',
        status: 'success',
        duration: 1000,
      });
    }
  }

  return (
    <Container>
      <AceEditor
        placeholder="Placeholder Text"
        mode="yaml"
        theme="monokai"
        height="100%"
        wrapEnabled={true}
        width="100%"
        onChange={setValue}
        fontSize={14}
        showPrintMargin={true}
        showGutter={true}
        highlightActiveLine={true}
        value={value}
        setOptions={{
          autoScrollEditorIntoView: true,
          enableBasicAutocompletion: false,
          enableLiveAutocompletion: false,
          enableSnippets: false,
          showLineNumbers: true,
          tabSize: 2,
        }}
      />
      <Button colorScheme="blue" isLoading={isLoading} onClick={handleClick}>
        Save Config
      </Button>
    </Container>
  );
}

export default Config;
