/**
 * SkillCenter 菜单管理模块
 * 参考 ooder-nexus 架构设计
 */

var COMMON = COMMON || {
    menuConfig: null
};

/**
 * 加载菜单配置
 */
async function loadMenuConfig() {
    try {
        const response = await fetch('/skillcenter/console/menu-config.json');
        if (!response.ok) {
            throw new Error('菜单配置加载失败');
        }
        COMMON.menuConfig = await response.json();
        renderMenu();
    } catch (error) {
        console.error('加载菜单配置错误:', error);
        renderDefaultMenu();
    }
}

/**
 * 渲染菜单
 */
function renderMenu() {
    const sidebar = document.getElementById('sidebar');
    if (!sidebar) {
        return;
    }

    const navMenu = document.getElementById('nav-menu');
    if (!navMenu) {
        return;
    }

    navMenu.innerHTML = '';

    COMMON.menuConfig.menu.forEach(item => {
        const menuItem = createMenuItem(item);
        navMenu.appendChild(menuItem);
        
        // 添加菜单分隔线
        if (item.id !== 'dashboard') {
            const divider = document.createElement('div');
            divider.className = 'menu-divider';
            navMenu.appendChild(divider);
        }
    });
    
    // 延迟绑定事件，确保DOM完全渲染
    setTimeout(() => {
        setupNavigation();
    }, 100);
}

/**
 * 创建菜单项
 * @param {Object} menuItem 菜单项配置
 * @returns {HTMLElement} 菜单项元素
 */
function createMenuItem(menuItem) {
    const li = document.createElement('li');

    if (menuItem.children && menuItem.children.length > 0) {
        // 有子菜单的菜单项
        const a = document.createElement('a');
        a.href = `#${menuItem.id}`;
        a.innerHTML = `
            <i class="${menuItem.icon}"></i>
            ${menuItem.name}
            <span class="toggle-icon">›</span>
        `;

        // 处理点击事件
        try {
            // 确保a是有效的DOM元素
            if (a && typeof a.addEventListener === 'function') {
                a.addEventListener('click', function(e) {
                    e.preventDefault();
                    e.stopPropagation();
                    
                    const toggleIcon = this.querySelector('.toggle-icon');
                    const submenu = this.nextElementSibling;

                    if (submenu) {
                        submenu.classList.toggle('hidden');
                        if (toggleIcon) {
                            toggleIcon.classList.toggle('collapsed');
                        }
                    }
                });
            } else {
                console.warn('链接元素无效或不支持addEventListener方法:', a);
            }
        } catch (e) {
            console.warn('添加点击事件监听器失败:', e);
        }

        li.appendChild(a);

        // 创建子菜单
        const submenu = document.createElement('ul');
        submenu.className = 'submenu hidden';

        menuItem.children.forEach(childItem => {
            const childLi = createMenuItem(childItem);
            submenu.appendChild(childLi);
        });

        // 只有当子菜单不为空时才添加
        if (submenu.children.length > 0) {
            li.appendChild(submenu);
        }
    } else {
        // 无子菜单的菜单项
        const a = document.createElement('a');
        if (menuItem.url) {
            a.href = '#';
            a.setAttribute('data-url', menuItem.url);
        } else {
            a.href = `#${menuItem.id}`;
        }
        a.setAttribute('data-page', menuItem.page || menuItem.id);
        a.innerHTML = `
            <i class="${menuItem.icon}"></i>
            ${menuItem.name}
        `;

        // 检查是否已实现
        if (menuItem.status !== 'implemented' && menuItem.id !== 'dashboard') {
            a.classList.add('disabled');
            a.title = '功能开发中，敬请期待';
        }

        li.appendChild(a);
    }

    return li;
}

/**
 * 渲染默认菜单（加载失败时使用）
 */
async function renderDefaultMenu() {
    try {
        const response = await fetch('/skillcenter/console/menu-config.json');
        if (!response.ok) {
            throw new Error('菜单配置加载失败');
        }
        const config = await response.json();
        
        const navMenu = document.getElementById('nav-menu');
        if (!navMenu) {
            return;
        }

        navMenu.innerHTML = '';

        config.menu.forEach(item => {
            const menuItem = createMenuItem(item);
            navMenu.appendChild(menuItem);
            
            // 添加菜单分隔线
            if (item.id !== 'dashboard') {
                const divider = document.createElement('div');
                divider.className = 'menu-divider';
                navMenu.appendChild(divider);
            }
        });
        
        // 延迟绑定事件，确保DOM完全渲染
        setTimeout(() => {
            setupNavigation();
        }, 100);
    } catch (error) {
        console.error('渲染默认菜单错误:', error);
        const navMenu = document.getElementById('nav-menu');
        if (navMenu) {
            navMenu.innerHTML = '<li><a href="#dashboard" class="active" data-page="dashboard"><i class="ri-dashboard-line"></i> 仪表盘</a></li>';
        }
    }
}

