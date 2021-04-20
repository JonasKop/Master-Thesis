import Test from '../../sections/test';
import Config from '../../sections/config';
import Tools from '../../sections/tools';
import Home from '../../sections/home';
import React from 'react';
import Pod from "../../sections/pod";

export interface RouteItem {
  path: string;
  Component: React.FC;
}

const routes: Array<RouteItem> = [
  { path: '/test', Component: Test },
  { path: '/config', Component: Config },
  { path: '/tools', Component: Tools },
  { path: '/pod/:name', Component: Pod },
  { path: '/', Component: Home },
];

export default routes;
