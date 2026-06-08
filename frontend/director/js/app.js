const { createApp, ref, reactive, computed, onMounted, watch, h } = Vue;
const { createRouter, createWebHashHistory, useRouter, useRoute } = VueRouter;
const { ElMessage, ElMessageBox, ElNotification } = ElementPlus;

const LoginPage = {
    template: `
        <div class="login-container">
            <div class="login-box">
                <div class="login-title">🎭 剧本杀导演平台</div>
                <div class="login-subtitle">沉浸式密室剧本杀管控系统</div>
                <el-form :model="form" :rules="rules" ref="formRef" label-width="0">
                    <el-form-item prop="username">
                        <el-input v-model="form.username" placeholder="请输入用户名" size="large" prefix-icon="User">
                            <template #prefix>
                                <el-icon><User /></el-icon>
                            </template>
                        </el-input>
                    </el-form-item>
                    <el-form-item prop="password">
                        <el-input v-model="form.password" type="password" placeholder="请输入密码" size="large" show-password>
                            <template #prefix>
                                <el-icon><Lock /></el-icon>
                            </template>
                        </el-input>
                    </el-form-item>
                    <el-form-item>
                        <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="handleLogin">
                            登 录
                        </el-button>
                    </el-form-item>
                </el-form>
                <div style="text-align: center; color: #999; font-size: 12px;">
                    默认账号：admin / 123456
                </div>
            </div>
        </div>
    `,
    setup() {
        const router = useRouter();
        const formRef = ref(null);
        const loading = ref(false);
        const form = reactive({
            username: 'admin',
            password: '123456'
        });
        const rules = {
            username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
            password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
        };

        const handleLogin = () => {
            formRef.value.validate(async valid => {
                if (valid) {
                    loading.value = true;
                    try {
                        const res = await api.login(form);
                        localStorage.setItem('director_token', res.token);
                        localStorage.setItem('director_info', JSON.stringify({
                            userId: res.userId,
                            username: res.username,
                            nickname: res.nickname,
                            avatar: res.avatar
                        }));
                        ElMessage.success('登录成功');
                        router.push('/');
                    } catch (e) {
                    } finally {
                        loading.value = false;
                    }
                }
            });
        };

        return { form, rules, formRef, loading, handleLogin };
    }
};

const Layout = {
    template: `
        <el-container class="layout-container">
            <el-aside width="220px" style="background: #304156;">
                <div class="sidebar-logo">🎭 剧本杀导演台</div>
                <el-menu
                    :default-active="activeMenu"
                    background-color="#304156"
                    text-color="#bfcbd9"
                    active-text-color="#409EFF"
                    router
                    style="border-right: none;">
                    <el-menu-item index="/">
                        <el-icon><DataAnalysis /></el-icon>
                        <span>控制台概览</span>
                    </el-menu-item>
                    <el-menu-item index="/scripts">
                        <el-icon><Document /></el-icon>
                        <span>剧本管理</span>
                    </el-menu-item>
                    <el-menu-item index="/sessions">
                        <el-icon><VideoPlay /></el-icon>
                        <span>场次管理</span>
                    </el-menu-item>
                </el-menu>
            </el-aside>
            <el-container>
                <el-header class="main-header" height="60px">
                    <span class="header-title">{{ pageTitle }}</span>
                    <div class="header-user">
                        <span>{{ userInfo.nickname || '导演' }}</span>
                        <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
                    </div>
                </el-header>
                <el-main class="main-content">
                    <router-view />
                </el-main>
            </el-container>
        </el-container>
    `,
    setup() {
        const router = useRouter();
        const route = useRoute();
        const userInfo = ref({});

        const activeMenu = computed(() => route.path);

        const pageTitle = computed(() => {
            const map = {
                '/': '控制台概览',
                '/scripts': '剧本管理',
                '/sessions': '场次管理'
            };
            return map[route.path] || '剧本杀管控';
        });

        onMounted(() => {
            const info = localStorage.getItem('director_info');
            if (info) {
                userInfo.value = JSON.parse(info);
            }
        });

        const handleLogout = () => {
            ElMessageBox.confirm('确定要退出登录吗？', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                localStorage.removeItem('director_token');
                localStorage.removeItem('director_info');
                router.push('/login');
                ElMessage.success('已退出登录');
            }).catch(() => {});
        };

        return { activeMenu, pageTitle, userInfo, handleLogout };
    }
};

