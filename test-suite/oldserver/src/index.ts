import readConfig from './config';
import startServer from './router/router';
import logger from './utilities';

const { USERNAME, PASSWORD } = process.env;
if (!USERNAME || !PASSWORD) {
  logger.error('Environment variables: $USERNAME and $PASSWORD must be set');
  process.exit(1);
}

console.log(readConfig());
startServer();
