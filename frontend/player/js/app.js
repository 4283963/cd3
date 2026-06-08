const { createApp, ref, reactive, computed, onMounted, onUnmounted, watch } = Vue;
const { createRouter, createWebHashHistory, useRouter, useRoute } = VueRouter;

const JoinPage = {
    template: `
        <div class="join-page">
            <div class="join-logo">🎭</div>
            <div class="join-title">沉浸式剧本杀</div>
            <div class="join-subtitle">开启你的推理之旅</div>
            
            <div class="join-card">
                <van-field
                    v-model="form.sessionCode"
                    label="房间码"
                    placeholder="请输入6位房间码"
                    maxlength="6"
                    class="join-input"
                    center>
                    <template #left-icon>
                        <van-icon name="friends-o" size="20" />
                    </template>
                </van-field>
                <van-field
                    v-model="form.nickname"
                    label="昵称"
                    placeholder="请输入你的昵称"
                    maxlength="20"
                    class="join-input"
                    center>
                    <template #left-icon>
                        <van-icon name="user-o" size="20" />
                    </template>
                </van-field>
                <van-button
                    type="primary"
                    block
                    round
                    size="large"
                    class="join-btn"
                    :loading="loading"
                    @click="handleJoin">
                    加入游戏
                </van-button>
            </div>
            
            <div style="margin-top: 40px; color: rgba(255,255,255,0.5); font-size: 12px;">
                请向导演获取房间码
            </div>
        </div>
    `,
    setup() {
        const router = useRouter();
        const loading = ref(false);
        const form = reactive({
            sessionCode: '',
            nickname: ''
        });

        const handleJoin = async () => {
            if (!form.sessionCode || form.sessionCode.length < 6) {
                vant.showToast('请输入有效的房间码');
                return;
            }
            if (!form.nickname) {
                vant.showToast('请输入你的昵称');
                return;
            }
            loading.value = true;
            try {
                const res = await api.joinGame({
                    sessionCode: form.sessionCode.toUpperCase(),
                    nickname: form.nickname,
                    avatar: ''
                });
                localStorage.setItem('player_token', res.token);
                localStorage.setItem('player_info', JSON.stringify({
                    playerId: res.playerId,
                    nickname: res.nickname,
                    sessionId: res.sessionId,
                    sessionCode: res.sessionCode
                }));
                vant.showToast({ message: '加入成功', icon: 'success' });
                setTimeout(() => {
                    router.push('/');
                }, 500);
            } catch (e) {
            } finally {
                loading.value = false;
            }
        };

        return { form, loading, handleJoin };
    }
};

