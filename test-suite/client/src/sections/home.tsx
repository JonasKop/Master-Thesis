import { appContext, Pod } from '../lib/queue';
import { useContext } from 'react';
import { Box, Spinner, Table, Thead, Tbody, Tr, Th, Td, Heading, Button } from '@chakra-ui/react';
import { NavLink } from 'react-router-dom';
import Area from '../components/area';
import { deletePod, deleteRepo } from '../lib/api';
import { colors } from '../lib/style';

interface RepoProps {
  repo: Pod;
}

function Repo({ repo }: RepoProps) {
  async function onClick() {
    if (repo.name.startsWith('cbtt-repo')) {
      await deleteRepo(repo.name);
    } else {
      await deletePod(repo.name);
    }
  }

  return (
    <Tr>
      <Td>
        <NavLink to={`/pod/${repo.name}`}>{repo.name}</NavLink>
      </Td>
      <Td>{repo.status}</Td>
      <Td>
        <Button colorScheme="red" onClick={onClick} height="25px" >
          X
        </Button>
      </Td>
    </Tr>
  );
}

interface PodTableProps {
  title: string;
  pods: Pod[];
}

function PodTable({ title, pods }: PodTableProps) {
  return (
    <Area>
      <Heading fontSize="xl">{title}</Heading>
      <Table variant="striped" colorScheme="blackAlpha">
        <Thead>
          <Tr>
            <Th>Name</Th>
            <Th>Status</Th>
            <Th>Delete</Th>
          </Tr>
        </Thead>
        <Tbody>
          {pods.map((e) => (
            <Repo key={e.name} repo={e} />
          ))}
        </Tbody>
      </Table>
    </Area>
  );
}

function Home() {
  const { state } = useContext(appContext);

  if (state?.pods == null) {
    return <Spinner size="xl" color={colors.accent} alignSelf="center" justifySelf="center" />;
  }

  const repos = state.pods.filter((e) => e.name.startsWith('cbtt-repo-'));
  const testPods = state.pods.filter((e) => e.name.startsWith('cbtt-test-'));
  return (
    <Box display="grid" gridGap="20px" width="100%">
      <PodTable title="Repositories" pods={repos} />
      <PodTable title="Completed Tests" pods={testPods} />
    </Box>
  );
}

// List all repos
// List all previous tests
export default Home;
