import http from 'k6/http';
import { check, sleep } from 'k6';

const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';
const clubId = __ENV.CLUB_ID || '1';

export const options = {
  scenarios: {
    mixed_club_popularity: {
      executor: 'ramping-vus',
      startVUs: 1,
      stages: [
        { duration: '30s', target: 5 },
        { duration: '60s', target: 20 },
        { duration: '30s', target: 0 },
      ],
      gracefulRampDown: '10s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<1000'],
  },
};

export default function () {
  const detail = http.get(`${baseUrl}/api/clubs/${clubId}`);
  check(detail, { 'club detail responds': (response) => response.status < 500 });

  const view = http.post(`${baseUrl}/api/clubs/${clubId}/views`, null, {
    tags: { endpoint: 'club-view' },
  });
  check(view, { 'view recording is accepted': (response) => response.status === 204 });

  const popular = http.get(`${baseUrl}/api/clubs/popular`, {
    tags: { endpoint: 'popular-clubs' },
  });
  check(popular, { 'popular list responds': (response) => response.status < 500 });

  if (__ITER % 3 === 0) {
    const heartbeat = http.post(`${baseUrl}/api/clubs/${clubId}/heartbeat`, null, {
      tags: { endpoint: 'club-heartbeat' },
    });
    check(heartbeat, { 'heartbeat is accepted': (response) => response.status === 204 });
  }

  sleep(1);
}