/**
 * 设置导航
 */
function setupNavigation() {
    try {
        console.log('开始设置导航...');
        const navMenu = document.getElementById('nav-menu');
        if (!navMenu) {
            console.warn('未找到 nav-menu 元素');
            return;
        }
        
        const navLinks = navMenu.querySelectorAll('a');
        console.log('找到导航链接数量:', navLinks.length);
        
        if (!navLinks || navLinks.length === 0) {
            console.warn('未找到导航链接');
            return;
        }
        
        navLinks.forEach((link, index) => {
            if (!link) {
                console.warn('导航链接为null，索引:', index);
                return;
            }
            
            try {
                // 检查是否有data-page属性
                if (link.hasAttribute('data-page')) {
                    console.log('为链接添加事件监听器:', link.getAttribute('data-page'));
                    
                    // 检查是否已经有点击事件监听器
                    if (!link.__hasClickEventListener) {
                        link.__hasClickEventListener = true;
                        
                        // 确保link是有效的DOM元素
                        if (typeof link.addEventListener === 'function') {
                            link.addEventListener('click', function(e) {
                                e.preventDefault();
                                e.stopPropagation();
                                
                                const page = this.getAttribute('data-page');
                                const url = this.getAttribute('data-url');
                                console.log('点击导航链接:', page, 'URL:', url);
                                
                                // 更新活跃状态
                                navMenu.querySelectorAll('a').forEach(l => {
                                    if (l) {
                                        l.classList.remove('active');
                                    }
                                });
                                this.classList.add('active');
                                
                                // 如果有URL属性，使用URL加载页面
                                if (url) {
                                    console.log('通过URL加载页面:', url);
                                    loadPageContentByUrl(url);
                                } else {
                                    // 否则使用页面名称加载
                                    console.log('通过页面名称加载:', page);
                                    loadPageContent(page);
                                }
                            });
                        } else {
                            console.warn('链接元素不支持addEventListener方法:', link);
                        }
                    }
                }
            } catch (e) {
                console.error('为链接添加事件监听器失败，索引:', index, '错误:', e);
            }
        });
        
        console.log('导航设置完成');
    } catch (e) {
        console.error('设置导航失败:', e);
    }
}

/**
 * 通过URL加载页面内容
 * @param {string} url - 页面URL
 */