const TabBarLayout = {
    template: `
        <div class="page-container">
            <router-view />
            <van-tabbar v-model="active" route safe-area-inset-bottom>
                <van-tabbar-item icon="home-o" to="/">大厅</van-tabbar-item>
                <van-tabbar-item icon="book-o" to="/script">剧本</van-tabbar-item>
                <van-tabbar-item icon="search" to="/clues">
                    <template #icon>
                        <div style="position: relative; display: inline-block;">
                            <van-icon name="search" size="20" />
                            <span v-if="newClueCount > 0" class="tab-badge">{{ newClueCount > 99 ? '99+' : newClueCount }}</span>
                        </div>
                    </template>
                    线索
                </van-tabbar-item>
                <van-tabbar-item icon="comment-o" to="/messages">
                    <template #icon>
                        <div style="position: relative; display: inline-block;">
                            <van-icon name="comment-o" size="20" />
                            <span v-if="newMsgCount > 0" class="tab-badge">{{ newMsgCount > 99 ? '99+' : newMsgCount }}</span>
                        </div>
                    </template>
                    消息
                </van-tabbar-item>
            </van-tabbar>
        </div>
    `,
    setup() {
        const route = useRoute();
        const router = useRouter();
        const active = ref(0);
        const newClueCount = ref(0);
        const newMsgCount = ref(0);

        watch(() => route.path, (path) => {
            const map = { '/': 0, '/script': 1, '/clues': 2, '/messages': 3 };
            active.value = map[path] ?? 0;
            if (path === '/clues') {
                newClueCount.value = 0;
            }
            if (path === '/messages') {
                newMsgCount.value = 0;
            }
        });

        const initWebSocket = async () => {
            const playerInfo = JSON.parse(localStorage.getItem('player_info') || '{}');
            if (!playerInfo.sessionId) return;

            try {
                await playerWS.connect(playerInfo.sessionId);

                playerWS.on('new_clue', (data) => {
                    console.log('[玩家端] 收到新线索:', data);
                    newClueCount.value++;
                    vant.Notify({
                        type: 'success',
                        message: '🎁 收到新线索！',
                        duration: 3000
                    });
                });

                playerWS.on('new_message', (data) => {
                    console.log('[玩家端] 收到新消息:', data);
                    newMsgCount.value++;
                    vant.Notify({
                        type: 'primary',
                        message: data?.title || '📨 收到新消息',
                        duration: 3000
                    });
                });

                playerWS.on('game_status_change', (data) => {
                    console.log('[玩家端] 游戏状态变化:', data);
                    vant.Notify({
                        type: 'warning',
                        message: '游戏状态已更新',
                        duration: 2000
                    });
                });

                playerWS.on('role_assigned', (data) => {
                    console.log('[玩家端] 角色已分配:', data);
                    vant.Notify({
                        type: 'success',
                        message: '🎭 你的角色已分配！',
                        duration: 3000
                    });
                });

            } catch (e) {
                console.error('[玩家端] WebSocket连接失败:', e);
            }
        };

        onMounted(() => {
            initWebSocket();
        });

        onUnmounted(() => {
            playerWS.close();
        });

        return { active, newClueCount, newMsgCount };
    }
};

