import axios from 'axios';

const API_BASE = 'http://localhost:8080/api';

const client = axios.create({
  baseURL: API_BASE,
});

client.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const authApi = {
  register: (payload) => client.post('/auth/register', payload),
  login: (payload) => client.post('/auth/login', payload),
};

export const travelApi = {
  plan: (payload) => client.post('/travel/plan', payload),
  history: () => client.get('/travel/history'),
  update: (payload) => client.put('/travel/update', payload),
};
