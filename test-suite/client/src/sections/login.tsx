import { Button, Heading, Input, useToast, Text, Box, useBoolean } from '@chakra-ui/react';
import { KeyboardEvent, useEffect, useRef, useState } from 'react';
import { login } from '../lib/api';
import { colors, gradients } from '../lib/style';
import Area from '../components/area';

function Login() {
  const toast = useToast();
  const inputRef = useRef<HTMLInputElement>(null);
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, { off, on }] = useBoolean();

  async function handleClick() {
    on();
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
    off();
  }

  useEffect(() => {
    inputRef.current?.focus();
  }, []);

  function handleKeyDown(e: KeyboardEvent) {
    if (e.key === 'Enter') handleClick();
  }

  return (
    <Area>
      <Heading>Login</Heading>
      <Box width="400px">
        <Text mb="8px">Username</Text>
        <Input
          ref={inputRef}
          onKeyDown={handleKeyDown}
          placeholder="Type your username"
          type="text"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
        />
      </Box>
      <Box>
        <Text mb="8px">Password</Text>
        <Input
          onKeyDown={handleKeyDown}
          placeholder="Type your password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
      </Box>
      <Button colorScheme="cbtt" isLoading={isLoading} bgGradient={gradients.default} color={colors.accent} onClick={handleClick}>
        Login
      </Button>
    </Area>
  );
}

export default Login;