const HomePage = {
    template: `
        <div>
            <div class="page-header">
                <div class="header-script-name">{{ sessionInfo.script?.title || '加载中...' }}</div>
                <div class="header-player-info">
                    <div class="header-avatar">{{ sessionInfo.player?.nickname?.charAt?.(0) || '玩' }}</div>
                    <div>
                        <div style="font-weight: bold;">{{ sessionInfo.player?.nickname || '玩家' }}</div>
                        <div style="font-size: 11px; opacity: 0.8;">
                            房间码: <span style="font-family: monospace;">{{ sessionInfo.session?.sessionCode || '------' }}</span>
                        </div>
                    </div>
                </div>
            </div>

            <div class="page-content">
                <div v-if="sessionInfo.role" class="role-card" @click="goToScript">
                    <img :src="sessionInfo.role.avatar" class="role-avatar" />
                    <div class="role-info">
                        <div class="role-name">{{ sessionInfo.role.name }}</div>
                        <div class="role-desc">{{ sessionInfo.role.description }}</div>
                    </div>
                    <van-icon name="arrow" style="color: #fff;" />
                </div>

                <div v-if="!sessionInfo.role" class="card">
                    <div style="text-align: center; padding: 20px 0;">
                        <van-icon name="user-o" size="48" color="#ddd" />
                        <div style="margin-top: 10px; color: #999;">角色分配中...</div>
                        <div style="margin-top: 4px; font-size: 12px; color: #ccc;">请等待导演分配角色</div>
                    </div>
                </div>

                <div class="section-title">快捷功能</div>
                <div class="quick-actions">
                    <div class="quick-action-item" @click="goToScript">
                        <div class="quick-action-icon" style="background: #ecf5ff; color: #409eff;">
                            📖
                        </div>
                        <div class="quick-action-text">我的剧本</div>
                    </div>
                    <div class="quick-action-item" @click="goToClues">
                        <div class="quick-action-icon" style="background: #f0f9eb; color: #67c23a;">
                            🔍
                        </div>
                        <div class="quick-action-text">线索库</div>
                    </div>
                    <div class="quick-action-item" @click="goToMessages">
                        <div class="quick-action-icon" style="background: #fdf6ec; color: #e6a23c;">
                            💬
                        </div>
                        <div class="quick-action-text">消息</div>
                    </div>
                </div>

                <div class="section-title">游戏状态</div>
                <div class="card">
                    <div style="display: flex; justify-content: space-between; padding: 8px 0;">
                        <span class="text-muted">游戏状态</span>
                        <span :class="statusClass">{{ statusText }}</span>
                    </div>
                    <div style="display: flex; justify-content: space-between; padding: 8px 0;">
                        <span class="text-muted">当前阶段</span>
                        <span>{{ sessionInfo.session?.currentStage || '准备中' }}</span>
                    </div>
                    <div style="display: flex; justify-content: space-between; padding: 8px 0;">
                        <span class="text-muted">已获线索</span>
                        <span>{{ clueCount }} 条</span>
                    </div>
                    <div style="display: flex; justify-content: space-between; padding: 8px 0;">
                        <span class="text-muted">已解锁</span>
                        <span style="color: #67c23a;">{{ unlockedClueCount }} 条</span>
                    </div>
                </div>
            </div>
        </div>
    `,
    setup() {
        const router = useRouter();
        const sessionInfo = ref({});
        const clueCount = ref(0);
        const unlockedClueCount = ref(0);

        const statusText = computed(() => {
            const status = sessionInfo.value.session?.status;
            const map = { 0: '待开始', 1: '进行中', 2: '已结束', 3: '已暂停' };
            return map[status] || '未知';
        });

        const statusClass = computed(() => {
            const status = sessionInfo.value.session?.status;
            const map = { 0: 'status-waiting', 1: 'status-playing', 2: 'status-ended', 3: 'status-paused' };
            return map[status] || '';
        });

        const loadInfo = async () => {
            try {
                const res = await api.getSessionInfo();
                sessionInfo.value = res;
            } catch (e) {}
            try {
                const clues = await api.getClues(0);
                clueCount.value = clues?.length || 0;
                unlockedClueCount.value = (clues || []).filter(c => c.isUnlocked === 1).length;
            } catch (e) {}
        };

        const goToScript = () => router.push('/script');
        const goToClues = () => router.push('/clues');
        const goToMessages = () => router.push('/messages');

        let timer = null;
        onMounted(() => {
            loadInfo();
            timer = setInterval(() => {
                api.heartbeat().catch(() => {});
            }, 30000);
        });

        onUnmounted(() => {
            if (timer) clearInterval(timer);
        });

        return {
            sessionInfo, clueCount, unlockedClueCount,
            statusText, statusClass,
            goToScript, goToClues, goToMessages
        };
    }
};

const ScriptPage = {
    template: `
        <div>
            <div class="page-header">
                <div class="header-script-name">我的剧本</div>
                <div class="header-player-info">
                    <span class="session-code-display">{{ playerInfo?.sessionCode || '------' }}</span>
                </div>
            </div>

            <div class="page-content">
                <div v-if="role" class="role-card" style="margin-bottom: 16px;">
                    <img :src="role.avatar" class="role-avatar" />
                    <div class="role-info">
                        <div class="role-name">{{ role.name }}</div>
                        <div class="role-desc">{{ role.description }}</div>
                    </div>
                </div>

                <div v-if="!role" class="empty-state">
                    <van-icon name="user-o" size="48" />
                    <div class="empty-text">角色尚未分配</div>
                    <div style="font-size: 12px; color: #ccc; margin-top: 4px;">请等待导演分配角色</div>
                </div>

                <div v-if="role && role.backgroundStory" class="script-content">
                    <h3 style="text-align: center; margin-bottom: 20px; color: #8b4513;">
                        —— 角色剧本 ——
                    </h3>
                    <p v-for="(p, i) in paragraphs" :key="i">{{ p }}</p>
                </div>
            </div>
        </div>
    `,
    setup() {
        const role = ref(null);
        const playerInfo = ref({});

        const paragraphs = computed(() => {
            if (!role.value?.backgroundStory) return [];
            return role.value.backgroundStory.split('\n').filter(p => p.trim());
        });

        const loadScript = async () => {
            try {
                const res = await api.getMyScript();
                role.value = res;
            } catch (e) {
                role.value = null;
            }
        };

        onMounted(() => {
            const info = localStorage.getItem('player_info');
            if (info) playerInfo.value = JSON.parse(info);
            loadScript();
        });

        return { role, playerInfo, paragraphs };
    }
};

