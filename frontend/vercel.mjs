const apiOrigin = process.env.RENDER_API_ORIGIN;
if (!apiOrigin) {
  throw new Error('Set RENDER_API_ORIGIN to the HTTPS origin of the Render API before deploying.');
}

const target = new URL(apiOrigin);
if (target.protocol !== 'https:' || target.origin !== apiOrigin.replace(/\/$/, '')) {
  throw new Error('RENDER_API_ORIGIN must be an HTTPS origin without credentials, path, query, or fragment.');
}

export const config = {
  framework: 'vite',
  buildCommand: 'npm run build',
  installCommand: 'npm ci',
  outputDirectory: 'dist',
  rewrites: [
    { source: '/api/:path*', destination: `${target.origin}/api/:path*` },
    { source: '/(.*)', destination: '/index.html' },
  ],
};
