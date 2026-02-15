// 技能发现/安装 JavaScript

var discoveryTable = null;

function initInstallPage() {
    console.log('[Install] 初始化技能安装页面');

    if (typeof initMenu === 'function') {
        initMenu('skill-install');
    }

    initDiscoveryTable();
    initSearchEvent();
}

function initDiscoveryTable() {
    console.log('[Install] 开始初始化技能发现表格');

    if (!utils || !utils.DataTable) {
        console.error('[Install] DataTable 组件未加载');
        return;
    }

    const tableBody = document.getElementById('discoveryTableBody');
    if (!tableBody) {
        console.error('[Install] 找不到表格体元素');
        return;
    }

    discoveryTable = utils.DataTable({
        tableId: 'discoveryTableBody',
        apiUrl: '/api/discovery/skills/list',
        pageName: 'Install',
        columns: [
            { field: 'id', title: '技能ID', width: '150px' },
            { field: 'name', title: '技能名称', width: '180px' },
            { field: 'type', title: '类型', width: '100px' },
            { 
                field: 'capabilities', 
                title: '能力标签', 
                width: '200px',
                formatter: (value) => {
                    if (!value || !Array.isArray(value)) return '-';
                    return value.slice(0, 3).map(cap => 
                        `<span class="nx-tag nx-tag--sm nx-tag--primary">${cap}</span>`
                    ).join(' ');
                }
            },
            { 
                field: 'scenes', 
                title: '适用场景', 
                width: '150px',
                formatter: (value) => {
                    if (!value || !Array.isArray(value)) return '-';
                    return value.slice(0, 2).join(', ');
                }
            },
            {
                field: 'actions',
                title: '操作',
                width: '150px',
                align: 'center',
                formatter: (value, row) => `
                    <div class="action-buttons">
                        <button class="nx-btn nx-btn--primary nx-btn--sm" onclick="installSkill('${row.id}')">
                            <i class="ri-download-line"></i> 安装
                        </button>
                        <button class="nx-btn nx-btn--ghost nx-btn--sm" onclick="viewSkillDetail('${row.id}')">
                            <i class="ri-eye-line"></i> 详情
                        </button>
                    </div>
                `
            }
        ],
        onLoad: (data) => {
            console.log('[Install] 技能列表加载完成，共', data.length, '条');
        },
        onError: (error) => {
            console.error('[Install] 技能列表加载失败:', error);
        }
    });
}

function initSearchEvent() {
    const btnSearch = document.getElementById('btnSearchSkill');
    if (btnSearch) {
        btnSearch.addEventListener('click', function(e) {
            e.preventDefault();
            searchSkills();
        });
    }

    const keywordInput = document.getElementById('skillKeyword');
    if (keywordInput) {
        keywordInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                searchSkills();
            }
        });
    }

    const typeFilter = document.getElementById('skillTypeFilter');
    if (typeFilter) {
        typeFilter.addEventListener('change', function() {
            searchSkills();
        });
    }
}

window.searchSkills = async function() {
    const keyword = document.getElementById('skillKeyword')?.value?.trim() || '';
    const type = document.getElementById('skillTypeFilter')?.value || '';

    if (keyword || type) {
        console.log('[Install] 搜索技能:', { keyword, type });
        try {
            const result = await utils.api.post('/api/discovery/skills/search', { 
                keyword: keyword, 
                type: type 
            });
            if (result.code === 200 && result.data) {
                console.log('[Install] 搜索结果:', result.data);
                if (discoveryTable && typeof discoveryTable.setData === 'function') {
                    discoveryTable.setData(result.data);
                }
            } else {
                utils.msg.error('搜索失败: ' + (result.message || '未知错误'));
            }
        } catch (error) {
            console.error('[Install] 搜索技能错误:', error);
            utils.msg.error('搜索失败');
        }
    } else {
        if (discoveryTable && typeof discoveryTable.refresh === 'function') {
            discoveryTable.refresh();
        }
    }
};

window.installSkill = async function(skillId) {
    console.log('[Install] 安装技能:', skillId);
    
    try {
        const result = await utils.api.post('/api/discovery/skills/download', { skillId: skillId });
        
        if (result.code === 200 && result.data) {
            utils.msg.success('技能安装成功！');
            if (discoveryTable) discoveryTable.refresh();
        } else {
            utils.msg.error('安装失败: ' + (result.message || '未知错误'));
        }
    } catch (error) {
        console.error('[Install] 安装技能错误:', error);
        utils.msg.error('安装失败');
    }
};

window.viewSkillDetail = async function(skillId) {
    console.log('[Install] 查看技能详情:', skillId);
    
    try {
        const result = await utils.api.post('/api/discovery/skills/get', { skillId: skillId });
        
        if (result.code === 200 && result.data) {
            showSkillDetailModal(result.data);
        } else {
            utils.msg.error('获取技能详情失败');
        }
    } catch (error) {
        console.error('[Install] 获取技能详情错误:', error);
        utils.msg.error('获取技能详情失败');
    }
};

function showSkillDetailModal(skill) {
    const modal = document.getElementById('skillDetailModal');
    const content = document.getElementById('skillDetailContent');
    
    if (!modal || !content) {
        console.warn('[Install] 模态框元素不存在');
        return;
    }
    
    content.innerHTML = `
        <div class="skill-detail">
            <h4>${skill.metadata?.name || skill.name || '未知技能'}</h4>
            <p><strong>类型:</strong> ${skill.metadata?.type || skill.type || '-'}</p>
            <p><strong>版本:</strong> ${skill.metadata?.version || '-'}</p>
            <p><strong>描述:</strong> ${skill.metadata?.description || '-'}</p>
            <p><strong>能力:</strong> ${(skill.capabilities || []).join(', ') || '-'}</p>
            <p><strong>场景:</strong> ${(skill.scenes || []).join(', ') || '-'}</p>
        </div>
    `;
    
    modal.style.display = 'block';
}

window.closeSkillDetailModal = function() {
    const modal = document.getElementById('skillDetailModal');
    if (modal) modal.style.display = 'none';
};

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', function() {
        initInstallPage();
    });
} else {
    initInstallPage();
}