const CluesPage = {
    template: `
        <div>
            <div class="page-header">
                <div class="header-script-name">线索库</div>
                <div class="header-player-info">
                    <span style="font-size: 12px; opacity: 0.8;">
                        已获得 {{ clues.length }} 条线索
                    </span>
                </div>
            </div>

            <div class="page-content">
                <div v-if="parentId > 0" style="margin-bottom: 12px;">
                    <van-button type="default" size="small" icon="arrow-left" @click="goBack">
                        返回上级
                    </van-button>
                </div>

                <div v-if="loading" style="text-align: center; padding: 40px;">
                    <van-loading color="#1989fa">加载中...</van-loading>
                </div>

                <div v-else-if="clues.length === 0" class="empty-state">
                    <van-icon name="search" size="48" />
                    <div class="empty-text">暂无线索</div>
                    <div style="font-size: 12px; color: #ccc; margin-top: 4px;">继续探索发现更多线索</div>
                </div>

                <div v-else>
                    <div v-for="clue in clues" :key="clue.id"
                         :class="['clue-item', clue.isUnlocked === 1 ? 'unlocked' : 'locked']"
                         @click="viewClue(clue)">
                        <div class="clue-icon">
                            {{ clueIcon(clue.type) }}
                        </div>
                        <div class="clue-info">
                            <div class="clue-name">
                                <span class="clue-type-badge" :class="'type-' + typeClass(clue.type)">
                                    {{ typeText(clue.type) }}
                                </span>
                                <span v-if="clue.isPuzzle === 1" class="clue-type-badge type-puzzle" style="margin-left: 4px;">
                                    🧩 拼图
                                </span>
                                {{ clue.name }}
                            </div>
                            <div class="clue-desc">
                                <template v-if="clue.isUnlocked === 1">
                                    点击查看详情
                                </template>
                                <template v-else-if="clue.isPuzzle === 1 && clue.puzzleStatus === 3">
                                    ⚠️ 线索已销毁
                                </template>
                                <template v-else-if="clue.isPuzzle === 1">
                                    {{ clue.unlockHint || '完成拼图解锁' }}
                                </template>
                                <template v-else>
                                    {{ clue.unlockHint || '需要密码解锁' }}
                                </template>
                            </div>
                        </div>
                        <van-icon name="arrow" class="clue-arrow" />
                    </div>
                </div>
            </div>

            <van-dialog
                v-model:show="showUnlockDialog"
                title="输入密码"
                show-cancel-button
                @confirm="confirmUnlock">
                <div style="padding: 16px 0;">
                    <div v-if="currentClue?.unlockHint" style="color: #999; font-size: 13px; margin-bottom: 12px;">
                        💡 提示: {{ currentClue.unlockHint }}
                    </div>
                    <van-field
                        v-model="unlockPassword"
                        placeholder="请输入密码"
                        clearable />
                </div>
            </van-dialog>
        </div>
    `,
    setup() {
        const router = useRouter();
        const route = useRoute();
        const clues = ref([]);
        const loading = ref(false);
        const parentId = ref(0);
        const showUnlockDialog = ref(false);
        const unlockPassword = ref('');
        const currentClue = ref(null);

        const clueIcon = (type) => {
            const map = { 1: '📝', 2: '🖼️', 3: '🎵', 4: '🎬' };
            return map[type] || '📝';
        };
        const typeText = (type) => {
            const map = { 1: '文字', 2: '图片', 3: '音频', 4: '视频' };
            return map[type] || '文字';
        };
        const typeClass = (type) => {
            const map = { 1: 'text', 2: 'image', 3: 'audio', 4: 'video' };
            return map[type] || 'text';
        };

        const loadClues = async () => {
            loading.value = true;
            try {
                const res = await api.getClues(parentId.value);
                clues.value = res || [];
            } catch (e) {
                clues.value = [];
            } finally {
                loading.value = false;
            }
        };

        const viewClue = (clue) => {
            if (clue.isUnlocked === 1) {
                router.push('/clue/' + clue.clueId);
            } else {
                currentClue.value = clue;
                unlockPassword.value = '';
                showUnlockDialog.value = true;
            }
        };

        const confirmUnlock = async () => {
            if (!unlockPassword.value) {
                vant.showToast('请输入密码');
                return false;
            }
            try {
                const res = await api.unlockClue(currentClue.value.clueId, unlockPassword.value);
                if (res?.success) {
                    vant.showToast({ message: '解锁成功', icon: 'success' });
                    showUnlockDialog.value = false;
                    loadClues();
                } else {
                    vant.showToast({ message: '密码错误', icon: 'fail' });
                }
            } catch (e) {}
            return false;
        };

        const goBack = () => {
            parentId.value = 0;
            loadClues();
        };

        onMounted(() => {
            loadClues();

            playerWS.on('new_clue', () => {
                loadClues();
            });
        });

        return {
            clues, loading, parentId, showUnlockDialog, unlockPassword, currentClue,
            clueIcon, typeText, typeClass, viewClue, confirmUnlock, goBack
        };
    }
};

