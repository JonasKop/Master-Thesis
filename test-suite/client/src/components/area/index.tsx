import { colors } from '../../lib/style';
import { Box, BoxProps } from '@chakra-ui/react';
import { ReactNode } from 'react';

interface AreaProps extends BoxProps {
  children: ReactNode;
}

function Area({ children, ...props }: AreaProps) {
  return (
    <Box {...props} width="100%" borderRadius="4px" display="grid" gridGap="20px" padding="30px" backgroundColor={colors.accent}>
      {children}
    </Box>
  );
}

export default Area;