const Dashboard = {
    template: `
        <div>
            <el-row :gutter="20">
                <el-col :span="6">
                    <div class="stat-card">
                        <div class="stat-number">{{ stats.totalScripts }}</div>
                        <div class="stat-label">剧本总数</div>
                    </div>
                </el-col>
                <el-col :span="6">
                    <div class="stat-card green">
                        <div class="stat-number">{{ stats.playingSessions }}</div>
                        <div class="stat-label">进行中场次</div>
                    </div>
                </el-col>
                <el-col :span="6">
                    <div class="stat-card orange">
                        <div class="stat-number">{{ stats.onlinePlayers }}</div>
                        <div class="stat-label">在线玩家</div>
                    </div>
                </el-col>
                <el-col :span="6">
                    <div class="stat-card blue">
                        <div class="stat-number">{{ stats.todaySessions }}</div>
                        <div class="stat-label">今日场次</div>
                    </div>
                </el-col>
            </el-row>

            <el-row :gutter="20" style="margin-top: 20px;">
                <el-col :span="12">
                    <div class="page-card">
                        <div class="page-title">快速开始</div>
                        <el-form :model="quickForm" label-width="100px">
                            <el-form-item label="选择剧本">
                                <el-select v-model="quickForm.scriptId" placeholder="请选择剧本" style="width: 100%;">
                                    <el-option v-for="s in scriptList" :key="s.id" :label="s.title" :value="s.id" />
                                </el-select>
                            </el-form-item>
                            <el-form-item>
                                <el-button type="primary" size="large" :loading="creating" @click="createSession">
                                    创建游戏场次
                                </el-button>
                            </el-form-item>
                        </el-form>
                    </div>
                </el-col>
                <el-col :span="12">
                    <div class="page-card">
                        <div class="page-title">最近场次</div>
                        <el-table :data="recentSessions" style="width: 100%">
                            <el-table-column prop="sessionCode" label="房间码" width="100">
                                <template #default="{ row }">
                                    <span style="font-family: monospace; font-weight: bold; color: #409eff;">
                                        {{ row.sessionCode }}
                                    </span>
                                </template>
                            </el-table-column>
                            <el-table-column prop="scriptTitle" label="剧本" />
                            <el-table-column prop="status" label="状态" width="80">
                                <template #default="{ row }">
                                    <el-tag :type="statusType(row.status)" size="small">
                                        {{ statusText(row.status) }}
                                    </el-tag>
                                </template>
                            </el-table-column>
                            <el-table-column label="操作" width="80">
                                <template #default="{ row }">
                                    <el-button type="primary" link size="small" @click="goToSession(row.id)">
                                        进入
                                    </el-button>
                                </template>
                            </el-table-column>
                        </el-table>
                        <div v-if="recentSessions.length === 0" class="empty-state">
                            暂无场次
                        </div>
                    </div>
                </el-col>
            </el-row>
        </div>
    `,
    setup() {
        const router = useRouter();
        const stats = reactive({
            totalScripts: 0,
            playingSessions: 0,
            onlinePlayers: 0,
            todaySessions: 0
        });
        const scriptList = ref([]);
        const recentSessions = ref([]);
        const quickForm = reactive({ scriptId: null });
        const creating = ref(false);

        const statusText = (status) => {
            const map = { 0: '待开始', 1: '进行中', 2: '已结束', 3: '已暂停' };
            return map[status] || '未知';
        };

        const statusType = (status) => {
            const map = { 0: 'info', 1: 'success', 2: 'info', 3: 'warning' };
            return map[status] || 'info';
        };

        const loadScripts = async () => {
            try {
                const res = await api.getScriptList(1, 50);
                scriptList.value = res.records || [];
                stats.totalScripts = res.total || 0;
            } catch (e) {}
        };

        const loadSessions = async () => {
            try {
                const res = await api.getSessionList(1, 5);
                recentSessions.value = res.records || [];
                const playing = (res.records || []).filter(s => s.status === 1);
                stats.playingSessions = playing.length;
            } catch (e) {}
        };

        const createSession = async () => {
            if (!quickForm.scriptId) {
                ElMessage.warning('请先选择剧本');
                return;
            }
            creating.value = true;
            try {
                const session = await api.createSession(quickForm.scriptId);
                ElMessage.success('场次创建成功！');
                router.push('/session/' + session.id);
            } catch (e) {
            } finally {
                creating.value = false;
            }
        };

        const goToSession = (id) => {
            router.push('/session/' + id);
        };

        onMounted(() => {
            loadScripts();
            loadSessions();
        });

        return { stats, scriptList, recentSessions, quickForm, creating,
                 statusText, statusType, createSession, goToSession };
    }
};