async function loadPageContentByUrl(url) {
    const mainContent = document.getElementById('main-content');
    if (!mainContent) {
        return;
    }
    
    const fullUrl = `/skillcenter/console/${url}`;
    console.log('正在加载页面:', fullUrl);
    const response = await fetch(fullUrl);
    console.log('页面加载响应状态:', response.status);
    if (!response.ok) {
        throw new Error(`页面加载失败: ${response.status} ${response.statusText}`);
    }
    const html = await response.text();
    console.log('页面加载成功，内容长度:', html.length);
    
    // 解析HTML并提取主要内容
    const parser = new DOMParser();
    const doc = parser.parseFromString(html, 'text/html');
    const pageContent = doc.querySelector('.skillcenter-content-with-sidebar') || doc.querySelector('.main-content');
    
    if (pageContent) {
        console.log('[Menu] pageContent HTML长度:', pageContent.innerHTML.length);
        console.log('[Menu] pageContent 是否包含按钮:', pageContent.innerHTML.includes('publish-skill-btn'));
        mainContent.innerHTML = pageContent.innerHTML;
        console.log('[Menu] mainContent 更新后 HTML长度:', mainContent.innerHTML.length);
        
        // 检查按钮是否存在
        const btn = document.getElementById('publish-skill-btn');
        console.log('[Menu] 发布技能按钮:', btn);
        console.log('[Menu] mainContent 内部HTML是否包含按钮:', mainContent.innerHTML.includes('publish-skill-btn'));
        
        // 同时提取模态框元素（在main之外的元素）
        const modals = doc.querySelectorAll('.modal');
        console.log('[Menu] 找到模态框数量:', modals.length);
        modals.forEach((modal, index) => {
            const modalId = modal.id;
            // 检查是否已存在
            if (modalId && !document.getElementById(modalId)) {
                console.log('[Menu] 添加模态框到DOM:', modalId);
                document.body.appendChild(modal);
            }
        });
        
        // 执行新页面中的脚本
        const scripts = doc.querySelectorAll('script');
        console.log('[Menu] 找到脚本数量:', scripts.length);
        scripts.forEach((script, index) => {
            console.log(`[Menu] 脚本 ${index}:`, script.src || '内联脚本');
            if (script.src) {
                const newScript = document.createElement('script');
                newScript.src = script.src;
                newScript.onload = function() {
                    console.log('[Menu] 脚本加载完成:', script.src);
                };
                newScript.onerror = function() {
                    console.error('[Menu] 脚本加载失败:', script.src);
                };
                document.body.appendChild(newScript);
            } else {
                const scriptContent = script.textContent;
                if (!scriptContent) {
                    return;
                }
                
                eval(scriptContent);
            }
        });
        
        // 从URL中提取页面名称
        const pageName = url.replace('pages/personal/', '').replace('pages/', '').replace('.html', '');
        console.log('[Menu] 从URL提取的页面名称:', pageName);
        
        // 等待外部脚本加载完成后初始化
        setTimeout(() => {
            console.log('[Menu] 检查初始化函数...');
            console.log('[Menu] initTabComponentV3:', typeof initTabComponentV3);
            console.log('[Menu] initMySkillPage:', typeof initMySkillPage);
            console.log('[Menu] initMySharingPage:', typeof initMySharingPage);
            console.log('[Menu] initMyExecutionPage:', typeof initMyExecutionPage);

            // 重新初始化Tab组件
            if (typeof initTabComponentV3 === 'function') {
                console.log('[Menu] 页面加载完成，重新初始化Tab组件V3');
                initTabComponentV3();
            }

            // 根据当前页面类型初始化对应的页面
            if (pageName === 'my-skill' && typeof initMySkillPage === 'function') {
                console.log('[Menu] 初始化我的技能页面');
                initMySkillPage();
            } else if (pageName === 'my-sharing' && typeof initMySharingPage === 'function') {
                console.log('[Menu] 初始化技能分享页面');
                initMySharingPage();
            } else if (pageName === 'my-execution' && typeof initMyExecutionPage === 'function') {
                console.log('[Menu] 初始化我的执行页面');
                initMyExecutionPage();
            } else if (pageName === 'my-group' && typeof initMyGroupPage === 'function') {
                console.log('[Menu] 初始化我的群组页面');
                initMyGroupPage();
            } else if (pageName === 'skill-authentication' && typeof initSkillAuthenticationPage === 'function') {
                console.log('[Menu] 初始化技能认证页面');
                initSkillAuthenticationPage();
            }
        }, 500);
    } else {
        console.warn('页面中未找到 .main-content 元素');
        mainContent.innerHTML = '<div class="section"><p>页面内容加载失败</p></div>';
    }
}

/**
 * 加载页面内容
 * @param {string} pageName - 页面名称
 */
