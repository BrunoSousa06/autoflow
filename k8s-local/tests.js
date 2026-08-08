import http from 'k6/http'
import { check, sleep } from 'k6'

export const options = {
    stages: [
        { duration: '30s', target: 100 },
        { duration: '1m', target: 2000 },  
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


  let res = http.post('http://localhost:30080/auth/login', payload, params);

  check(res, { 'success login': (r) => r.status === 200 });

  sleep(0.3);
}