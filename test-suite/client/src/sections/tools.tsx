import {
  Popover,
  PopoverTrigger,
  PopoverContent,
  PopoverHeader,
  PopoverBody,
  PopoverCloseButton,
  Table,
  Button,
  Thead,
  Tbody,
  Tr,
  Th,
  Td,
  Heading,
} from '@chakra-ui/react';
import { Fragment } from 'react';
import { BuildTool } from '../lib/types';
import { useContext } from 'react';
import { appContext } from '../lib/queue';
import Area from '../components/area';
import { AiOutlineInfoCircle } from 'react-icons/ai';
import { colors } from '../lib/style';

const explanations = {
  seccomp: 'If case of a compromised container, unconfined access to seccomp increases the chances of a container breakout.',
  apparmor: 'If case of a compromised container, unconfined access to apparmor increases the chances of a container breakout.',
  privileged: 'If a privileged container is compromised, a container breakout is very trivial to perform.',
  root: 'Because a root user in a container is the same as the root user on the host, a container breakout from a container running as root will give the attacker root access on the host.',
  remoteCache: 'The build tool supports remote caching in a container registry.',
  localCache: 'The build tool supports local caching in a directory.',
  push: 'The build tool supports pushing images to a remote container registry.',
};

interface TElementProps {
  fn: (e: BuildTool) => boolean;
}

function TElement({ fn }: TElementProps) {
  const { config } = useContext(appContext);
  const { buildTools } = config;

  return (
    <>
      {buildTools.map((e) => {
        const ok = fn(e);
        return (
          <Td key={e.name} color={ok ? colors.teal : "red"} fontWeight={600}>
            {ok ? 'yes' : 'no'}
          </Td>
        );
      })}
    </>
  );
}

interface InfoProps {
  title: string;
  description: string;
}

function Info({ title, description }: InfoProps) {
  return (
    <Popover placement="bottom">
      <PopoverTrigger>
        <Button leftIcon={<AiOutlineInfoCircle />} color="black" colorScheme={'transparent'}></Button>
      </PopoverTrigger>
      <PopoverContent color="white" bg="blue.800" borderColor="blue.800">
        <PopoverHeader pt={4} fontWeight="bold" border="0">
          {title}
        </PopoverHeader>
        <PopoverCloseButton />
        <PopoverBody>{description}</PopoverBody>
      </PopoverContent>
    </Popover>
  );
}

interface RowProps {
  boolFn: (e: BuildTool) => boolean;
  title: string;
  description: string;
}

function Row({ title, description, boolFn }: RowProps) {
  return (
    <Tr>
      <Td>
        <Info title={title} description={description} />
        {title}
      </Td>
      <TElement fn={boolFn} />
    </Tr>
  );
}

function Tools() {
  const { config } = useContext(appContext);
  const { buildTools } = config;
  return (
    <Area justifySelf="center" alignSelf="flex-start" display="grid">
      <Heading fontSize="xl">OCI Container Image Build-tool comparison</Heading>
      <Table>
        <Thead>
          <Tr>
            <Th>Setting</Th>
            {buildTools.map((e) => (
              <Fragment key={e.name}>
                <Th>{e.name}</Th>
              </Fragment>
            ))}
          </Tr>
        </Thead>
        <Tbody>
          <Row
            title="Unconfined Seccomp"
            description={explanations.seccomp}
            boolFn={(e) => !!e.securityContext?.seccomp}
          />
          <Row
            title="Unconfined Apparmor"
            description={explanations.apparmor}
            boolFn={(e) => !!e.securityContext?.apparmor}
          />
          <Row
            title="Privileged"
            description={explanations.privileged}
            boolFn={(e) => !!e.securityContext?.privileged}
          />
          <Row
            title="Root"
            description={explanations.root}
            boolFn={(e) => !e.securityContext?.userID || e.securityContext?.userID === 0}
          />
          <Row
            title="Remote Cache"
            description={explanations.remoteCache}
            boolFn={(e) => !!e.command.remoteCache?.noPush || !!e.command.remoteCache?.push}
          />
          <Row
            title="Local Cache"
            description={explanations.localCache}
            boolFn={(e) => !!e.command.localCache?.noPush || !!e.command.localCache?.push}
          />
          <Row
            title="Push"
            description={explanations.push}
            boolFn={(e) => !!e.command.remoteCache?.push || !!e.command.localCache?.push || !!e.command.noCache?.push}
          />
        </Tbody>
      </Table>
    </Area>
  );
}

export default Tools;
