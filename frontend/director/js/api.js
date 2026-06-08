const API_BASE = 'http://localhost:8080/api';
const WS_BASE = 'ws://localhost:8080/api';

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

class WebSocketClient {
    constructor() {
        this.ws = null;
        this.sessionId = null;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 10;
        this.reconnectDelay = 3000;
        this.heartbeatInterval = null;
        this.messageHandlers = {};
        this.isManualClose = false;
    }

    connect(sessionId) {
        return new Promise((resolve, reject) => {
            if (this.ws && this.ws.readyState === WebSocket.OPEN) {
                if (this.sessionId === sessionId) {
                    resolve();
                    return;
                }
                this.close();
            }

            this.sessionId = sessionId;
            this.isManualClose = false;
            this.reconnectAttempts = 0;

            const token = localStorage.getItem('director_token');
            const wsUrl = `${WS_BASE}/ws/director/${sessionId}?token=${token}`;

            try {
                this.ws = new WebSocket(wsUrl);
            } catch (e) {
                reject(e);
                return;
            }

            this.ws.onopen = () => {
                console.log('[WebSocket] 连接成功');
                this.reconnectAttempts = 0;
                this.startHeartbeat();
                resolve();
            };

            this.ws.onmessage = (event) => {
                try {
                    const message = JSON.parse(event.data);
                    this.handleMessage(message);
                } catch (e) {
                    console.warn('[WebSocket] 消息解析失败:', e);
                }
            };

            this.ws.onerror = (error) => {
                console.error('[WebSocket] 连接错误:', error);
                reject(error);
            };

            this.ws.onclose = (event) => {
                console.log('[WebSocket] 连接关闭:', event.code, event.reason);
                this.stopHeartbeat();
                if (!this.isManualClose && this.reconnectAttempts < this.maxReconnectAttempts) {
                    this.scheduleReconnect();
                }
            };
        });
    }

    handleMessage(message) {
        const { type, data } = message;
        const handler = this.messageHandlers[type];
        if (handler) {
            try {
                handler(data);
            } catch (e) {
                console.error('[WebSocket] 消息处理异常:', type, e);
            }
        }

        if (this.messageHandlers['*']) {
            try {
                this.messageHandlers['*'](message);
            } catch (e) {
                console.error('[WebSocket] 通用消息处理异常:', e);
            }
        }
    }

    on(type, handler) {
        this.messageHandlers[type] = handler;
    }

    off(type) {
        delete this.messageHandlers[type];
    }

    send(type, data) {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify({ type, data }));
        }
    }

    startHeartbeat() {
        this.stopHeartbeat();
        this.heartbeatInterval = setInterval(() => {
            if (this.ws && this.ws.readyState === WebSocket.OPEN) {
                this.send('heartbeat', { ts: Date.now() });
            }
        }, 25000);
    }

    stopHeartbeat() {
        if (this.heartbeatInterval) {
            clearInterval(this.heartbeatInterval);
            this.heartbeatInterval = null;
        }
    }

    scheduleReconnect() {
        this.reconnectAttempts++;
        const delay = this.reconnectDelay * Math.min(this.reconnectAttempts, 5);
        console.log(`[WebSocket] ${delay / 1000}秒后尝试重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);

        setTimeout(() => {
            if (!this.isManualClose && this.sessionId) {
                console.log('[WebSocket] 正在重连...');
                this.connect(this.sessionId).catch(() => {});
            }
        }, delay);
    }

    close() {
        this.isManualClose = true;
        this.stopHeartbeat();
        if (this.ws) {
            this.ws.close();
            this.ws = null;
        }
        this.messageHandlers = {};
    }

    get isConnected() {
        return this.ws && this.ws.readyState === WebSocket.OPEN;
    }
}

const directorWS = new WebSocketClient();

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
