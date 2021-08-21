import { useCallback, useState } from 'react';
import AceEditor from 'react-ace';
import { Button, Heading } from '@chakra-ui/react';
import { useParams, useHistory } from 'react-router-dom';
import { BASE_WS_URL } from '../lib';
import { deletePod } from '../lib/api';
import { deleteRepo } from '../lib/api';
import useWebSocket from '../lib/websocket';
import Area from '../components/area';

interface Params {
  name: string;
}

function Pod() {
  const [logs, setLogs] = useState('');
  const { name } = useParams<Params>();
  const history = useHistory();

  const fn = useCallback((e) => setLogs((e1) => e1 + e), []);
  useWebSocket(`${BASE_WS_URL}/logs/${name}`, fn, false);

  async function onClick() {
    if (name.startsWith('cbtt-repo')) {
      await deleteRepo(name);
    } else {
      await deletePod(name);
    }

    history.push('/');
  }

  return (
    <Area
      display="grid"
      width="100%"
      height="100%"
      gridTemplateRows="min-content 1fr min-content"
      justifyItems="center"
      gridGap="10px"
    >
      <Heading fontSize="xl" justifySelf="flex-start">
        {name}
      </Heading>
      <AceEditor
        placeholder="Placeholder Text"
        theme="monokai"
        height="100%"
        readOnly
        wrapEnabled={true}
        width="100%"
        fontSize={14}
        showPrintMargin={true}
        showGutter={true}
        highlightActiveLine={true}
        value={logs}
        setOptions={{
          autoScrollEditorIntoView: true,
          enableBasicAutocompletion: false,
          enableLiveAutocompletion: false,
          enableSnippets: false,
          showLineNumbers: true,
          tabSize: 2,
        }}
      />
      <Button colorScheme="red" onClick={onClick}>
        Delete
      </Button>
    </Area>
  );
}

export default Pod;