const ScriptList = {
    template: `
        <div>
            <div class="page-card">
                <div class="toolbar">
                    <div class="page-title" style="margin-bottom: 0; border: none;">剧本管理</div>
                    <div style="display: flex; gap: 10px;">
                        <el-input v-model="keyword" placeholder="搜索剧本名称" class="search-box" clearable @keyup.enter="search">
                            <template #append>
                                <el-button @click="search">搜索</el-button>
                            </template>
                        </el-input>
                        <el-button type="primary" @click="showAddDialog = true">
                            <el-icon><Plus /></el-icon> 新增剧本
                        </el-button>
                    </div>
                </div>

                <el-row :gutter="16">
                    <el-col :span="6" v-for="script in scriptList" :key="script.id">
                        <div class="script-card" @click="viewDetail(script.id)">
                            <img :src="script.coverImage || 'https://via.placeholder.com/300x180'" 
                                 class="script-cover" :alt="script.title" />
                            <div class="script-info">
                                <div class="script-title">{{ script.title }}</div>
                                <div class="script-meta">
                                    <span>{{ script.playerCountMin }}-{{ script.playerCountMax }}人</span>
                                    <el-tag :type="difficultyType(script.difficulty)" size="small">
                                        {{ difficultyText(script.difficulty) }}
                                    </el-tag>
                                </div>
                            </div>
                        </div>
                    </el-col>
                </el-row>

                <div v-if="scriptList.length === 0" class="empty-state">
                    暂无剧本数据
                </div>

                <div style="margin-top: 20px; text-align: center;">
                    <el-pagination
                        v-model:current-page="current"
                        v-model:page-size="size"
                        :total="total"
                        :page-sizes="[8, 16, 24, 40]"
                        layout="total, sizes, prev, pager, next, jumper"
                        @size-change="loadList"
                        @current-change="loadList" />
                </div>
            </div>
        </div>
    `,
    setup() {
        const router = useRouter();
        const scriptList = ref([]);
        const keyword = ref('');
        const current = ref(1);
        const size = ref(8);
        const total = ref(0);
        const showAddDialog = ref(false);

        const difficultyText = (d) => {
            const map = { 1: '简单', 2: '中等', 3: '困难' };
            return map[d] || '未知';
        };
        const difficultyType = (d) => {
            const map = { 1: 'success', 2: 'warning', 3: 'danger' };
            return map[d] || 'info';
        };

        const loadList = async () => {
            try {
                const res = await api.getScriptList(current.value, size.value, keyword.value);
                scriptList.value = res.records || [];
                total.value = res.total || 0;
            } catch (e) {}
        };

        const search = () => {
            current.value = 1;
            loadList();
        };

        const viewDetail = (id) => {
            router.push('/script/' + id);
        };

        onMounted(loadList);

        return { scriptList, keyword, current, size, total, showAddDialog,
                 difficultyText, difficultyType, search, viewDetail };
    }
};

const ScriptDetail = {
    template: `
        <div>
            <el-page-header @back="goBack" content="剧本详情" style="margin-bottom: 20px;">
                <template #content>
                    <span style="font-size: 20px; font-weight: bold;">{{ script.title }}</span>
                </template>
            </el-page-header>

            <el-row :gutter="20">
                <el-col :span="8">
                    <div class="page-card">
                        <img :src="script.coverImage" style="width: 100%; border-radius: 8px;" />
                        <div style="margin-top: 15px;">
                            <h3>{{ script.title }}</h3>
                            <p class="text-muted" style="margin-top: 8px;">{{ script.description }}</p>
                            <div style="margin-top: 15px;">
                                <el-tag :type="difficultyType(script.difficulty)" style="margin-right: 8px;">
                                    {{ difficultyText(script.difficulty) }}
                                </el-tag>
                                <el-tag type="info">{{ script.duration }}分钟</el-tag>
                            </div>
                        </div>
                    </div>
                </el-col>
                <el-col :span="16">
                    <el-tabs v-model="activeTab">
                        <el-tab-pane label="角色列表" name="roles">
                            <el-row :gutter="16">
                                <el-col :span="8" v-for="role in roles" :key="role.id">
                                    <div class="role-card">
                                        <img :src="role.avatar" class="role-avatar" />
                                        <div class="role-name">{{ role.name }}</div>
                                        <div class="role-desc">{{ role.description }}</div>
                                        <el-button type="primary" size="small" style="margin-top: 10px;" 
                                                   @click="viewRole(role)">查看剧本</el-button>
                                    </div>
                                </el-col>
                            </el-row>
                        </el-tab-pane>
                        <el-tab-pane label="线索树" name="clues">
                            <el-tree
                                :data="clueTree"
                                :props="{ label: 'name', children: 'children' }"
                                default-expand-all
                                node-key="id">
                                <template #default="{ node, data }">
                                    <span>
                                        {{ data.name }}
                                        <el-tag size="small" class="clue-type-badge" :type="clueTypeColor(data.type)">
                                            {{ clueTypeText(data.type) }}
                                        </el-tag>
                                        <el-tag v-if="data.isPuzzle === 1" size="small" 
                                                style="margin-left: 4px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #fff; border: none;">
                                            🧩 拼图
                                        </el-tag>
                                        <el-tag v-if="data.isPublic" size="small" type="success" style="margin-left: 4px;">
                                            公开
                                        </el-tag>
                                    </span>
                                </template>
                            </el-tree>
                        </el-tab-pane>
                    </el-tabs>
                </el-col>
            </el-row>
        </div>
    `,
    setup() {
        const router = useRouter();
        const route = useRoute();
        const script = ref({});
        const roles = ref([]);
        const clueTree = ref([]);
        const activeTab = ref('roles');

        const difficultyText = (d) => {
            const map = { 1: '简单', 2: '中等', 3: '困难' };
            return map[d] || '未知';
        };
        const difficultyType = (d) => {
            const map = { 1: 'success', 2: 'warning', 3: 'danger' };
            return map[d] || 'info';
        };
        const clueTypeText = (t) => {
            const map = { 1: '文字', 2: '图片', 3: '音频', 4: '视频' };
            return map[t] || '文字';
        };
        const clueTypeColor = (t) => {
            const map = { 1: 'primary', 2: 'success', 3: 'warning', 4: 'danger' };
            return map[t] || 'info';
        };

        const loadDetail = async () => {
            const id = route.params.id;
            try {
                const res = await api.getScriptDetail(id);
                script.value = res;
                roles.value = res.roles || [];
            } catch (e) {}
            try {
                const res = await api.getClueTree(id);
                clueTree.value = res || [];
            } catch (e) {}
        };

        const goBack = () => {
            router.push('/scripts');
        };

        const viewRole = (role) => {
            ElMessageBox.alert(role.backgroundStory || '暂无剧本内容', role.name + ' - 剧本', {
                confirmButtonText: '关闭',
                dangerouslyUseHTMLString: false
            });
        };

        onMounted(loadDetail);

        return { script, roles, clueTree, activeTab, difficultyText, difficultyType,
                 clueTypeText, clueTypeColor, goBack, viewRole };
    }
};

