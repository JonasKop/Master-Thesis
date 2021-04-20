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
  TableCaption,
} from '@chakra-ui/react';
import { Fragment } from 'react';
import styled from 'styled-components';
import { BuildTool } from '../lib/types';
import { useContext } from 'react';
import { appContext } from '../lib/queue';

const explanations = {
  seccomp: 'Unconfined access to seccomp is bad.',
  apparmor: 'Unconfined access to apparmor is bad.',
  privileged: 'Requiring full privileges is very bad.',
  root: 'Running as root is a security issue.',
  cache: 'The build-tool supports caching.',
  push: 'The build-tool supports pushing images.',
};

interface TElementProps {
  fn: (e: BuildTool) => boolean;
}

function TElement({ fn }: TElementProps) {
  const { config } = useContext(appContext);
  const { buildTools } = config;

  return (
    <>
      {buildTools.map((e) => (
        <Td key={e.name}>{fn(e) ? 'yes' : 'no'}</Td>
      ))}
    </>
  );
  // <Tr>
  //   <Td>{capitalize(title)}</Td>
  //   <Td>{value ? 'yes' : 'no'}</Td>
  //   <Td>{explanations[title][(!!value).toString()]}</Td>
  // </Tr>
}

const Container = styled.div`
  display: grid;
  grid-template-columns: 1fr;
  div {
    padding: 20px;
  }
`;

interface InfoProps {
  title: string;
  description: string;
}

function Info({ title, description }: InfoProps) {
  return (
    <Popover placement="bottom">
      <PopoverTrigger>
        <Button>Info</Button>
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
  fn: (e: BuildTool) => boolean;
  title: string;
  description: string;
}

function Row({ title, description, fn }: RowProps) {
  return (
    <Tr>
      <Td>
        <Info title={title} description={description} />
        {title}
      </Td>
      <TElement fn={fn} />
    </Tr>
  );
}

function Tools() {
  const { config } = useContext(appContext);
  const { buildTools } = config;
  return (
    <Container>
      <Table>
        <TableCaption>OCI Container Image Build-tool comparison</TableCaption>
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
            title="Seccomp"
            description={explanations.seccomp}
            fn={(e) => !!e.securityContext?.seccomp}
          />
          <Row
            title="Apparmor"
            description={explanations.apparmor}
            fn={(e) => !!e.securityContext?.apparmor}
          />
          <Row
            title="Privileged"
            description={explanations.privileged}
            fn={(e) => !!e.securityContext?.privileged}
          />
          <Row
            title="Root"
            description={explanations.root}
            fn={(e) => !e.securityContext?.userID || e.securityContext?.userID === 0}
          />
          <Row
            title="Cache"
            description={explanations.cache}
            fn={(e) => !!e.command.cache?.noPush || !!e.command.cache?.push}
          />
          <Row
            title="Push"
            description={explanations.push}
            fn={(e) => !!e.command.cache?.push || !!e.command.noCache?.push}
          />
        </Tbody>
      </Table>
    </Container>
  );
}

export default Tools;
