import * as React from 'react';
import ReactDOM from 'react-dom';
import App from './App';
import { ChakraProvider } from '@chakra-ui/react';

// 1. Import the extendTheme function
import { extendTheme } from '@chakra-ui/react';
import { colors } from './lib/style';
// 2. Extend the theme to include custom colors, fonts, etc

const makeColorScheme = (color: string) =>
  [100, 200, 300, 400, 500, 600, 700, 800, 900].reduce((s, e) => ({ ...s, [e]: color }), {});

const theme = extendTheme({
  colors: {
    cbtt: makeColorScheme(colors.bluepurple),
    black: makeColorScheme('#000'),
  },
});

ReactDOM.render(
  <React.StrictMode>
    <ChakraProvider theme={theme} resetCSS>
      <App />
    </ChakraProvider>
  </React.StrictMode>,
  document.getElementById('root')
);
