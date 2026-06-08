const API_BASE = 'http://localhost:8080/api';

const request = axios.create({
    baseURL: API_BASE,
    timeout: 15000
});

request.interceptors.request.use(
    config => {
        const token = localStorage.getItem('player_token');
        if (token) {
            config.headers['Authorization'] = 'Bearer ' + token;
        }
        return config;
    },
    error => Promise.reject(error)
);

request.interceptors.response.use(
    response => {
        const res = response.data;
        if (res.code === 200) {
            return res.data;
        } else {
            vant.showToast({ message: res.message || '请求失败', icon: 'fail' });
            if (res.code === 401) {
                localStorage.removeItem('player_token');
                localStorage.removeItem('player_info');
                window.location.hash = '#/join';
            }
            return Promise.reject(res);
        }
    },
    error => {
        vant.showToast({ message: '网络错误', icon: 'fail' });
        return Promise.reject(error);
    }
);

const api = {
    joinGame(data) {
        return request.post('/player/join', data);
    },

    getSessionInfo() {
        return request.get('/player/session/info');
    },

    getMyScript() {
        return request.get('/player/script');
    },

    getClues(parentId = 0) {
        return request.get('/player/clues', { params: { parentId } });
    },

    getClueDetail(clueId) {
        return request.get(`/player/clue/${clueId}`);
    },

    unlockClue(clueId, password) {
        return request.post('/player/clue/unlock', { clueId, password });
    },

    getMessages() {
        return request.get('/player/messages');
    },

    getProgress() {
        return request.get('/player/progress');
    },

    updateProgress(data) {
        return request.post('/player/progress', data);
    },

    heartbeat() {
        return request.post('/player/heartbeat');
    }
};
