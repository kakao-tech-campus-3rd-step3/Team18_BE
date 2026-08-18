import http from 'k6/http';
import { check, sleep } from 'k6';

const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';
const clubId = __ENV.CLUB_ID || '1';
const shortRun = __ENV.LOAD_TEST_SHORT === 'true';
const detailResponse = http.expectedStatuses({ min: 200, max: 399 }, 404);

export const options = {
  scenarios: {
    detail_entry: {
      executor: 'ramping-vus',
      startVUs: 1,
      stages: [
        { duration: shortRun ? '5s' : '30s', target: 5 },
        { duration: shortRun ? '10s' : '60s', target: 20 },
        { duration: shortRun ? '5s' : '30s', target: 0 },
      ],
      gracefulRampDown: '10s',
    },
    popular_polling: {
      executor: 'constant-vus',
      vus: Number(__ENV.POPULAR_POLL_VUS || 5),
      duration: shortRun ? '20s' : '2m',
      exec: 'popularPolling',
    },
    heartbeat: {
      executor: 'constant-vus',
      vus: Number(__ENV.HEARTBEAT_VUS || 5),
      duration: shortRun ? '35s' : '2m',
      exec: 'heartbeat',
    },
  },
};

export default function () {
  const detail = http.get(`${baseUrl}/api/clubs/${clubId}`, { responseCallback: detailResponse });
  check(detail, { 'club detail responds': (response) => response.status < 500 });

  const view = http.post(`${baseUrl}/api/clubs/${clubId}/views`, null, {
    tags: { endpoint: 'club-view' },
  });
  check(view, { 'view recording is accepted': (response) => response.status === 204 });

  sleep(Number(__ENV.DETAIL_ENTRY_INTERVAL_SECONDS || 30));
}

export function popularPolling() {
  const popular = http.get(`${baseUrl}/api/clubs/popular`, { tags: { endpoint: 'popular-clubs' } });
  check(popular, { 'popular list responds': (response) => response.status < 500 });
  sleep(Number(__ENV.POPULAR_POLL_INTERVAL_SECONDS || 10));
}

export function heartbeat() {
  const response = http.post(`${baseUrl}/api/clubs/${clubId}/heartbeat`, null,
    { tags: { endpoint: 'club-heartbeat' } });
  check(response, { 'heartbeat is accepted': (value) => value.status === 204 });
  sleep(Number(__ENV.HEARTBEAT_INTERVAL_SECONDS || 30));
}
