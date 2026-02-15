// 远程托管管理 JavaScript

// 托管实例表格实例（使用 var 避免重复声明错误，并暴露到全局作用域）
var hostingTable = null;
// 技能选择器实例
var hostingSkillSelector = null;

// 页面加载时初始化
function initRemoteHostingPage() {
    console.log('[RemoteHosting] 初始化远程托管页面');

    // 初始化菜单
    if (typeof initMenu === 'function') {
        initMenu('remote-hosting');
    }

    // 初始化表格
    initHostingTable();

    // 初始化技能选择器
    initSkillSelector();

    // 绑定表单提交事件
    initHostingForm();

    // 绑定搜索事件
    initSearchEvent();
}

// 初始化托管实例表格
function initHostingTable() {
    console.log('[RemoteHosting] 开始初始化托管实例表格');

    if (!utils || !utils.DataTable) {
        console.error('[RemoteHosting] DataTable 组件未加载, utils:', utils);
        return;
    }

    console.log('[RemoteHosting] DataTable 组件已加载');

    const tableBody = document.getElementById('hostingTableBody');
    if (!tableBody) {
        console.error('[RemoteHosting] 找不到表格体元素 hostingTableBody');
        return;
    }

    console.log('[RemoteHosting] 找到表格体元素');

    hostingTable = utils.DataTable({
        tableId: 'hostingTableBody',
        apiUrl: '/api/hosting/instances',
        pageName: 'RemoteHosting',
        columns: [
            { field: 'id', title: '实例ID', width: '120px' },
            { field: 'name', title: '实例名称', width: '150px' },
            { field: 'skillId', title: '关联技能', width: '150px' },
            {
                field: 'status',
                title: '状态',
                width: '100px',
                align: 'center',
                formatter: (value) => `<span class="hosting-status status-${value || 'unknown'}">${getStatusText(value)}</span>`
            },
            {
                field: 'createdAt',
                title: '创建时间',
                width: '180px',
                formatter: (value) => value ? utils.date.format(value) : '-'
            },
            { 
                field: 'healthStatus', 
                title: '健康状态', 
                width: '100px', 
                align: 'center',
                formatter: (value) => `<span class="health-status status-${value || 'unknown'}">${getHealthText(value)}</span>`
            },
            {
                field: 'actions',
                title: '操作',
                width: '300px',
                align: 'center',
                formatter: (value, row) => `
                    <div class="action-buttons">
                        <button class="btn btn-success" onclick="startHosting('${row.id}')" ${row.status === 'running' ? 'disabled' : ''}>
                            <i class="ri-play-line"></i> 启动
                        </button>
                        <button class="btn btn-danger" onclick="stopHosting('${row.id}')" ${row.status === 'stopped' ? 'disabled' : ''}>
                            <i class="ri-stop-line"></i> 停止
                        </button>
                        <button class="btn btn-secondary" onclick="editHosting('${row.id}')">
                            <i class="ri-edit-line"></i> 编辑
                        </button>
                        <button class="btn btn-danger" onclick="deleteHosting('${row.id}')">
                            <i class="ri-delete-line"></i> 删除
                        </button>
                    </div>
                `
            }
        ],
        onLoad: (data) => {
            console.log('[RemoteHosting] 表格数据加载完成，共', data.length, '条记录');
        },
        onError: (error) => {
            console.error('[RemoteHosting] 表格数据加载失败:', error);
        }
    });

    if (hostingTable) {
        console.log('[RemoteHosting] 托管实例表格初始化完成');
    } else {
        console.error('[RemoteHosting] 托管实例表格初始化失败，返回null');
    }
}

// 初始化技能选择器
function initSkillSelector() {
    if (utils && utils.SkillSelector) {
        hostingSkillSelector = utils.SkillSelector({
            selectId: 'skillId',
            emptyText: '请选择技能',
            pageName: 'RemoteHosting',
            onLoad: (skills) => {
                console.log('[RemoteHosting] 技能选择器加载完成，共', skills.length, '个技能');
            },
            onError: (error) => {
                console.error('[RemoteHosting] 技能选择器加载失败:', error);
            }
        });
    } else {
        console.warn('[RemoteHosting] SkillSelector 组件未加载');
    }
}

// 初始化表单提交事件
function initHostingForm() {
    console.log('[RemoteHosting] 表单事件初始化完成（使用 onclick 方式）');
}

