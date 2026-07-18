export interface LogEntry {
  id: string;
  source: string;
  level: 'ERROR' | 'WARN' | 'INFO' | 'DEBUG' | 'TRACE';
  message: string;
  timestamp: string;
  metadata?: string;
}

export interface LogIngestRequest {
  source: string;
  level: string;
  message: string;
  metadata?: string;
}

export interface LogSearchRequest {
  searchText?: string;
  semanticQuery?: string;
  source?: string;
  level?: string;
  startTime?: string;
  endTime?: string;
  page?: number;
  size?: number;
}

export interface AiAnalysisResult {
  summary?: string;
  anomalies?: string[];
  recommendations?: string[];
  rawResponse?: string;
}
