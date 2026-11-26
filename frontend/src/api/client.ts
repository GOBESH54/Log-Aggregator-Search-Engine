import axios from 'axios';
import type { LogEntry, LogIngestRequest, LogSearchRequest } from '../types';

const api = axios.create({ baseURL: '/api' });

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('jwt');
  if (token && config.url !== '/auth/login') config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (response) => {
    if (String(response.headers['content-type']).includes('text/html')) {
      throw new Error('The API returned a web page. Check the connection to the backend.');
    }
    return response;
  },
  (error: unknown) => {
    if (axios.isAxiosError(error)) {
      const data = error.response?.data;
      const details = Array.isArray(data?.details) ? data.details.join(' · ') : '';
      const message = details || data?.message;
      if (typeof message === 'string' && message) error.message = message;
      if (!error.response) error.message = 'Could not reach the API. Try again in a moment.';
    }
    return Promise.reject(error);
  },
);

export const authApi = {
  async login(username: string, password: string) {
    const response = await api.post<{ token: string; expiresInSeconds: number }>('/auth/login', { username, password });
    if (typeof response.data?.token !== 'string' || !response.data.token.trim()) {
      throw new Error('The API returned an invalid sign-in response.');
    }
    return response;
  },
};

export const logsApi = {
  ingest: (logs: LogIngestRequest[]) =>
    api.post<string>('/logs/ingest', logs),

  search: (req: LogSearchRequest) =>
    api.post<LogEntry[]>('/logs/search', req),

  semanticSearch: (req: LogSearchRequest) =>
    api.post<LogEntry[]>('/logs/search/semantic', req),

  runAnomalyScan: () =>
    api.post<string>('/logs/anomaly/scan'),
};
