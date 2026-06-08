const API_BASE = 'http://localhost:8080/api';
const WS_BASE = 'ws://localhost:8080/api/ws';

class WebSocketClient {
    constructor() {
        this.ws = null;
        this.sessionId = null;
        this.handlers = {};
        this.reconnectCount = 0;
        this.maxReconnect = 10;
        this.heartbeatInterval = null;
        this.reconnectTimer = null;
        this.manualClose = false;
        this.connected = false;
    }

    connect(sessionId) {
        return new Promise((resolve, reject) => {
            if (this.ws && this.connected) {
                resolve();
                return;
            }

            this.manualClose = false;
            this.sessionId = sessionId;
            const token = localStorage.getItem('player_token');
            const url = `${WS_BASE}/player/${sessionId}?token=${encodeURIComponent(token)}`;

            try {
                this.ws = new WebSocket(url);
            } catch (e) {
                reject(e);
                return;
            }

            this.ws.onopen = () => {
                console.log('[玩家端WebSocket] 连接成功');
                this.connected = true;
                this.reconnectCount = 0;
                this.startHeartbeat();
                this.emit('connected');
                resolve();
            };

            this.ws.onmessage = (event) => {
                try {
                    const message = JSON.parse(event.data);
                    if (message.type === 'heartbeat') {
                        return;
                    }
                    console.log('[玩家端WebSocket] 收到消息:', message);
                    this.emit(message.type, message.data);
                } catch (e) {
                    console.error('[玩家端WebSocket] 消息解析失败:', e);
                }
            };

            this.ws.onclose = (event) => {
                console.log('[玩家端WebSocket] 连接关闭, code:', event.code, 'reason:', event.reason);
                this.connected = false;
                this.stopHeartbeat();
                if (!this.manualClose) {
                    this.scheduleReconnect();
                }
                this.emit('disconnected');
            };

            this.ws.onerror = (error) => {
                console.error('[玩家端WebSocket] 连接错误:', error);
                this.connected = false;
                if (!this.manualClose) {
                    this.scheduleReconnect();
                }
                reject(error);
            };
        });
    }

    on(type, handler) {
        if (!this.handlers[type]) {
            this.handlers[type] = [];
        }
        this.handlers[type].push(handler);
        return () => this.off(type, handler);
    }

    off(type, handler) {
        if (!this.handlers[type]) return;
        const index = this.handlers[type].indexOf(handler);
        if (index > -1) {
            this.handlers[type].splice(index, 1);
        }
    }

    emit(type, data) {
        if (this.handlers[type]) {
            this.handlers[type].forEach(h => {
                try {
                    h(data);
                } catch (e) {
                    console.error('[玩家端WebSocket] 消息处理错误:', e);
                }
            });
        }
    }

    send(type, data) {
        if (!this.ws || !this.connected) {
            console.warn('[玩家端WebSocket] 未连接，无法发送消息');
            return false;
        }
        try {
            this.ws.send(JSON.stringify({ type, data }));
            return true;
        } catch (e) {
            console.error('[玩家端WebSocket] 发送失败:', e);
            return false;
        }
    }

    startHeartbeat() {
        this.stopHeartbeat();
        this.heartbeatInterval = setInterval(() => {
            if (this.connected) {
                this.send('heartbeat', { timestamp: Date.now() });
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
        if (this.reconnectTimer) {
            clearTimeout(this.reconnectTimer);
        }
        if (this.reconnectCount >= this.maxReconnect) {
            console.error('[玩家端WebSocket] 重连次数已达上限');
            return;
        }

        this.reconnectCount++;
        const delay = Math.min(1000 * Math.pow(2, this.reconnectCount - 1), 30000);
        console.log(`[玩家端WebSocket] ${delay / 1000}秒后尝试第${this.reconnectCount}次重连`);

        this.reconnectTimer = setTimeout(() => {
            if (this.sessionId && !this.manualClose) {
                this.connect(this.sessionId).catch(() => {});
            }
        }, delay);
    }

    close() {
        this.manualClose = true;
        this.stopHeartbeat();
        if (this.reconnectTimer) {
            clearTimeout(this.reconnectTimer);
            this.reconnectTimer = null;
        }
        if (this.ws) {
            this.ws.close();
            this.ws = null;
        }
        this.connected = false;
        this.handlers = {};
        console.log('[玩家端WebSocket] 手动关闭连接');
    }

    isConnected() {
        return this.connected;
    }
}

const playerWS = new WebSocketClient();

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
