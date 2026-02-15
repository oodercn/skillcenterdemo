// 初始化菜单
initMenu('my-groups');

// 使用 IIFE 封装，避免全局变量污染
(function() {
    // 模拟群组数据
    let myGroups = [];
    
    // 模拟群组技能数据
    let myGroupSkills = [];
    
    // 加载群组数据
    async function loadGroupsData() {
        try {
            // 从API获取数据 - 使用正确的API路径
            const response = await fetch(`${utils.API_BASE_URL}/personal/groups`);
            if (response.ok) {
                const result = await response.json();
                if (result.success && result.data) {
                    myGroups = result.data;
                    console.log('[MyGroup] 从API加载群组数据:', myGroups.length);
                    return;
                }
            }
            throw new Error('API返回数据格式不正确');
        } catch (error) {
            console.warn('[MyGroup] API获取失败，使用模拟数据:', error);
            // 使用模拟数据
            myGroups = [
                {
                    id: 'dev-team',
                    name: '开发团队',
                    description: '公司的开发团队，负责产品的技术开发',
                    memberCount: 15,
                    createdAt: '2026-01-01',
                    role: '成员'
                },
                {
                    id: 'marketing-team',
                    name: '市场团队',
                    description: '公司的市场团队，负责产品的市场推广',
                    memberCount: 8,
                    createdAt: '2026-01-05',
                    role: '成员'
                },
                {
                    id: 'hr-team',
                    name: '人力资源团队',
                    description: '公司的人力资源团队，负责人事管理',
                    memberCount: 5,
                    createdAt: '2026-01-10',
                    role: '管理员'
                }
            ];
        }
    }

    // 加载群组技能数据
    async function loadGroupSkillsData() {
        try {
            // 从API获取数据 - 使用正确的API路径
            const response = await fetch(`${utils.API_BASE_URL}/personal/groups/skills`);
            if (response.ok) {
                const result = await response.json();
                if (result.success && result.data) {
                    myGroupSkills = result.data;
                    console.log('[MyGroup] 从API加载群组技能数据:', myGroupSkills.length);
                    return;
                }
            }
            throw new Error('API返回数据格式不正确');
        } catch (error) {
            console.warn('[MyGroup] API获取失败，使用模拟数据:', error);
            // 使用模拟数据
            myGroupSkills = [
                {
                    id: 'group-skill-001',
                    groupId: 'dev-team',
                    groupName: '开发团队',
                    skillId: 'code-generator',
                    skillName: '代码生成',
                    sharedBy: '张三',
                    sharedAt: '2026-01-20',
                    description: '用于生成各种代码模板'
                },
                {
                    id: 'group-skill-002',
                    groupId: 'dev-team',
                    groupName: '开发团队',
                    skillId: 'json-formatter',
                    skillName: 'JSON格式化',
                    sharedBy: '李四',
                    sharedAt: '2026-01-22',
                    description: '格式化JSON数据'
                },
                {
                    id: 'group-skill-003',
                    groupId: 'marketing-team',
                    groupName: '市场团队',
                    skillId: 'image-resizer',
                    skillName: '图片 resize',
                    sharedBy: '王五',
                    sharedAt: '2026-01-25',
                    description: '调整图片大小'
                }
            ];
        }
    }

    // 渲染群组列表
    function renderGroupList() {
        const groupList = document.getElementById('group-list');
        if (!groupList) return;
        
        groupList.innerHTML = '';
        
        myGroups.forEach(group => {
            const groupCard = document.createElement('div');
            groupCard.className = 'group-card';
            groupCard.innerHTML = `
                <div class="group-card-header">
                    <h3>${group.name}</h3>
                    <span class="group-role">${group.role}</span>
                </div>
                <div class="group-card-body">
                    <p>${group.description}</p>
                    <div class="group-meta">
                        <span class="group-members">成员: ${group.memberCount}人</span>
                        <span class="group-created">创建于: ${group.createdAt}</span>
                    </div>
                </div>
                <div class="group-card-footer">
                    <button class="btn-sm btn-primary view-group-skills-btn" data-id="${group.id}">
                        <i class="ri-lightbulb-line"></i> 查看技能
                    </button>
                </div>
            `;
            groupList.appendChild(groupCard);
        });
        
        // 绑定查看群组技能按钮事件
        document.querySelectorAll('.view-group-skills-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                const groupId = this.getAttribute('data-id');
                // 切换到技能标签并筛选该群组的技能
                const skillsTabBtn = document.querySelector('.tab-btn[data-tab="skills"]');
                if (skillsTabBtn) skillsTabBtn.click();
                filterGroupSkills(groupId);
            });
        });
    }

    // 渲染群组技能
    function renderGroupSkills() {
        const groupSkillsContainer = document.getElementById('group-skills');
        if (!groupSkillsContainer) return;
        
        groupSkillsContainer.innerHTML = '';
        
        myGroupSkills.forEach(skill => {
            const skillCard = document.createElement('div');
            skillCard.className = 'group-skill-card';
            skillCard.innerHTML = `
                <div class="group-skill-card-header">
                    <h4>${skill.skillName}</h4>
                    <span class="group-name">${skill.groupName}</span>
                </div>
                <div class="group-skill-card-body">
                    <p>${skill.description}</p>
                    <div class="skill-meta">
                        <span class="shared-by">分享人: ${skill.sharedBy}</span>
                        <span class="shared-at">分享时间: ${skill.sharedAt}</span>
                    </div>
                </div>
                <div class="group-skill-card-footer">
                    <button class="btn-sm btn-primary add-to-my-skills-btn" data-id="${skill.id}">
                        <i class="ri-add-circle-line"></i> 添加到我的技能
                    </button>
                </div>
            `;
            groupSkillsContainer.appendChild(skillCard);
        });
        
        // 绑定添加到我的技能按钮事件
        document.querySelectorAll('.add-to-my-skills-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                const skillId = this.getAttribute('data-id');
                addToMySkills(skillId);
            });
        });
    }

    // 筛选群组技能
    function filterGroupSkills(groupId) {
        const skillCards = document.querySelectorAll('.group-skill-card');
        const group = myGroups.find(g => g.id === groupId);
        if (!group) return;
        
        skillCards.forEach(card => {
            const cardGroupName = card.querySelector('.group-name').textContent;
            if (cardGroupName === group.name) {
                card.style.display = 'block';
            } else {
                card.style.display = 'none';
            }
        });
    }

    // 添加到我的技能
    function addToMySkills(skillId) {
        const skill = myGroupSkills.find(s => s.id === skillId);
        if (skill) {
            alert(`技能 ${skill.skillName} 已添加到我的技能！`);
            // 这里可以添加实际的添加逻辑
        }
    }

    // 初始化页面
    async function initPage() {
        console.log('[MyGroup] 初始化我的群组页面');
        
        // 加载数据
        await loadGroupsData();
        await loadGroupSkillsData();
        
        // 渲染群组列表和群组技能
        renderGroupList();
        renderGroupSkills();
        
        // 标签切换
        document.querySelectorAll('.tab-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                // 移除所有标签的active类
                document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
                // 添加当前标签的active类
                this.classList.add('active');
                // 隐藏所有内容
                document.querySelectorAll('.tab-content').forEach(content => content.style.display = 'none');
                // 显示当前标签的内容
                const tabId = this.getAttribute('data-tab');
                const tabContent = document.getElementById(`${tabId}-tab`);
                if (tabContent) tabContent.style.display = 'block';
            });
        });
        
        // 搜索群组
        const groupSearchInput = document.getElementById('group-search-input');
        if (groupSearchInput) {
            groupSearchInput.addEventListener('input', function() {
                const searchTerm = this.value.toLowerCase();
                const activeTabBtn = document.querySelector('.tab-btn.active');
                if (!activeTabBtn) return;
                
                const activeTab = activeTabBtn.getAttribute('data-tab');
                
                if (activeTab === 'list') {
                    const groupCards = document.querySelectorAll('.group-card');
                    groupCards.forEach(card => {
                        const groupName = card.querySelector('h3').textContent.toLowerCase();
                        const groupDescription = card.querySelector('p').textContent.toLowerCase();
                        if (groupName.includes(searchTerm) || groupDescription.includes(searchTerm)) {
                            card.style.display = 'block';
                        } else {
                            card.style.display = 'none';
                        }
                    });
                } else if (activeTab === 'skills') {
                    const skillCards = document.querySelectorAll('.group-skill-card');
                    skillCards.forEach(card => {
                        const skillName = card.querySelector('h4').textContent.toLowerCase();
                        const groupName = card.querySelector('.group-name').textContent.toLowerCase();
                        if (skillName.includes(searchTerm) || groupName.includes(searchTerm)) {
                            card.style.display = 'block';
                        } else {
                            card.style.display = 'none';
                        }
                    });
                }
            });
        }
    }
    
    // 导出初始化函数供外部调用
    window.initMyGroupPage = function() {
        console.log('[MyGroup] 外部初始化调用');
        
        // 使用重试机制确保页面内容已加载
        let retryCount = 0;
        const maxRetries = 10;
        const retryInterval = 200;
        
        function tryInit() {
            const groupList = document.getElementById('group-list');
            const groupSkills = document.getElementById('group-skills');
            
            if (groupList && groupSkills) {
                console.log('[MyGroup] 页面元素已就绪，执行初始化');
                initPage();
                return true;
            }
            
            retryCount++;
            if (retryCount < maxRetries) {
                console.log(`[MyGroup] 页面元素未就绪，第${retryCount}次重试...`);
                setTimeout(tryInit, retryInterval);
            } else {
                console.error('[MyGroup] 达到最大重试次数，页面元素仍未就绪');
                console.error('[MyGroup] group-list:', groupList);
                console.error('[MyGroup] group-skills:', groupSkills);
            }
            return false;
        }
        
        tryInit();
    };
    
    // DOMContentLoaded 事件（直接访问页面时使用）
    document.addEventListener('DOMContentLoaded', function() {
        // 检查是否是通过菜单加载的（menu.js会调用initMyGroupPage）
        // 如果页面元素已经存在，说明是直接访问页面
        const groupList = document.getElementById('group-list');
        if (groupList) {
            console.log('[MyGroup] 直接访问页面，自动初始化');
            initPage();
        }
    });
})();
