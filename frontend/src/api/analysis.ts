export async function streamAnalysis(
  searchText: string,
  prompt: string,
  signal: AbortSignal,
  onText: (chunk: string) => void,
) {
  const token = localStorage.getItem('jwt');
  const response = await fetch('/api/logs/analyze/stream', {
    method: 'POST',
    headers: {
      Accept: 'text/event-stream, application/json',
      'Content-Type': 'application/json',
      ...(token && { Authorization: `Bearer ${token}` }),
    },
    body: JSON.stringify({ searchText: searchText.trim() || undefined, prompt: prompt.trim() || undefined }),
    signal,
  });
  if (!response.ok) {
    const error = await response.json().catch(() => null);
    throw new Error(error?.message || `Analysis failed (HTTP ${response.status}).`);
  }
  if (!response.headers.get('content-type')?.includes('text/event-stream') || !response.body) {
    throw new Error('The API did not return an analysis stream. Check the connection to the backend.');
  }

  // Network chunks can split a UTF-8 character, SSE field, or event delimiter.
  const reader = response.body.pipeThrough(new TextDecoderStream()).getReader();
  let pending = '';
  while (true) {
    const { done, value } = await reader.read();
    if (done) break;
    pending += value;
    const events = pending.split(/\r?\n\r?\n/);
    pending = events.pop()!;
    for (const event of events) {
      const data = event.split(/\r?\n/)
        .filter((line) => line.startsWith('data:'))
        .map((line) => line.slice(5));
      if (data.length) onText(data.join('\n'));
    }
  }
}
