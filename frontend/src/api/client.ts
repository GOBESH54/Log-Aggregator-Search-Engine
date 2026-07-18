import axios from 'axios';
import type { LogEntry, LogIngestRequest, LogSearchRequest, AiAnalysisResult } from '../types';

const api = axios.create({ baseURL: '/api' });

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('jwt');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export const authApi = {
  login: (username: string, password: string) =>
    api.post<{ token: string; expiresIn: number }>('/auth/login', { username, password }),
};

export const logsApi = {
  ingest: (logs: LogIngestRequest[]) =>
    api.post<string>('/logs/ingest', logs),

  search: (req: LogSearchRequest) =>
    api.post<LogEntry[]>('/logs/search', req),

  semanticSearch: (req: LogSearchRequest) =>
    api.post<LogEntry[]>('/logs/search/semantic', req),

  analyze: (logs: string[], query?: string) =>
    api.post<AiAnalysisResult>('/logs/analyze', { logs, query }),

  runAnomalyScan: () =>
    api.post<string>('/logs/anomaly/scan'),
};