// 初始化搜索事件
function initSearchEvent() {
    console.log('[RemoteHosting] 初始化搜索事件');

    // 绑定查询按钮点击事件
    const btnSearch = document.getElementById('btnSearchHosting');
    if (btnSearch) {
        btnSearch.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            console.log('[RemoteHosting] 查询按钮点击事件触发');
            searchHosting();
        });
    }

    // 绑定重置按钮点击事件
    const btnReset = document.getElementById('btnResetFilters');
    if (btnReset) {
        btnReset.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            console.log('[RemoteHosting] 重置按钮点击事件触发');
            resetFilters();
        });
    }

    // 为关键词输入框添加回车键支持
    const hostingSearch = document.getElementById('hostingSearch');
    if (hostingSearch) {
        hostingSearch.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                searchHosting();
            }
        });
    }
}

// 查询托管实例（点击查询按钮时调用）
window.searchHosting = function() {
    console.log('[RemoteHosting] 查询按钮被点击');
    console.log('[RemoteHosting] hostingTable 状态:', hostingTable);

    const keyword = document.getElementById('hostingSearch')?.value?.trim() || '';
    console.log('[RemoteHosting] 筛选值:', { keyword });

    if (hostingTable && typeof hostingTable.load === 'function') {
        const params = {};
        if (keyword) params.keyword = keyword;
        console.log('[RemoteHosting] 查询参数:', params);
        hostingTable.load(params);
    } else {
        console.error('[RemoteHosting] hostingTable 未初始化或 load 方法不可用');
        utils.msg.error('表格未初始化，请刷新页面重试');
    }
};

// 重置筛选条件
window.resetFilters = function() {
    console.log('[RemoteHosting] 重置筛选条件');

    const keywordInput = document.getElementById('hostingSearch');
    if (keywordInput) keywordInput.value = '';

    // 重新加载全部数据
    if (hostingTable) {
        hostingTable.refresh();
    }
};

// 获取状态文本
function getStatusText(status) {
    switch (status) {
        case 'running': return '运行中';
        case 'stopped': return '已停止';
        case 'pending': return '待处理';
        case 'error': return '错误';
        default: return status || '未知';
    }
}

// 获取健康状态文本
function getHealthText(health) {
    switch (health) {
        case 'healthy': return '健康';
        case 'unhealthy': return '不健康';
        case 'unknown': return '未知';
        default: return health || '未知';
    }
}

// 打开添加托管模态框
window.openAddHostingModal = function() {
    console.log('[RemoteHosting] 打开添加托管模态框');
    const modalTitle = document.getElementById('modalTitle');
    const hostingId = document.getElementById('hostingId');
    const hostingName = document.getElementById('hostingName');
    const skillId = document.getElementById('skillId');
    const hostingDescription = document.getElementById('hostingDescription');
    const hostingStatus = document.getElementById('hostingStatus');
    const hostingModal = document.getElementById('hostingModal');

    if (modalTitle) modalTitle.textContent = '添加托管实例';
    if (hostingId) hostingId.value = '';
    if (hostingName) hostingName.value = '';
    if (skillId) skillId.value = '';
    if (hostingDescription) hostingDescription.value = '';
    if (hostingStatus) hostingStatus.value = 'stopped';
    if (hostingModal) hostingModal.style.display = 'block';
};

// 打开编辑托管模态框
window.editHosting = async function(instanceId) {
    console.log('[RemoteHosting] 编辑托管实例:', instanceId);

    try {
        const data = await utils.api.post('/api/hosting/instances/get', { instanceId: instanceId });
        console.log('[RemoteHosting] 获取托管实例详情响应:', data);

        if (data.code === 200 && data.data) {
            const instance = data.data;

            const modalTitle = document.getElementById('modalTitle');
            const hostingIdInput = document.getElementById('hostingId');
            const hostingName = document.getElementById('hostingName');
            const skillId = document.getElementById('skillId');
            const hostingDescription = document.getElementById('hostingDescription');
            const hostingStatus = document.getElementById('hostingStatus');
            const hostingModal = document.getElementById('hostingModal');

            if (modalTitle) modalTitle.textContent = '编辑托管实例';
            if (hostingIdInput) hostingIdInput.value = instance.id || '';
            if (hostingName) hostingName.value = instance.name || '';
            if (skillId) skillId.value = instance.skillId || '';
            if (hostingDescription) hostingDescription.value = instance.description || '';
            if (hostingStatus) hostingStatus.value = instance.status || 'stopped';
            if (hostingModal) hostingModal.style.display = 'block';
        } else {
            utils.msg.error('获取托管实例详情失败');
        }
    } catch (error) {
        console.error('[RemoteHosting] 获取托管实例详情错误:', error);
        utils.msg.error('获取托管实例详情失败');
    }
};

