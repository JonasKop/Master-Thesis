import useWebSocket from '../lib/websocket';
import { useCallback, useState } from 'react';
import AceEditor from 'react-ace';
import { Button } from '@chakra-ui/react';

import { useParams, useHistory } from 'react-router-dom';
import styled from 'styled-components';
import { deletePod } from '../lib/api';
import { deleteRepo } from '../lib/api';
import { BASE_WS_URL } from '../lib';

interface Params {
  name: string;
}

const Container = styled.div`
  width: 100%;
  height: 100%;
  display: grid;
  grid-template-rows: 1fr min-content;
  justify-items: center;
  grid-gap: 10px;
`;

function Pod() {
  const [logs, setLogs] = useState('');
  const { name } = useParams<Params>();
  const history = useHistory<Params>();

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
    <Container>
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
      <Button onClick={onClick}>Delete</Button>
    </Container>
  );
}

export default Pod;
