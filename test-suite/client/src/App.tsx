import WithSubnavigation from './components/nav';
import Router from './components/router';
import styled from 'styled-components';
import { Spinner, Button } from '@chakra-ui/react';
import { BrowserRouter } from 'react-router-dom';
import { appContext, useAppContext } from './lib/queue';
import useWebSocket from './lib/websocket';

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

function App() {
  const [ctx, loading] = useAppContext();
  const { setState } = ctx;
  const { error } = useWebSocket('ws://localhost:8080/api/state', setState);

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

export default App;