// 关闭托管模态框
window.closeHostingModal = function() {
    const hostingModal = document.getElementById('hostingModal');
    if (hostingModal) hostingModal.style.display = 'none';
};

// 启动托管实例
window.startHosting = async function(instanceId) {
    console.log('[RemoteHosting] 启动托管实例:', instanceId);
    try {
        const result = await utils.api.post('/api/hosting/instances/start', { instanceId: instanceId });
        console.log('[RemoteHosting] 启动托管实例响应:', result);

        if (result.code === 200 && result.data) {
            if (hostingTable) hostingTable.refresh();
            utils.msg.success('托管实例启动成功！');
        } else {
            utils.msg.error('启动失败: ' + (result.message || '未知错误'));
        }
    } catch (error) {
        console.error('[RemoteHosting] 启动托管实例错误:', error);
        utils.msg.error('启动失败');
    }
};

// 停止托管实例
window.stopHosting = async function(instanceId) {
    console.log('[RemoteHosting] 停止托管实例:', instanceId);
    try {
        const result = await utils.api.post('/api/hosting/instances/stop', { instanceId: instanceId });
        console.log('[RemoteHosting] 停止托管实例响应:', result);

        if (result.code === 200 && result.data) {
            if (hostingTable) hostingTable.refresh();
            utils.msg.success('托管实例停止成功！');
        } else {
            utils.msg.error('停止失败: ' + (result.message || '未知错误'));
        }
    } catch (error) {
        console.error('[RemoteHosting] 停止托管实例错误:', error);
        utils.msg.error('停止失败');
    }
};

// 重启托管实例
window.restartHosting = async function(instanceId) {
    console.log('[RemoteHosting] 重启托管实例:', instanceId);
    try {
        const result = await utils.api.post('/api/hosting/instances/restart', { instanceId: instanceId });
        if (result.code === 200 && result.data) {
            if (hostingTable) hostingTable.refresh();
            utils.msg.success('托管实例重启成功！');
        } else {
            utils.msg.error('重启失败: ' + (result.message || '未知错误'));
        }
    } catch (error) {
        console.error('[RemoteHosting] 重启托管实例错误:', error);
        utils.msg.error('重启失败');
    }
};

// 扩缩容托管实例
window.scaleHosting = async function(instanceId, replicas) {
    console.log('[RemoteHosting] 扩缩容托管实例:', instanceId, '副本数:', replicas);
    try {
        const result = await utils.api.post('/api/hosting/instances/scale', { 
            instanceId: instanceId, 
            replicas: replicas 
        });
        if (result.code === 200 && result.data) {
            if (hostingTable) hostingTable.refresh();
            utils.msg.success('扩缩容成功！当前副本数: ' + replicas);
        } else {
            utils.msg.error('扩缩容失败: ' + (result.message || '未知错误'));
        }
    } catch (error) {
        console.error('[RemoteHosting] 扩缩容错误:', error);
        utils.msg.error('扩缩容失败');
    }
};

// 获取实例健康状态
window.getHostingHealth = async function(instanceId) {
    console.log('[RemoteHosting] 获取实例健康状态:', instanceId);
    try {
        const result = await utils.api.post('/api/hosting/instances/health', { instanceId: instanceId });
        if (result.code === 200) {
            const health = result.data;
            utils.msg.info('实例健康状态: ' + (health || '未知'));
            return health;
        } else {
            utils.msg.error('获取健康状态失败');
            return null;
        }
    } catch (error) {
        console.error('[RemoteHosting] 获取健康状态错误:', error);
        utils.msg.error('获取健康状态失败');
        return null;
    }
};

// 获取实例状态
window.getHostingStatus = async function(instanceId) {
    console.log('[RemoteHosting] 获取实例状态:', instanceId);
    try {
        const result = await utils.api.post('/api/hosting/instances/status', { instanceId: instanceId });
        if (result.code === 200) {
            const status = result.data;
            utils.msg.info('实例状态: ' + (status || '未知'));
            return status;
        } else {
            utils.msg.error('获取状态失败');
            return null;
        }
    } catch (error) {
        console.error('[RemoteHosting] 获取状态错误:', error);
        utils.msg.error('获取状态失败');
        return null;
    }
};

