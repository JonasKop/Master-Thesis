import { Route, Switch } from 'react-router-dom';
import routes from './routes';

function Router() {
  return (
    <Switch>
      {routes.map((e) => (
        <Route key={e.path} exact path={e.path}>
          <e.Component />
        </Route>
      ))}
    </Switch>
  );
}

export default Router;
