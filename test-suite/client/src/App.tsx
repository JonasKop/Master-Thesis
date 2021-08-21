import WithSubnavigation from './components/nav';
import Router from './components/router';
import { Spinner, Box } from '@chakra-ui/react';
import { BrowserRouter } from 'react-router-dom';
import { appContext, useAppContext } from './lib/queue';
import useWebSocket from './lib/websocket';
import { BASE_WS_URL } from './lib';
import Login from './sections/login';
import BigError from './components/bigError';
import { colors } from './lib/style';

const auth = localStorage.auth;

function Wrapper() {
  return (
    <Box
      background="url('/assets/bg.svg')"
      backgroundSize="cover"
      height="100vh"
      display="grid"
      justifyContent="center"
      justifyItems="center"
      alignItems="center"
      alignContent="center"
    >
      {!auth && <Login />}
      {auth && <App />}
    </Box>
  );
}

function App() {
  const [ctx, loading] = useAppContext();
  const { setState } = ctx;
  const { error } = useWebSocket(`${BASE_WS_URL}/state`, setState);

  if (error) {
    return <BigError />;
  }

  if (loading) {
    return <Spinner size="xl" color={colors.accent} />;
  }

  return (
    <appContext.Provider value={ctx}>
      <BrowserRouter>
        <WithSubnavigation />
        <Box height="calc(100vh - 60px)" width="100vw" padding="20px;" display="grid">
          <Router />
        </Box>
      </BrowserRouter>
    </appContext.Provider>
  );
}

export default Wrapper;
