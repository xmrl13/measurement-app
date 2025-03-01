import http from 'k6/http';
import { check, sleep } from 'k6';
import { htmlReport } from 'https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js';

export let options = {
    stages: [
        { duration: '10s', target: 10 },  // Sobe para 10 usuários em 10s
        { duration: '10s', target: 30 },  // Sobe para 30 usuários em mais 10s
        { duration: '10s', target: 50 },  // Sobe para 50 usuários em mais 10s
        { duration: '10s', target: 100 },  // Sobe para 50 usuários em mais 10s
        { duration: '10s', target: 200 },  // Sobe para 50 usuários em mais 10s
    ],
    thresholds: {
        http_req_duration: ['p(95)<1000'], // 95% das requisições devem ser < 1000ms
    },
};

const USER_BASE_URL = __ENV.USER_BASE_URL

const TOKEN = 'Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBlbWFpbC5jb20iLCJyb2xlIjoiQURNSU4iLCJpYXQiOjE3NDA4NTQ0NjUsImV4cCI6MTc0MDk0MDg2NX0.ce9ZshQDREF2-e1pbbooL169Q0tFoMDX2rcUV2KXdXY';

function generateRandomEmail() {
    const randomPart = Math.random().toString(36).substring(2, 6);
    // ex: "ab3z"
    return `testuser_${randomPart}@test.com`;
}

function generateRandomName() {
    const names = ['Alice', 'Bobert', 'Carlos', 'Dorothy', 'Evelyn'];
    const base = names[Math.floor(Math.random() * names.length)];
    return base.length < 5 ? base + 'Test' : base;
}
function generateRandomPassword() {
    const upper = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
    const lower = 'abcdefghijklmnopqrstuvwxyz';
    const digits = '0123456789';
    const special = '@#$%^&+=';

    function getRandomChar(str) {
        return str[Math.floor(Math.random() * str.length)];
    }

    let password = [
        getRandomChar(upper),
        getRandomChar(lower),
        getRandomChar(digits),
        getRandomChar(special),
    ];

    const allChars = upper + lower + digits + special;
    while (password.length < 8) {
        password.push(getRandomChar(allChars));
    }

    return password.sort(() => 0.5 - Math.random()).join('');
}

function generateSecretPhrase() {
    return 'secret-' + Math.random().toString(36).substring(2, 8);
}

export default function () {
    const email = generateRandomEmail();
    const name = generateRandomName();
    const password = generateRandomPassword();
    const secretPhrase = generateSecretPhrase();

    const createPayload = JSON.stringify({
        name: name,
        email: email,
        role: 'ADMIN',
        password: password,
        secretPhrase: secretPhrase,
    });

    const createParams = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': TOKEN,
        },
    };

    let createRes = http.post(`${USER_BASE_URL}/api/users`, createPayload, createParams);

    check(createRes, {
        'CreateUser: status 201': (r) => r.status === 201,
    });

    if (createRes.status !== 201) {
        sleep(1);
        return;
    }

    const newPassword = generateRandomPassword();

    const updatePayload = JSON.stringify({
        oldEmail: email,
        name: `${name}_UPDATED`,
        oldPassword: password,
        newPassword: newPassword,
        secretPhrase: secretPhrase,
    });

    let updateRes = http.put(`${USER_BASE_URL}/api/users/update`, updatePayload, {
        headers: { 'Content-Type': 'application/json' },
    });

    check(updateRes, {
        'UpdateUser: status 200 or 204': (r) => r.status === 200 || r.status === 204,
    });

    const deletePayload = JSON.stringify({
        email: email,
    });

    let deleteRes = http.del(`${USER_BASE_URL}/api/users`, deletePayload, {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': TOKEN,
        },
    });

    check(deleteRes, {
        'DeleteUser: status 200': (r) => r.status === 200,
    });

 //   sleep(0.1);
}

export function handleSummary(data) {
    console.log('Resumo dos testes:');
    console.log(JSON.stringify(data, null, 2));
    return {
        'result.html': htmlReport(data),
    };
}