const SessionList = {
    template: `
        <div>
            <div class="page-card">
                <div class="toolbar">
                    <div class="page-title" style="margin-bottom: 0; border: none;">场次管理</div>
                    <el-button type="primary" @click="showCreateDialog = true">
                        <el-icon><Plus /></el-icon> 创建场次
                    </el-button>
                </div>

                <el-table :data="sessions" style="width: 100%">
                    <el-table-column prop="sessionCode" label="房间码" width="120">
                        <template #default="{ row }">
                            <span style="font-family: monospace; font-size: 18px; font-weight: bold; color: #409eff;">
                                {{ row.sessionCode }}
                            </span>
                        </template>
                    </el-table-column>
                    <el-table-column prop="scriptTitle" label="剧本" />
                    <el-table-column prop="directorName" label="导演" width="100px" />
                    <el-table-column prop="playerCount" label="玩家数" width="80px" />
                    <el-table-column prop="status" label="状态" width="100px">
                        <template #default="{ row }">
                            <el-tag :type="statusType(row.status)" size="small">
                                {{ statusText(row.status) }}
                            </el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column prop="createTime" label="创建时间" width="170px" />
                    <el-table-column label="操作" width="200px" fixed="right">
                        <template #default="{ row }">
                            <el-button type="primary" link size="small" @click="goToControl(row.id)">
                                控制台
                            </el-button>
                            <el-button v-if="row.status === 0" type="success" link size="small" @click="startGame(row.id)">
                                开始
                            </el-button>
                            <el-button v-if="row.status === 1" type="warning" link size="small" @click="pauseGame(row.id)">
                                暂停
                            </el-button>
                            <el-button v-if="row.status === 3" type="success" link size="small" @click="resumeGame(row.id)">
                                恢复
                            </el-button>
                            <el-button v-if="row.status !== 2" type="danger" link size="small" @click="endGame(row.id)">
                                结束
                            </el-button>
                        </template>
                    </el-table-column>
                </el-table>

                <div v-if="sessions.length === 0" class="empty-state">
                    暂无场次
                </div>

                <div style="margin-top: 20px; text-align: center;">
                    <el-pagination
                        v-model:current-page="current"
                        v-model:page-size="size"
                        :total="total"
                        layout="total, prev, pager, next, jumper" />
                </div>
            </div>

            <el-dialog v-model="showCreateDialog" title="创建场次" width="500px">
                <el-form :model="createForm" label-width="80px">
                    <el-form-item label="选择剧本">
                        <el-select v-model="createForm.scriptId" placeholder="请选择剧本" style="width: 100%;">
                            <el-option v-for="s in scriptList" :key="s.id" :label="s.title" :value="s.id" />
                        </el-select>
                    </el-form-item>
                </el-form>
                <template #footer>
                    <el-button @click="showCreateDialog = false">取消</el-button>
                    <el-button type="primary" :loading="creating" @click="doCreate">创建</el-button>
                </template>
            </el-dialog>
        </div>
    `,
    setup() {
        const router = useRouter();
        const sessions = ref([]);
        const current = ref(1);
        const size = ref(10);
        const total = ref(0);
        const showCreateDialog = ref(false);
        const createForm = reactive({ scriptId: null });
        const scriptList = ref([]);
        const creating = ref(false);

        const statusText = (status) => {
            const map = { 0: '待开始', 1: '进行中', 2: '已结束', 3: '已暂停' };
            return map[status] || '未知';
        };
        const statusType = (status) => {
            const map = { 0: 'info', 1: 'success', 2: 'info', 3: 'warning' };
            return map[status] || 'info';
        };

        const loadList = async () => {
            try {
                const res = await api.getSessionList(current.value, size.value);
                sessions.value = res.records || [];
                total.value = res.total || 0;
            } catch (e) {}
        };

        const loadScripts = async () => {
            try {
                const res = await api.getScriptList(1, 50);
                scriptList.value = res.records || [];
            } catch (e) {}
        };

        const goToControl = (id) => {
            router.push('/session/' + id);
        };

        const startGame = async (id) => {
            try {
                await api.startSession(id);
                ElMessage.success('游戏已开始');
                loadList();
            } catch (e) {}
        };

        const pauseGame = async (id) => {
            try {
                await api.pauseSession(id);
                ElMessage.success('游戏已暂停');
                loadList();
            } catch (e) {}
        };

        const resumeGame = async (id) => {
            try {
                await api.resumeSession(id);
                ElMessage.success('游戏已恢复');
                loadList();
            } catch (e) {}
        };

        const endGame = async (id) => {
            try {
                await ElMessageBox.confirm('确定要结束这场游戏吗？', '提示', {
                    type: 'warning'
                });
                await api.endSession(id);
                ElMessage.success('游戏已结束');
                loadList();
            } catch (e) {}
        };

        const doCreate = async () => {
            if (!createForm.scriptId) {
                ElMessage.warning('请选择剧本');
                return;
            }
            creating.value = true;
            try {
                const session = await api.createSession(createForm.scriptId);
                ElMessage.success('创建成功');
                showCreateDialog.value = false;
                router.push('/session/' + session.id);
            } catch (e) {
            } finally {
                creating.value = false;
            }
        };

        onMounted(() => {
            loadList();
            loadScripts();
        });

        return { sessions, current, size, total, showCreateDialog, createForm,
                 scriptList, creating, statusText, statusType, goToControl,
                 startGame, pauseGame, resumeGame, endGame, doCreate };
    }
};