async function loadPageContent(pageName) {
    const mainContent = document.getElementById('main-content');
    if (!mainContent) {
        return;
    }
    
    // 页面映射
    const pageMap = {
        'dashboard': 'pages/dashboard.html',
        'personal-dashboard': 'pages/personal/dashboard.html',
        'my-skills': 'pages/personal/my-skill.html',
        'my-execution': 'pages/personal/my-execution.html',
        'my-sharing': 'pages/personal/my-sharing.html',
        'my-groups': 'pages/personal/my-group.html',
        'my-identity': 'pages/personal/my-identity.html',
        'my-help': 'pages/personal/my-help.html',
        'market-skills': 'pages/market.html',
        'admin-dashboard': 'pages/admin/dashboard.html',
        'skill-management': 'pages/admin/skill-management.html',
        'market-management': 'pages/admin/market-management.html',
        'skill-authentication': 'pages/admin/skill-authentication.html',
        'group-management': 'pages/admin/group-management.html',
        'remote-hosting': 'pages/admin/remote-hosting.html',
        'storage-management': 'pages/admin/storage-management.html',
        'system-management': 'pages/admin/system-management.html'
    };
    
    const pagePath = pageMap[pageName];
    if (!pagePath) {
        console.warn('未找到页面映射:', pageName);
        mainContent.innerHTML = '<div class="section"><p>页面未找到</p></div>';
        return;
    }
    
    const fullUrl = `/skillcenter/console/${pagePath}`;
    console.log('正在加载页面:', fullUrl);
    const response = await fetch(fullUrl);
    console.log('页面加载响应状态:', response.status);
    if (!response.ok) {
        throw new Error(`页面加载失败: ${response.status} ${response.statusText}`);
    }
    const html = await response.text();
    console.log('页面加载成功，内容长度:', html.length);
    
    // 解析HTML并提取主要内容
    const parser = new DOMParser();
    const doc = parser.parseFromString(html, 'text/html');
    const pageContent = doc.querySelector('.skillcenter-content-with-sidebar') || doc.querySelector('.main-content');
    
    if (pageContent) {
        console.log('[Menu] loadPageContent pageContent HTML长度:', pageContent.innerHTML.length);
        console.log('[Menu] loadPageContent pageContent 是否包含按钮:', pageContent.innerHTML.includes('publish-skill-btn'));
        mainContent.innerHTML = pageContent.innerHTML;
        console.log('[Menu] loadPageContent mainContent 更新后 HTML长度:', mainContent.innerHTML.length);
        
        // 检查按钮是否存在
        const btn = document.getElementById('publish-skill-btn');
        console.log('[Menu] loadPageContent 发布技能按钮:', btn);
        console.log('[Menu] loadPageContent mainContent 内部HTML是否包含按钮:', mainContent.innerHTML.includes('publish-skill-btn'));
        
        // 同时提取模态框元素（在main之外的元素）
        const modals = doc.querySelectorAll('.modal');
        console.log('[Menu] 找到模态框数量:', modals.length);
        modals.forEach((modal, index) => {
            const modalId = modal.id;
            // 检查是否已存在
            if (modalId && !document.getElementById(modalId)) {
                console.log('[Menu] 添加模态框到DOM:', modalId);
                document.body.appendChild(modal);
            }
        });
        
        // 执行新页面中的脚本
        const scripts = doc.querySelectorAll('script');
        console.log('[Menu] 找到脚本数量:', scripts.length);
        scripts.forEach((script, index) => {
            console.log(`[Menu] 脚本 ${index}:`, script.src || '内联脚本');
            if (script.src) {
                const newScript = document.createElement('script');
                newScript.src = script.src;
                newScript.onload = function() {
                    console.log('[Menu] 脚本加载完成:', script.src);
                };
                newScript.onerror = function() {
                    console.error('[Menu] 脚本加载失败:', script.src);
                };
                document.body.appendChild(newScript);
            } else {
                const scriptContent = script.textContent;
                if (!scriptContent) {
                    return;
                }
                
                eval(scriptContent);
            }
        });
        
        // 等待外部脚本加载完成后初始化
        setTimeout(() => {
            console.log('[Menu] 检查初始化函数...');
            console.log('[Menu] initTabComponentV3:', typeof initTabComponentV3);
            console.log('[Menu] initMySkillPage:', typeof initMySkillPage);
            console.log('[Menu] initMySharingPage:', typeof initMySharingPage);
            console.log('[Menu] initMyExecutionPage:', typeof initMyExecutionPage);
            
            // 重新初始化Tab组件
            if (typeof initTabComponentV3 === 'function') {
                console.log('[Menu] 页面加载完成，重新初始化Tab组件V3');
                initTabComponentV3();
            }
            
            // 初始化我的技能页面
            if (typeof initMySkillPage === 'function') {
                console.log('[Menu] 初始化我的技能页面');
                initMySkillPage();
            }
            
            // 初始化技能分享页面
            if (typeof initMySharingPage === 'function') {
                console.log('[Menu] 初始化技能分享页面');
                initMySharingPage();
            }
            
            // 初始化我的执行页面
            if (typeof initMyExecutionPage === 'function') {
                console.log('[Menu] 初始化我的执行页面');
                initMyExecutionPage();
            }
        }, 500);
    } else {
        console.warn('页面中未找到 .main-content 元素');
        mainContent.innerHTML = '<div class="section"><p>页面内容加载失败</p></div>';
    }
}

/**
 * 初始化菜单（兼容旧版本）
 * @param {string} activeSection 当前活动的菜单项ID
 */
function initMenu(activeSection) {
    if (typeof COMMON === 'undefined') {
        loadMenuConfig();
    }
    
    // 设置当前活动菜单项
    setTimeout(() => {
        const navLinks = document.querySelectorAll('.nav-menu a');
        navLinks.forEach(link => {
            const dataPage = link.getAttribute('data-page');
            if (dataPage === activeSection) {
                link.classList.add('active');
            }
        });
    }, 100);
}

/**
 * 页面加载完成后初始化菜单
 */
document.addEventListener('DOMContentLoaded', function() {
    loadMenuConfig();
});

/**
 * 导出模块
 */
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        loadMenuConfig,
        renderMenu,
        createMenuItem,
        renderDefaultMenu,
        setupNavigation
    };
}
