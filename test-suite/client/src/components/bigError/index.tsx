import { Button, Text } from '@chakra-ui/react';
import Area from '../area';

function BigError() {
  const reloadWindow = () => window.location.reload();

  return (
    <Area>
      <Text fontSize='3xl'>An error has occured</Text>
      <Button colorScheme='red' onClick={reloadWindow}>Try again!</Button>
    </Area>
  );
}

export default BigError;