const SessionControl = {
    template: `
        <div>
            <el-page-header @back="goBack" content="场次控制台">
                <template #content>
                    <span style="font-size: 20px; font-weight: bold;">
                        房间码: 
                        <span style="color: #409eff; font-family: monospace; letter-spacing: 3px;">
                            {{ session.sessionCode }}
                        </span>
                    </span>
                    <el-tag :type="statusType(session.status)" size="large" style="margin-left: 15px;">
                        {{ statusText(session.status) }}
                    </el-tag>
                </template>
                <template #extra>
                    <el-button v-if="session.status === 0" type="success" @click="startGame">开始游戏</el-button>
                    <el-button v-if="session.status === 1" type="warning" @click="pauseGame">暂停</el-button>
                    <el-button v-if="session.status === 3" type="success" @click="resumeGame">恢复</el-button>
                    <el-button v-if="session.status !== 2" type="danger" @click="endGame">结束游戏</el-button>
                </template>
            </el-page-header>

            <el-row :gutter="20" style="margin-top: 20px;">
                <el-col :span="16">
                    <el-tabs v-model="activeTab">
                        <el-tab-pane label="玩家管理" name="players">
                            <div class="page-card">
                                <div class="control-title">
                                    当前玩家 ({{ players.length }}人)
                                    <el-button type="primary" size="small" style="float: right;" 
                                               :disabled="!hasAvailableRole" @click="showAssignRoleDialog">
                                        分配角色
                                    </el-button>
                                </div>
                                <div v-for="player in players" :key="player.id" class="player-item">
                                    <div class="player-avatar">{{ player.nickname?.charAt(0) || '玩' }}</div>
                                    <div class="player-info">
                                        <div class="player-name">
                                            <span :class="player.isOnline ? 'online-dot' : 'offline-dot'"></span>
                                            {{ player.nickname }}
                                        </div>
                                        <div class="player-role">
                                            角色: {{ getRoleName(player.roleId) || '未分配' }}
                                        </div>
                                    </div>
                                    <div>
                                        <el-button type="primary" link size="small" @click="assignRoleToPlayer(player)">
                                            分配角色
                                        </el-button>
                                    </div>
                                </div>
                                <div v-if="players.length === 0" class="empty-state">
                                    暂无玩家加入
                                    <div style="font-size: 12px; margin-top: 8px;">
                                        让玩家输入房间码加入游戏: <b>{{ session.sessionCode }}</b>
                                    </div>
                                </div>
                            </div>
                        </el-tab-pane>

                        <el-tab-pane label="线索分发" name="clues">
                            <div class="page-card">
                                <div class="control-title">
                                    选择线索分发
                                </div>
                                <el-tree
                                    :data="clueTree"
                                    :props="{ label: 'name', children: 'children' }"
                                    show-checkbox
                                    node-key="id"
                                    default-expand-all
                                    ref="clueTreeRef">
                                    <template #default="{ data }">
                                        <span>
                                            {{ data.name }}
                                            <el-tag size="small" style="margin-left: 8px;" :type="clueTypeColor(data.type)">
                                                {{ clueTypeText(data.type) }}
                                            </el-tag>
                                            <el-tag v-if="data.isPuzzle === 1" size="small" 
                                                    style="margin-left: 4px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #fff; border: none;">
                                                🧩 拼图
                                            </el-tag>
                                        </span>
                                    </template>
                                </el-tree>
                                <div style="margin-top: 20px;">
                                    <div style="margin-bottom: 10px; font-weight: bold;">选择接收玩家:</div>
                                    <el-checkbox-group v-model="selectedPlayers">
                                        <el-checkbox v-for="p in players" :key="p.id" :label="p.id">
                                            {{ p.nickname }}
                                        </el-checkbox>
                                    </el-checkbox-group>
                                </div>
                                <div style="margin-top: 20px;">
                                    <el-checkbox v-model="sendNotification">发送消息通知</el-checkbox>
                                </div>
                                <el-button type="primary" style="margin-top: 20px; width: 100%;" 
                                           :disabled="!canDistribute" :loading="distributing" @click="distributeClues">
                                    分发选中线索
                                </el-button>
                            </div>
                        </el-tab-pane>

                        <el-tab-pane label="消息推送" name="messages">
                            <div class="page-card">
                                <div class="control-title">发送消息</div>
                                <el-form label-width="80px">
                                    <el-form-item label="接收人">
                                        <el-radio-group v-model="msgForm.receiverType">
                                            <el-radio :label="1">全部玩家</el-radio>
                                            <el-radio :label="2">指定玩家</el-radio>
                                        </el-radio-group>
                                    </el-form-item>
                                    <el-form-item v-if="msgForm.receiverType === 2" label="选择玩家">
                                        <el-checkbox-group v-model="msgForm.receiverIds">
                                            <el-checkbox v-for="p in players" :key="p.id" :label="p.id">
                                                {{ p.nickname }}
                                            </el-checkbox>
                                        </el-checkbox-group>
                                    </el-form-item>
                                    <el-form-item label="消息类型">
                                        <el-select v-model="msgForm.msgType" style="width: 200px;">
                                            <el-option :label="系统消息" :value="1" />
                                            <el-option :label="剧情提示" :value="2" />
                                            <el-option :label="线索通知" :value="3" />
                                        </el-select>
                                    </el-form-item>
                                    <el-form-item label="标题">
                                        <el-input v-model="msgForm.title" placeholder="消息标题" />
                                    </el-form-item>
                                    <el-form-item label="内容">
                                        <el-input v-model="msgForm.content" type="textarea" :rows="4" placeholder="消息内容" />
                                    </el-form-item>
                                    <el-form-item>
                                        <el-button type="primary" :loading="sendingMsg" @click="sendMessage">
                                            发送消息
                                        </el-button>
                                    </el-form-item>
                                </el-form>

                                <div class="control-title" style="margin-top: 20px;">消息记录</div>
                                <div class="message-list">
                                    <div v-for="msg in messages" :key="msg.id" 
                                         :class="['message-item', msgClass(msg.msgType)]">
                                        <div class="message-title">{{ msg.title }}</div>
                                        <div>{{ msg.content }}</div>
                                        <div class="message-time">{{ msg.createTime }}</div>
                                    </div>
                                    <div v-if="messages.length === 0" class="empty-state" style="padding: 30px;">
                                        暂无消息
                                    </div>
                                </div>
                            </div>
                        </el-tab-pane>
                    </el-tabs>
                </el-col>

                <el-col :span="8">
                    <div class="page-card" style="margin-bottom: 20px;">
                        <div class="control-title">剧本信息</div>
                        <h3>{{ session.scriptTitle }}</h3>
                        <p class="text-muted" style="margin-top: 8px;">导演: {{ session.directorName }}</p>
                    </div>
                    <div class="page-card">
                        <div class="control-title">角色列表</div>
                        <div v-for="role in roles" :key="role.id" style="padding: 8px 0; border-bottom: 1px solid #f0f0f0;">
                            <div style="display: flex; align-items: center;">
                                <img :src="role.avatar" style="width: 32px; height: 32px; border-radius: 50%; margin-right: 10px;" />
                                <div>
                                    <div style="font-weight: bold;">{{ role.name }}</div>
                                    <div style="font-size: 12px; color: #999;">
                                        {{ getRolePlayerCount(role.id) }}人已选
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </el-col>
            </el-row>
        </div>
    `,
    setup() {
        const router = useRouter();
        const route = useRoute();
        const session = ref({});
        const players = ref([]);
        const roles = ref([]);
        const clueTree = ref([]);
        const clueTreeRef = ref(null);
        const messages = ref([]);
        const activeTab = ref('players');
        const selectedPlayers = ref([]);
        const sendNotification = ref(true);
        const distributing = ref(false);
        const sendingMsg = ref(false);
        const msgForm = reactive({
            receiverType: 1,
            receiverIds: [],
            msgType: 2,
            title: '',
            content: ''
        });

        const statusText = (status) => {
            const map = { 0: '待开始', 1: '进行中', 2: '已结束', 3: '已暂停' };
            return map[status] || '未知';
        };
        const statusType = (status) => {
            const map = { 0: 'info', 1: 'success', 2: 'info', 3: 'warning' };
            return map[status] || 'info';
        };
        const clueTypeText = (t) => {
            const map = { 1: '文字', 2: '图片', 3: '音频', 4: '视频' };
            return map[t] || '文字';
        };
        const clueTypeColor = (t) => {
            const map = { 1: 'primary', 2: 'success', 3: 'warning', 4: 'danger' };
            return map[t] || 'info';
        };
        const msgClass = (t) => {
            const map = { 1: 'system', 2: '', 3: 'clue' };
            return map[t] || '';
        };

        const hasAvailableRole = computed(() => roles.value.length > 0);
        const canDistribute = computed(() => {
            const checked = clueTreeRef.value?.getCheckedKeys?.() || [];
            return checked.length > 0 && selectedPlayers.value.length > 0;
        });

        const getRoleName = (roleId) => {
            if (!roleId) return '';
            const role = roles.value.find(r => r.id === roleId);
            return role ? role.name : '';
        };

        const getRolePlayerCount = (roleId) => {
            return players.value.filter(p => p.roleId === roleId).length;
        };

        const loadSession = async () => {
            const id = route.params.id;
            try {
                const res = await api.getSessionDetail(id);
                session.value = res;
                if (res.scriptId) {
                    loadRoles(res.scriptId);
                    loadClueTree(res.scriptId);
                }
            } catch (e) {}
        };

        const loadPlayers = async () => {
            const id = route.params.id;
            try {
                const res = await api.getSessionPlayers(id);
                players.value = res || [];
            } catch (e) {}
        };

        const loadRoles = async (scriptId) => {
            try {
                const res = await api.getScriptRoles(scriptId);
                roles.value = res || [];
            } catch (e) {}
        };

        const loadClueTree = async (scriptId) => {
            try {
                const res = await api.getClueTree(scriptId);
                clueTree.value = res || [];
            } catch (e) {}
        };

        const loadMessages = async () => {
            const id = route.params.id;
            try {
                const res = await api.getSessionMessages(id);
                messages.value = res || [];
            } catch (e) {}
        };

        const goBack = () => {
            router.push('/sessions');
        };

        const startGame = async () => {
            try {
                await api.startSession(session.value.id);
                ElMessage.success('游戏已开始');
                loadSession();
            } catch (e) {}
        };

        const pauseGame = async () => {
            try {
                await api.pauseSession(session.value.id);
                ElMessage.success('游戏已暂停');
                loadSession();
            } catch (e) {}
        };

        const resumeGame = async () => {
            try {
                await api.resumeSession(session.value.id);
                ElMessage.success('游戏已恢复');
                loadSession();
            } catch (e) {}
        };

        const endGame = async () => {
            try {
                await ElMessageBox.confirm('确定要结束这场游戏吗？', '提示', { type: 'warning' });
                await api.endSession(session.value.id);
                ElMessage.success('游戏已结束');
                loadSession();
            } catch (e) {}
        };

        const assignRoleToPlayer = (player) => {
            const options = roles.value.map(r => ({
                value: r.id,
                label: r.name
            }));
            ElMessageBox.prompt('选择角色:', '为 ' + player.nickname + ' 分配角色', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                inputType: 'select',
                inputValue: player.roleId || '',
                inputOptions: options,
                inputValidator: (val) => {
                    if (!val) return '请选择角色';
                    return true;
                }
            }).then(async ({ value: roleId }) => {
                try {
                    await api.assignRole(session.value.id, player.id, roleId);
                    ElMessage.success('角色分配成功');
                    loadPlayers();
                } catch (e) {}
            }).catch(() => {});
        };

        const showAssignRoleDialog = () => {};

        const distributeClues = async () => {
            const clueIds = clueTreeRef.value?.getCheckedKeys?.() || [];
            if (clueIds.length === 0) {
                ElMessage.warning('请选择要分发的线索');
                return;
            }
            if (selectedPlayers.value.length === 0) {
                ElMessage.warning('请选择接收玩家');
                return;
            }
            distributing.value = true;
            try {
                for (const clueId of clueIds) {
                    await api.distributeClue({
                        sessionId: session.value.id,
                        clueId: clueId,
                        playerIds: selectedPlayers.value,
                        sendNotification: sendNotification.value
                    });
                }
                ElMessage.success('线索分发成功');
            } catch (e) {
            } finally {
                distributing.value = false;
            }
        };

        const sendMessage = async () => {
            if (!msgForm.title || !msgForm.content) {
                ElMessage.warning('请填写标题和内容');
                return;
            }
            if (msgForm.receiverType === 2 && msgForm.receiverIds.length === 0) {
                ElMessage.warning('请选择接收玩家');
                return;
            }
            sendingMsg.value = true;
            try {
                await api.sendMessage({
                    sessionId: session.value.id,
                    receiverType: msgForm.receiverType,
                    receiverIds: msgForm.receiverType === 1 ? [] : msgForm.receiverIds,
                    msgType: msgForm.msgType,
                    title: msgForm.title,
                    content: msgForm.content
                });
                ElMessage.success('消息发送成功');
                msgForm.title = '';
                msgForm.content = '';
                loadMessages();
            } catch (e) {
            } finally {
                sendingMsg.value = false;
            }
        };

        let refreshTimer = null;
        const wsConnected = ref(false);
        const wsReconnectCount = ref(0);

        const initWebSocket = async () => {
            const sessionId = route.params.id;
            if (!sessionId) return;

            try {
                await directorWS.connect(sessionId);
                wsConnected.value = true;
                wsReconnectCount.value = 0;
                console.log('[场次控制台] WebSocket连接成功');

                directorWS.on('player_status_change', (data) => {
                    console.log('[场次控制台] 玩家状态变化:', data);
                    loadPlayers();
                });

                directorWS.on('new_message', (data) => {
                    console.log('[场次控制台] 新消息:', data);
                    loadMessages();
                });

                directorWS.on('clue_unlocked', (data) => {
                    console.log('[场次控制台] 线索已解锁:', data);
                    ElNotification({
                        title: '线索解锁',
                        message: `玩家解锁了一条线索`,
                        type: 'success',
                        duration: 3000
                    });
                });

                directorWS.on('game_status_change', (data) => {
                    console.log('[场次控制台] 游戏状态变化:', data);
                    loadSession();
                });

                directorWS.on('role_assigned', (data) => {
                    console.log('[场次控制台] 角色分配:', data);
                    loadPlayers();
                });

                directorWS.on('connected', () => {
                    wsConnected.value = true;
                });

            } catch (e) {
                console.error('[场次控制台] WebSocket连接失败:', e);
                wsConnected.value = false;
            }
        };

        onMounted(() => {
            loadSession();
            loadPlayers();
            loadMessages();
            initWebSocket();
        });

        watch(route, () => {
            if (route.name === 'SessionControl') {
                loadSession();
                loadPlayers();
                loadMessages();
                initWebSocket();
            }
        });

        const { onUnmounted } = Vue;
        onUnmounted(() => {
            if (refreshTimer) {
                clearInterval(refreshTimer);
            }
            directorWS.close();
        });

        return {
            session, players, roles, clueTree, clueTreeRef, messages, activeTab,
            selectedPlayers, sendNotification, distributing, sendingMsg, msgForm,
            wsConnected,
            statusText, statusType, clueTypeText, clueTypeColor, msgClass,
            hasAvailableRole, canDistribute, getRoleName, getRolePlayerCount,
            goBack, startGame, pauseGame, resumeGame, endGame,
            assignRoleToPlayer, showAssignRoleDialog, distributeClues, sendMessage
        };
    }
};

const routes = [
    { path: '/login', component: LoginPage, name: 'Login' },
    {
        path: '/',
        component: Layout,
        children: [
            { path: '', component: Dashboard, name: 'Dashboard' },
            { path: 'scripts', component: ScriptList, name: 'ScriptList' },
            { path: 'script/:id', component: ScriptDetail, name: 'ScriptDetail' },
            { path: 'sessions', component: SessionList, name: 'SessionList' },
            { path: 'session/:id', component: SessionControl, name: 'SessionControl' }
        ]
    }
];

const router = createRouter({
    history: createWebHashHistory(),
    routes
});

router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('director_token');
    if (to.path === '/login') {
        next();
    } else {
        if (!token) {
            next('/login');
        } else {
            next();
        }
    }
});

const app = createApp({
    template: '<router-view />'
});

app.use(router);
app.use(ElementPlus);

for (const [key, comp] of Object.entries(ElementPlusIconsVue || {})) {
    app.component(key, comp);
}

app.mount('#app');
