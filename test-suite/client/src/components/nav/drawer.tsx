import {
  Drawer,
  DrawerBody,
  DrawerHeader,
  DrawerOverlay,
  Text,
  DrawerContent,
  Button,
  useDisclosure,
  DrawerCloseButton,
  Box,
} from '@chakra-ui/react';
import { useContext, useRef } from 'react';
import { appContext } from '../../lib/queue';
import { NavLink } from 'react-router-dom';
import { Job } from '../../lib/types';
import { RiLogoutBoxRLine } from 'react-icons/ri';

function getCache(job: Job) {
  if (job.localCache) return 'localcache';
  if (job.remoteCache) return 'remotecache';
  return 'nocache';
}

function DrawerExample() {
  const { state } = useContext(appContext);
  const { isOpen, onOpen, onClose } = useDisclosure();
  const btnRef = useRef() as any;

  const tests = state?.pods.filter((e) => e.name.startsWith('cbtt-test-'));

  const current = tests?.filter((e) => ['Running', 'Terminating', 'ContainerCreating'].includes(e.status));

  function logout() {
    localStorage.removeItem('auth');
    window.location.reload();
  }

  return (
    <>
      <Button leftIcon={<RiLogoutBoxRLine />} variant="outline" colorScheme="black" ref={btnRef} onClick={logout}>
        Logout
      </Button>
      <Button ref={btnRef} variant="outline" colorScheme="black" onClick={onOpen}>
        {(state?.queue.length || 0) + (current && current.length > 0 ? 1 : 0)}
      </Button>
      <Drawer isOpen={isOpen} placement="right" onClose={onClose} finalFocusRef={btnRef} size="sm">
        <DrawerOverlay>
          <DrawerContent>
            <DrawerCloseButton />
            <DrawerHeader>Tests</DrawerHeader>

            <DrawerBody display="grid" gridAutoRows="1fr">
              <Box>
                <Text fontSize="2xl">Active</Text>
                {current && current.length > 0 && <NavLink to={`/pod/${current[0].name}`}>{current[0].name}</NavLink>}
              </Box>
              <Box>
                <Text fontSize="2xl">Test Queue</Text>
                {state?.queue?.map((e) => {
                  const cache = getCache(e);
                  const push = e.push ? 'push' : 'nopush';
                  const val = `${e.buildTool.name} ${cache} ${push}`;
                  return <p key={val}>{val}</p>;
                })}
              </Box>
            </DrawerBody>

            {/*<DrawerFooter>*/}
            {/*  <Button variant="outline" mr={3} onClick={onClose}>*/}
            {/*    Cancel*/}
            {/*  </Button>*/}
            {/*  <Button colorScheme="blue">Save</Button>*/}
            {/*</DrawerFooter>*/}
          </DrawerContent>
        </DrawerOverlay>
      </Drawer>
    </>
  );
}

export default DrawerExample;
