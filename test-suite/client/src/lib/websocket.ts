import { useEffect, useState } from 'react';

function useWebSocket<T>(url: string, fn: (data: T) => void, isJSON: boolean = true) {
  const [ws, setWs] = useState<WebSocket>();
  const [error, setError] = useState<any>();

  useEffect(() => {
    try {
      const socket = new WebSocket(url);
      socket.onopen = () => {
        socket.send('Message to send');
        console.log('Message is sent...');
      };

      socket.onerror = (evt) => {
        setError(evt);
      };
      socket.onmessage = ({ data }) => {
        const readData = isJSON ? JSON.parse(data) : data;
        fn(readData);
      };

      socket.onclose = () => {
        console.log('Connection is closed...');
      };
      setWs(socket);
    } catch (e) {}
  }, [fn, isJSON, url]);

  return { ws, error };
}

export default useWebSocket;
