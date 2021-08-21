import express from 'express';
import logger from '../utilities';
import { login } from './handlers';

function createRouter() {
  const app = express();
  app.use(express.json());
  app.post('/api/login', login);
  return app;
}

export default function startServer(): void {
  const port = process.env.PORT || '3000';
  const app = createRouter();

  app.listen(port, () => {
    logger.info(`Listening at http://localhost:${port}`);
  });
}