const ClueDetailPage = {
    template: `
        <div>
            <div class="page-header" style="padding: 15px 16px;">
                <div style="display: flex; align-items: center; gap: 15px;">
                    <van-icon name="arrow-left" size="24" @click="goBack" />
                    <span style="font-size: 17px; font-weight: bold;">线索详情</span>
                </div>
            </div>

            <div class="page-content">
                <div v-if="loading" style="text-align: center; padding: 60px;">
                    <van-loading color="#1989fa">加载中...</van-loading>
                </div>

                <div v-else-if="clue" class="card">
                    <div style="margin-bottom: 16px;">
                        <span class="clue-type-badge" :class="'type-' + typeClass(clue.type)" style="margin-bottom: 10px;">
                            {{ typeText(clue.type) }}
                        </span>
                        <span v-if="clue.isPuzzle === 1" class="clue-type-badge type-puzzle" style="margin-left: 8px; margin-bottom: 10px;">
                            🧩 拼图解锁
                        </span>
                        <h2 style="font-size: 18px; font-weight: bold; margin-top: 8px;">
                            {{ clue.name }}
                        </h2>
                    </div>

                    <template v-if="clue.isUnlocked === 1">
                        <img v-if="clue.type === 2 && clue.resourceUrl"
                             :src="clue.resourceUrl"
                             class="clue-detail-image"
                             @click="previewImage" />

                        <div v-if="clue.type === 1 || clue.content" class="clue-detail-content">
                            <p v-for="(p, i) in paragraphs" :key="i">{{ p }}</p>
                        </div>

                        <div v-if="clue.type === 3" class="empty-state" style="padding: 30px;">
                            <van-icon name="play-circle-o" size="48" color="#1989fa" />
                            <div class="empty-text">音频线索</div>
                            <div style="font-size: 12px; color: #999; margin-top: 8px;">点击播放</div>
                        </div>

                        <div v-if="clue.type === 4" class="empty-state" style="padding: 30px;">
                            <van-icon name="video-o" size="48" color="#1989fa" />
                            <div class="empty-text">视频线索</div>
                            <div style="font-size: 12px; color: #999; margin-top: 8px;">点击播放</div>
                        </div>

                        <div v-if="clue.hasChildren" style="margin-top: 20px;">
                            <van-button type="primary" block round @click="viewChildren">
                                查看下级线索
                            </van-button>
                        </div>
                    </template>

                    <template v-else-if="clue.isPuzzle === 1 && clue.puzzleStatus === 3">
                        <div class="unlock-section">
                            <div class="unlock-icon">�</div>
                            <div class="unlock-title">线索已销毁</div>
                            <div class="unlock-hint">很遗憾，你没能在规定时间内完成拼图，线索已自动销毁。</div>
                        </div>
                    </template>

                    <template v-else-if="clue.isPuzzle === 1">
                        <div class="puzzle-section">
                            <div v-if="!puzzleStarted" class="unlock-section">
                                <div class="unlock-icon">🧩</div>
                                <div class="unlock-title">拼图解锁</div>
                                <div class="unlock-hint">{{ clue.unlockHint || '完成拼图即可解锁线索' }}</div>
                                <div style="color: #ff6034; font-size: 13px; margin: 12px 0;">
                                    ⏱ 限时 {{ formatTime(clue.puzzleTimeLimit || 180) }}，超时线索将自动销毁
                                </div>
                                <van-button type="primary" block round @click="startPuzzle">
                                    开始拼图
                                </van-button>
                            </div>

                            <div v-else>
                                <div class="puzzle-timer" :class="{ warning: remainTime < 60, danger: remainTime < 30 }">
                                    <van-icon name="clock-o" />
                                    <span style="margin-left: 6px;">剩余时间：{{ formatTime(remainTime) }}</span>
                                </div>

                                <div class="puzzle-board" :style="boardStyle">
                                    <div
                                        v-for="(piece, index) in puzzlePieces"
                                        :key="index"
                                        class="puzzle-piece"
                                        :class="{ empty: piece === emptyValue, movable: isMovable(index) }"
                                        :style="getPieceStyle(piece, index)"
                                        @click="movePiece(index)"
                                    >
                                        <span v-if="piece !== emptyValue" class="piece-number">{{ piece + 1 }}</span>
                                    </div>
                                </div>

                                <div style="text-align: center; color: #999; font-size: 12px; margin-top: 12px;">
                                    点击空白格旁边的方块进行移动
                                </div>

                                <div v-if="clue.resourceUrl" style="margin-top: 16px; text-align: center;">
                                    <div style="font-size: 12px; color: #999; margin-bottom: 6px;">完成后将显示：</div>
                                    <van-image
                                        width="80"
                                        height="80"
                                        :src="clue.resourceUrl"
                                        fit="cover"
                                        style="filter: blur(6px); opacity: 0.5; border-radius: 8px;"
                                    />
                                </div>
                            </div>
                        </div>
                    </template>

                    <template v-else>
                        <div class="unlock-section">
                            <div class="unlock-icon">��</div>
                            <div class="unlock-title">线索已锁定</div>
                            <div class="unlock-hint">{{ clue.unlockHint || '找到密码后即可解锁' }}</div>
                            <van-field
                                v-model="unlockPassword"
                                placeholder="请输入密码"
                                class="unlock-input" />
                            <van-button type="primary" block round @click="unlockClue">
                                解锁线索
                            </van-button>
                        </div>
                    </template>
                </div>
            </div>
        </div>
    `,
    setup() {
        const router = useRouter();
        const route = useRoute();
        const clue = ref(null);
        const loading = ref(true);
        const unlockPassword = ref('');

        const puzzleStarted = ref(false);
        const puzzlePieces = ref([]);
        const remainTime = ref(180);
        const emptyValue = ref(8);
        let timer = null;

        const typeText = (type) => {
            const map = { 1: '文字线索', 2: '图片线索', 3: '音频线索', 4: '视频线索' };
            return map[type] || '文字线索';
        };
        const typeClass = (type) => {
            const map = { 1: 'text', 2: 'image', 3: 'audio', 4: 'video' };
            return map[type] || 'text';
        };

        const paragraphs = computed(() => {
            if (!clue.value?.content) return [];
            return clue.value.content.split('\n').filter(p => p.trim());
        });

        const boardStyle = computed(() => {
            const rows = clue.value?.puzzleRows || 3;
            const cols = clue.value?.puzzleCols || 3;
            emptyValue.value = rows * cols - 1;
            return {
                gridTemplateColumns: `repeat(${cols}, 1fr)`,
                gridTemplateRows: `repeat(${rows}, 1fr)`
            };
        });

        const formatTime = (seconds) => {
            const m = Math.floor(seconds / 60);
            const s = seconds % 60;
            return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
        };

        const getPieceStyle = (piece, index) => {
            if (piece === emptyValue.value) return {};
            const cols = clue.value?.puzzleCols || 3;
            const total = (clue.value?.puzzleRows || 3) * cols;
            const col = piece % cols;
            const row = Math.floor(piece / cols);
            const imgUrl = clue.value?.resourceUrl;
            if (imgUrl) {
                return {
                    backgroundImage: `url(${imgUrl})`,
                    backgroundSize: `${cols * 100}%`,
                    backgroundPosition: `${(col / (cols - 1)) * 100}% ${(row / ((total / cols) - 1)) * 100}%`
                };
            }
            return {};
        };

        const isMovable = (index) => {
            const cols = clue.value?.puzzleCols || 3;
            const rows = clue.value?.puzzleRows || 3;
            const emptyIndex = puzzlePieces.value.indexOf(emptyValue.value);
            const r1 = Math.floor(index / cols);
            const c1 = index % cols;
            const r2 = Math.floor(emptyIndex / cols);
            const c2 = emptyIndex % cols;
            return (Math.abs(r1 - r2) === 1 && c1 === c2) ||
                   (Math.abs(c1 - c2) === 1 && r1 === r2);
        };

        const movePiece = async (index) => {
            if (!isMovable(index)) return;
            try {
                const res = await api.movePuzzlePiece(clue.value.id, index);
                if (res?.puzzle) {
                    puzzlePieces.value = res.puzzle;
                    if (res.isUnlocked === 1) {
                        clearInterval(timer);
                        vant.showToast({ message: '🎉 恭喜完成！线索已解锁', icon: 'success', duration: 2000 });
                        setTimeout(() => {
                            loadDetail();
                        }, 1500);
                    }
                }
            } catch (e) {
                if (e?.message?.includes('时间已到')) {
                    clearInterval(timer);
                    vant.showToast({ message: '时间已到，线索已销毁', icon: 'fail', duration: 2000 });
                    setTimeout(() => {
                        loadDetail();
                    }, 1500);
                }
            }
        };

        const startPuzzle = async () => {
            try {
                const res = await api.startPuzzle(clue.value.id);
                if (res?.puzzle) {
                    puzzlePieces.value = res.puzzle;
                    puzzleStarted.value = true;
                    remainTime.value = clue.value?.puzzleTimeLimit || 180;
                    startTimer();
                }
            } catch (e) {
                vant.showToast('启动拼图失败');
            }
        };

        const startTimer = () => {
            if (timer) clearInterval(timer);
            timer = setInterval(async () => {
                remainTime.value--;
                if (remainTime.value <= 0) {
                    clearInterval(timer);
                    vant.showToast({ message: '时间已到，线索已销毁', icon: 'fail', duration: 2000 });
                    setTimeout(() => {
                        loadDetail();
                    }, 1500);
                }
            }, 1000);
        };

        const loadDetail = async () => {
            loading.value = true;
            try {
                const id = route.params.id;
                const res = await api.getClueDetail(id);
                clue.value = res;
                if (res?.isPuzzle === 1 && res?.puzzleStatus === 1) {
                    try {
                        const state = await api.getPuzzleState(res.id);
                        if (state?.puzzle) {
                            puzzlePieces.value = state.puzzle;
                            puzzleStarted.value = true;
                            if (state.puzzleStartTime) {
                                const start = new Date(state.puzzleStartTime).getTime();
                                const now = Date.now();
                                const elapsed = Math.floor((now - start) / 1000);
                                remainTime.value = Math.max(0, (res.puzzleTimeLimit || 180) - elapsed);
                                if (remainTime.value > 0 && !state.timeout) {
                                    startTimer();
                                }
                            }
                        }
                    } catch (e) {}
                }
            } catch (e) {
            } finally {
                loading.value = false;
            }
        };

        const goBack = () => {
            router.back();
        };

        const unlockClue = async () => {
            if (!unlockPassword.value) {
                vant.showToast('请输入密码');
                return;
            }
            try {
                const id = route.params.id;
                const res = await api.unlockClue(id, unlockPassword.value);
                if (res?.success) {
                    vant.showToast({ message: '解锁成功', icon: 'success' });
                    loadDetail();
                } else {
                    vant.showToast({ message: '密码错误', icon: 'fail' });
                }
            } catch (e) {}
        };

        const viewChildren = () => {
            router.push('/clues?parentId=' + (clue.value?.clueId || clue.value?.id));
        };

        const previewImage = () => {
            if (clue.value?.resourceUrl) {
                vant.ImagePreview([clue.value.resourceUrl]);
            }
        };

        onMounted(loadDetail);

        onUnmounted(() => {
            if (timer) clearInterval(timer);
        });

        return {
            clue, loading, unlockPassword, paragraphs,
            typeText, typeClass, goBack, unlockClue, viewChildren, previewImage,
            puzzleStarted, puzzlePieces, remainTime, emptyValue,
            boardStyle, formatTime, getPieceStyle, isMovable,
            startPuzzle, movePiece
        };
    }
};

