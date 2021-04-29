import WithSubnavigation from './components/nav';
import Router from './components/router';
import styled from 'styled-components';
import { Spinner, Button, Input, toast, useToast } from '@chakra-ui/react';
import { BrowserRouter } from 'react-router-dom';
import { appContext, useAppContext } from './lib/queue';
import useWebSocket from './lib/websocket';
import { KeyboardEvent, KeyboardEventHandler, useState } from 'react';
import { login } from './lib/api';
import { BASE_WS_URL } from './lib';

const Container = styled.div`
  height: calc(100vh - 60px);
  width: 100vw;
  padding: 20px;
`;

const SpinnerContainer = styled.div`
  height: 100vh;
  width: 100vw;
  display: grid;
  justify-items: center;
  align-items: center;
`;

const auth = localStorage.auth;

function Wrapper() {
  const toast = useToast();

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');

  async function handleClick() {
    const status = await login(username, password);
    if (status === 401) {
      toast({
        title: 'Invalid credentials',
        status: 'error',
        duration: 1000,
      });
    } else if (status !== 200) {
      toast({
        title: 'An unknown error has occured',
        status: 'error',
        duration: 1000,
      });
    } else {
      localStorage.auth = btoa(`${username}:${password}`);
      window.location.reload();
    }
  }

  function handleKeyDown(e: KeyboardEvent) {
    if (e.key === 'Enter') handleClick();
  }

  if (!auth)
    return (
      <div>
        <p>Login</p>
        <Input
          onKeyDown={handleKeyDown}
          placeholder="username"
          type="text"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
        />
        <Input
          onKeyDown={handleKeyDown}
          placeholder="password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
        <Button onClick={handleClick}>Login</Button>
      </div>
    );
  return <App />;
}

function App() {
  const [ctx, loading] = useAppContext();
  const { setState } = ctx;
  const { error } = useWebSocket(`${BASE_WS_URL}/state`, setState);

  if (error) {
    return (
      <SpinnerContainer>
        <h1>An error has occured</h1>
        <Button onClick={() => window.location.reload()}>Try again!</Button>
      </SpinnerContainer>
    );
  }

  if (loading) {
    return (
      <SpinnerContainer>
        <Spinner size="xl" />
      </SpinnerContainer>
    );
  }

  return (
    <appContext.Provider value={ctx}>
      <BrowserRouter>
        <WithSubnavigation />
        <Container>
          <Router />
        </Container>
      </BrowserRouter>
    </appContext.Provider>
  );
}

export default Wrapper;
