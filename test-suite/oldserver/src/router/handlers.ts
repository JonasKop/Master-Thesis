import { Request, Response } from 'express';

interface Credentials {
  username: string;
  password: string;
}

function validateCredentials({ username, password }: Credentials) {
  return username && password;
}

export function login(req: Request, res: Response): void {
  const credentials = <Credentials>req.body;
  if (!validateCredentials(credentials)) {
    res.status(400).json({ error: 'invalid credentials' });
    return;
  }
  res.status(200).json(credentials);
}
