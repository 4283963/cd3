const API_BASE = 'http://localhost:8080/api';

const request = axios.create({
    baseURL: API_BASE,
    timeout: 15000
});

request.interceptors.request.use(
    config => {
        const token = localStorage.getItem('director_token');
        if (token) {
            config.headers['Authorization'] = 'Bearer ' + token;
        }
        return config;
    },
    error => {
        return Promise.reject(error);
    }
);

request.interceptors.response.use(
    response => {
        const res = response.data;
        if (res.code === 200) {
            return res.data;
        } else {
            ElementPlus.ElMessage.error(res.message || '请求失败');
            if (res.code === 401) {
                localStorage.removeItem('director_token');
                localStorage.removeItem('director_info');
                window.location.hash = '#/login';
            }
            return Promise.reject(res);
        }
    },
    error => {
        ElementPlus.ElMessage.error(error.message || '网络错误');
        return Promise.reject(error);
    }
);

const api = {
    login(data) {
        return request.post('/auth/login', data);
    },

    getScriptList(current = 1, size = 10, keyword = '') {
        return request.get('/director/script/page', {
            params: { current, size, keyword }
        });
    },

    getScriptDetail(id) {
        return request.get(`/director/script/${id}`);
    },

    getScriptRoles(scriptId) {
        return request.get(`/director/script/${scriptId}/roles`);
    },

    getClueTree(scriptId) {
        return request.get(`/director/script/${scriptId}/clue-tree`);
    },

    createScript(data) {
        return request.post('/director/script', data);
    },

    updateScript(data) {
        return request.put('/director/script', data);
    },

    deleteScript(id) {
        return request.delete(`/director/script/${id}`);
    },

    getCluesByScript(scriptId) {
        return request.get(`/director/clue/script/${scriptId}`);
    },

    getClueDetail(id) {
        return request.get(`/director/clue/${id}`);
    },

    createClue(data) {
        return request.post('/director/clue', data);
    },

    updateClue(data) {
        return request.put('/director/clue', data);
    },

    deleteClue(id) {
        return request.delete(`/director/clue/${id}`);
    },

    getSessionList(current = 1, size = 10) {
        return request.get('/director/session/page', {
            params: { current, size }
        });
    },

    getSessionDetail(id) {
        return request.get(`/director/session/${id}`);
    },

    createSession(scriptId) {
        return request.post('/director/session/create', null, {
            params: { scriptId }
        });
    },

    startSession(id) {
        return request.post(`/director/session/${id}/start`);
    },

    endSession(id) {
        return request.post(`/director/session/${id}/end`);
    },

    pauseSession(id) {
        return request.post(`/director/session/${id}/pause`);
    },

    resumeSession(id) {
        return request.post(`/director/session/${id}/resume`);
    },

    getSessionPlayers(sessionId) {
        return request.get(`/director/session/${sessionId}/players`);
    },

    assignRole(sessionId, playerId, roleId) {
        return request.post(`/director/session/${sessionId}/player/${playerId}/assign-role`, null, {
            params: { roleId }
        });
    },

    distributeClue(data) {
        return request.post('/director/session/distribute-clue', data);
    },

    sendMessage(data) {
        return request.post('/director/session/send-message', data);
    },

    getSessionMessages(sessionId) {
        return request.get(`/director/session/${sessionId}/messages`);
    }
};