// 更新资源配置
window.updateHostingResources = async function(instanceId, cpuLimit, memoryLimit) {
    console.log('[RemoteHosting] 更新资源配置:', instanceId, 'CPU:', cpuLimit, '内存:', memoryLimit);
    try {
        const result = await utils.api.post('/api/hosting/instances/resources', { 
            instanceId: instanceId, 
            cpuLimit: cpuLimit, 
            memoryLimit: memoryLimit 
        });
        if (result.code === 200 && result.data) {
            if (hostingTable) hostingTable.refresh();
            utils.msg.success('资源配置更新成功！');
        } else {
            utils.msg.error('更新资源配置失败: ' + (result.message || '未知错误'));
        }
    } catch (error) {
        console.error('[RemoteHosting] 更新资源配置错误:', error);
        utils.msg.error('更新资源配置失败');
    }
};

// 获取托管统计
window.getHostingStats = async function() {
    console.log('[RemoteHosting] 获取托管统计');
    try {
        const result = await utils.api.post('/api/hosting/stats', {});
        if (result.code === 200 && result.data) {
            return result.data;
        }
        return null;
    } catch (error) {
        console.error('[RemoteHosting] 获取托管统计错误:', error);
        return null;
    }
};

// 按技能查询托管实例
window.getHostingBySkill = async function(skillId) {
    console.log('[RemoteHosting] 按技能查询托管实例:', skillId);
    try {
        const result = await utils.api.post('/api/hosting/instances/by-skill', { skillId: skillId });
        if (result.code === 200 && result.data) {
            return result.data;
        }
        return [];
    } catch (error) {
        console.error('[RemoteHosting] 按技能查询托管实例错误:', error);
        return [];
    }
};

// 删除托管实例
window.deleteHosting = async function(instanceId) {
    if (!utils.msg.confirm('确定要删除这个托管实例吗？')) {
        return;
    }

    console.log('[RemoteHosting] 删除托管实例:', instanceId);
    try {
        const result = await utils.api.post('/api/hosting/instances/delete', { instanceId: instanceId });
        console.log('[RemoteHosting] 删除托管实例响应:', result);

        if (result.code === 200 && result.data) {
            if (hostingTable) hostingTable.refresh();
            utils.msg.success('托管实例删除成功！');
        } else {
            utils.msg.error('删除失败: ' + (result.message || '未知错误'));
        }
    } catch (error) {
        console.error('[RemoteHosting] 删除托管实例错误:', error);
        utils.msg.error('删除失败');
    }
};

// 提交托管表单
window.submitHostingForm = async function() {
    console.log('[RemoteHosting] 提交托管表单');

    const instanceId = document.getElementById('hostingId').value;
    const hostingName = document.getElementById('hostingName').value;
    const skillId = document.getElementById('skillId').value;
    const hostingDescription = document.getElementById('hostingDescription').value;
    const hostingStatus = document.getElementById('hostingStatus').value;

    if (!hostingName || hostingName.trim().length < 2) {
        utils.msg.error('托管实例名称至少2个字符');
        return;
    }
    if (!skillId) {
        utils.msg.error('请选择关联技能');
        return;
    }

    const hostingData = {
        name: hostingName.trim(),
        skillId: skillId,
        description: hostingDescription.trim(),
        status: hostingStatus
    };

    const submitBtn = document.querySelector('#hostingModal .btn-primary');
    const originalText = submitBtn ? submitBtn.textContent : '保存';
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="ri-loader-4-line ri-spin"></i> 保存中...';
    }

    try {
        let result;
        if (instanceId) {
            console.log('[RemoteHosting] 更新托管实例:', instanceId);
            result = await utils.api.post('/api/hosting/instances/update', { 
                instanceId: instanceId, 
                instance: hostingData 
            });
        } else {
            console.log('[RemoteHosting] 创建托管实例');
            result = await utils.api.post('/api/hosting/instances/create', hostingData);
        }

        console.log('[RemoteHosting] 保存托管实例响应:', result);

        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.textContent = originalText;
        }

        if (result.code === 200) {
            if (hostingTable) hostingTable.refresh();
            closeHostingModal();
            utils.msg.success('托管实例保存成功！');
        } else {
            utils.msg.error('保存失败: ' + (result.message || '未知错误'));
        }
    } catch (error) {
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.textContent = originalText;
        }
        console.error('[RemoteHosting] 保存托管实例错误:', error);
        utils.msg.error('保存失败');
    }
};

// 页面加载时初始化
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', function () {
        console.log('[RemoteHosting] DOM加载完成（通过事件）');
        initRemoteHostingPage();
    });
} else {
    console.log('[RemoteHosting] DOM已加载，直接初始化');
    initRemoteHostingPage();
}
