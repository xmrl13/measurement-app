import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';
import { htmlReport } from './libs/k6-reporter.js';

// ---------------------------------------------------
//  MÉTRICAS CUSTOMIZADAS (Counters)
// ---------------------------------------------------
export let get200 = new Counter('GET_200');
export let get404 = new Counter('GET_404');
export let get403 = new Counter('GET_403');

export let update200 = new Counter('UPDATE_200');
export let update404 = new Counter('UPDATE_404');
export let update403 = new Counter('UPDATE_403');

export let create201 = new Counter('CREATE_201');
export let create409 = new Counter('CREATE_409');
export let create403 = new Counter('CREATE_403');

export let delete200 = new Counter('DELETE_200');
export let delete404 = new Counter('DELETE_404');
export let delete403 = new Counter('DELETE_403');

// ---------------------------------------------------
//  Configurações do teste
// ---------------------------------------------------
export let options = {
    stages: [
        { duration: '10s', target: 10 },
        { duration: '10s', target: 30 },
        { duration: '10s', target: 50 },
        { duration: '10s', target: 100 },
        { duration: '10s', target: 200 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<1000'], // 95% das requisições devem ser < 1000ms
    },
};

const USER_BASE_URL = __ENV.USER_BASE_URL;
const TOKEN = 'Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBlbWFpbC5jb20iLCJyb2xlIjoiQURNSU4iLCJpYXQiOjE3NDA4NjUwNzEsImV4cCI6MTc0MDk1MTQ3MX0.oNjApP-wxyET4RHxnEMzZFR2IGKDEk_CV7M3GhTb5_Y';

/**
 * Probabilidade das ações:
 *  - GET:    80%
 *  - UPDATE: 10%
 *  - CREATE:  5%
 *  - DELETE:  5%
 */
function pickAction() {
    const r = Math.random();
    if (r < 0.80) return 'GET';
    else if (r < 0.90) return 'UPDATE';
    else if (r < 0.95) return 'CREATE';
    else return 'DELETE';
}

// ---------------------------------------------------
//  Helpers
// ---------------------------------------------------
function generateRandomEmail(prefix = 'test') {
    const rand = Math.random().toString(36).substring(2, 6);
    return `${prefix}_${rand}@test.com`;
}

function createUserPayload(email) {
    return JSON.stringify({
        name: 'LoadTestUser',
        email: email,
        role: 'ADMIN',
        password: 'P@ssw0rd123',
        secretPhrase: 'secret-xyz',
    });
}

function updateUserPayload(email) {
    return JSON.stringify({
        name: 'UpdatedName',
        oldEmail: email,
        newPassword: 'N3wP@ssw0rd!',
        secretPhrase: 'secret-xyz',
    });
}

function deleteUserPayload(email) {
    return JSON.stringify({ email: email });
}

// ---------------------------------------------------
// setup: cria 150 usuários só para GET e 50 para UPDATE/DELETE
// ---------------------------------------------------
export function setup() {
    const GET_USERS_COUNT = 150;
    const WRITE_USERS_COUNT = 50;

    let getEmails = [];
    let writeEmails = [];

    // 1) Cria 150 e-mails para GET
    for (let i = 0; i < GET_USERS_COUNT; i++) {
        let email = generateRandomEmail('get');
        let res = http.post(`${USER_BASE_URL}/api/users`, createUserPayload(email), {
            headers: { 'Content-Type': 'application/json', 'Authorization': TOKEN },
        });
        // Se 201 ou 409, guardamos
        if (res.status === 201 || res.status === 409) {
            getEmails.push(email);
        }
    }

    // 2) Cria 50 e-mails para UPDATE/DELETE
    for (let j = 0; j < WRITE_USERS_COUNT; j++) {
        let email = generateRandomEmail('write');
        let res = http.post(`${USER_BASE_URL}/api/users`, createUserPayload(email), {
            headers: { 'Content-Type': 'application/json', 'Authorization': TOKEN },
        });
        if (res.status === 201 || res.status === 409) {
            writeEmails.push(email);
        }
    }

    return {
        getEmails: getEmails,
        writeEmails: writeEmails,
    };
}

// ---------------------------------------------------
// default: fluxo principal
// ---------------------------------------------------
export default function (data) {
    const { getEmails, writeEmails } = data;
    const action = pickAction();

    switch (action) {
        case 'GET': {
            // 80% GET - usa somente e-mails do grupo GET
            let email = getEmails[Math.floor(Math.random() * getEmails.length)];
            let url = `${USER_BASE_URL}/api/users/get-by-email?email=${email}`;
            let res = http.get(url, {
                headers: { 'Authorization': TOKEN },
            });

            // Verificação (check)
            check(res, {
                'GET user: 200/404/403': (r) => [200, 404, 403].includes(r.status),
            });

            // Incrementa counters de acordo com o status
            if (res.status === 200) {
                get200.add(1);
            } else if (res.status === 404) {
                get404.add(1);
            } else if (res.status === 403) {
                get403.add(1);
            }
            break;
        }

        case 'UPDATE': {
            // 10% UPDATE - usa e-mails do grupo de escrita
            let email = writeEmails[Math.floor(Math.random() * writeEmails.length)];
            let payload = updateUserPayload(email);

            let res = http.put(`${USER_BASE_URL}/api/users/update`, payload, {
                headers: { 'Content-Type': 'application/json' },
            });

            check(res, {
                'UpdateUser: 200/404/403': (r) => [200, 404, 403].includes(r.status),
            });

            if (res.status === 200) {
                update200.add(1);
            } else if (res.status === 404) {
                update404.add(1);
            } else if (res.status === 403) {
                update403.add(1);
            }
            break;
        }

        case 'CREATE': {
            // 5% CREATE - gera sempre e-mail novo
            let email = generateRandomEmail('create');
            let payload = createUserPayload(email);

            let res = http.post(`${USER_BASE_URL}/api/users`, payload, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': TOKEN,
                },
            });

            check(res, {
                'CreateUser: 201/409/403': (r) => [201, 409, 403].includes(r.status),
            });

            if (res.status === 201) {
                create201.add(1);
            } else if (res.status === 409) {
                create409.add(1);
            } else if (res.status === 403) {
                create403.add(1);
            }
            break;
        }

        case 'DELETE': {
            // 5% DELETE - usa e-mails do grupo de escrita
            let email = writeEmails[Math.floor(Math.random() * writeEmails.length)];
            let payload = deleteUserPayload(email);

            let res = http.del(`${USER_BASE_URL}/api/users`, payload, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': TOKEN,
                },
            });

            check(res, {
                'DeleteUser: 200/404/403': (r) => [200, 404, 403].includes(r.status),
            });

            if (res.status === 200) {
                delete200.add(1);
            } else if (res.status === 404) {
                delete404.add(1);
            } else if (res.status === 403) {
                delete403.add(1);
            }
            break;
        }
    }

    sleep(0.3);
}

// ---------------------------------------------------
// handleSummary: gera relatório HTML no final
// ---------------------------------------------------
export function handleSummary(data) {
    console.log('Resumo dos testes:');
    console.log(JSON.stringify(data, null, 2));
    return {
        'result.html': htmlReport(data),
    };
}
