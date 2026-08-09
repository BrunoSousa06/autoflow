import http from 'k6/http'
import { check, sleep } from 'k6'

export const options = {
    stages: [
        { duration: '1m', target: 100 },
        { duration: '2m', target: 250 }, 
        { duration: '3m', target: 250 }, 
        { duration: '1m', target: 0 },
    ],
};

export default function () {
  const payload = JSON.stringify({ 
    email: 'admin@autoflow.com', 
    senha: 'Senha@1234' 
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };


  let res = http.post('http://127.0.0.1:53222/auth/login', payload, params);

  check(res, { 'success login': (r) => r.status === 200 });

  sleep(1);
}