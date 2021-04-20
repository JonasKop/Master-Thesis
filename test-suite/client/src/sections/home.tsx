import { appContext, Pod } from '../lib/queue';
import { useContext } from 'react';
import { Spinner, Table, Thead, Tbody, Tr, Th, Td, TableCaption } from '@chakra-ui/react';
import { NavLink } from 'react-router-dom';

interface RepoProps {
  repo: Pod;
}

function Repo({ repo }: RepoProps) {
  return (
    <Tr>
      <Td>
        <NavLink to={`/pod/${repo.name}`}>{repo.name}</NavLink>
      </Td>
      <Td>{repo.status}</Td>
    </Tr>
  );
}

function Repos() {
  const { state } = useContext(appContext);

  if (state?.pods == null) return <Spinner size="xl" />;

  const repos = state.pods.filter((e) => e.name.startsWith('cbtt-repo-'));
  const testPods = state.pods.filter((e) => e.name.startsWith('cbtt-test-'));
  return (
    <>
      <Table>
        <TableCaption>Repositories</TableCaption>
        <Thead>
          <Tr>
            <Th>Name</Th>
            <Th>Status</Th>
          </Tr>
        </Thead>
        <Tbody>
          {repos.map((e) => (
            <Repo key={e.name} repo={e} />
          ))}
        </Tbody>
      </Table>
      <Table>
        <TableCaption>Completed Tests</TableCaption>
        <Thead>
          <Tr>
            <Th>Name</Th>
            <Th>Status</Th>
          </Tr>
        </Thead>
        <Tbody>
          {testPods.map((e) => (
            <Repo key={e.name} repo={e} />
          ))}
        </Tbody>
      </Table>
    </>
  );
}

function Home() {
  return (
    <div>
      <Repos />
    </div>
  );
}

// List all repos
// List all previous tests
export default Home;
