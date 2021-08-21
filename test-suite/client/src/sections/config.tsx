import AceEditor from 'react-ace';
import 'ace-builds/src-noconflict/mode-yaml';
import 'ace-builds/src-noconflict/theme-monokai';
import 'ace-builds/src-noconflict/ext-language_tools';
import { Config as ConfigType } from '../lib/types';
import { Button, Heading, useBoolean, useToast } from '@chakra-ui/react';
import { useContext, useEffect, useState } from 'react';
import yaml from 'js-yaml';
import { appContext } from '../lib/queue';
import { getConfig, putConfig } from '../lib/api';
import Area from '../components/area';
import { colors } from '../lib/style';

function Config() {
  const toast = useToast();
  const { configFile, setConfigFile, setConfig } = useContext(appContext);
  const [value, setValue] = useState(configFile);
  const [isLoading, { on, off }] = useBoolean(false);

  useEffect(() => {
    getConfig();
  }, []);

  async function handleClick() {
    on();
    const status = await putConfig(value);
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
    <Area
      height="100%"
      width="100%"
      display="grid"
      gridTemplateRows="min-content 1fr min-content"
      gridGap="10px"
      justifyItems="center"
    >
      <Heading fontSize="xl" justifySelf="flex-start">
        Configuration
      </Heading>
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
      <Button colorScheme="cbtt" color={colors.accent} isLoading={isLoading} onClick={handleClick}>
        Save Config
      </Button>
    </Area>
  );
}

export default Config;