const MessagesPage = {
    template: `
        <div>
            <div class="page-header">
                <div class="header-script-name">消息中心</div>
                <div class="header-player-info">
                    <span style="font-size: 12px; opacity: 0.8;">
                        导演推送
                    </span>
                </div>
            </div>

            <div class="page-content">
                <div v-if="loading" style="text-align: center; padding: 40px;">
                    <van-loading color="#1989fa">加载中...</van-loading>
                </div>

                <div v-else-if="messages.length === 0" class="empty-state">
                    <van-icon name="comment-o" size="48" />
                    <div class="empty-text">暂无消息</div>
                    <div style="font-size: 12px; color: #ccc; margin-top: 4px;">有新消息会在这里显示</div>
                </div>

                <div v-else>
                    <div v-for="msg in messages" :key="msg.id"
                         :class="['message-item', msgTypeClass(msg.msgType)]">
                        <div class="message-title">{{ msg.title || '系统消息' }}</div>
                        <div class="message-content">{{ msg.content }}</div>
                        <div class="message-time">{{ formatTime(msg.createTime) }}</div>
                    </div>
                </div>
            </div>
        </div>
    `,
    setup() {
        const messages = ref([]);
        const loading = ref(false);

        const msgTypeClass = (type) => {
            const map = { 1: 'system', 2: '', 3: 'clue' };
            return map[type] || '';
        };

        const formatTime = (time) => {
            if (!time) return '';
            return time.replace('T', ' ').substring(0, 16);
        };

        const loadMessages = async () => {
            loading.value = true;
            try {
                const res = await api.getMessages();
                messages.value = res || [];
            } catch (e) {
                messages.value = [];
            } finally {
                loading.value = false;
            }
        };

        let newMsgHandler = null;
        onMounted(() => {
            loadMessages();
            newMsgHandler = playerWS.on('new_message', () => {
                loadMessages();
            });
        });

        onUnmounted(() => {
            if (newMsgHandler) {
                newMsgHandler();
            }
        });

        return { messages, loading, msgTypeClass, formatTime };
    }
};

const routes = [
    { path: '/join', component: JoinPage, name: 'Join' },
    {
        path: '/',
        component: TabBarLayout,
        children: [
            { path: '', component: HomePage, name: 'Home' },
            { path: 'script', component: ScriptPage, name: 'Script' },
            { path: 'clues', component: CluesPage, name: 'Clues' },
            { path: 'clue/:id', component: ClueDetailPage, name: 'ClueDetail' },
            { path: 'messages', component: MessagesPage, name: 'Messages' }
        ]
    }
];

const router = createRouter({
    history: createWebHashHistory(),
    routes
});

router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('player_token');
    if (to.path === '/join') {
        next();
    } else {
        if (!token) {
            next('/join');
        } else {
            next();
        }
    }
});

const app = createApp({
    template: '<router-view />'
});

app.use(router);
app.use(vant);

app.mount('#app');
