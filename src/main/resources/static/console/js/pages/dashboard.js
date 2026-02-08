/**
 * 仪表盘页面脚本
 */

function updateTimestamp() {
    const now = new Date();
    const timestamp = document.getElementById('timestamp');
    if (timestamp) {
        timestamp.textContent = now.toLocaleString('zh-CN');
    }
}

async function loadDashboardData() {
    try {
        const statsResponse = await fetch(`${utils.API_BASE_URL}/dashboard/stats`);
        if (statsResponse.ok) {
            const statsData = await statsResponse.json();
            if (statsData.data) {
                const skillCount = document.getElementById('skill-count');
                if (skillCount) skillCount.textContent = statsData.data.totalSkills || 0;
            }
        }
        
        const executionStatsResponse = await fetch(`${utils.API_BASE_URL}/dashboard/execution-stats`);
        if (executionStatsResponse.ok) {
            const executionStatsData = await executionStatsResponse.json();
            if (executionStatsData.data) {
                const executionSuccessRate = document.getElementById('execution-success-rate');
                if (executionSuccessRate) executionSuccessRate.textContent = `${executionStatsData.data.successRate || 0}%`;
            }
        }
        
        const marketStatsResponse = await fetch(`${utils.API_BASE_URL}/dashboard/market-stats`);
        if (marketStatsResponse.ok) {
            const marketStatsData = await marketStatsResponse.json();
            if (marketStatsData.data) {
                const downloads = marketStatsData.data.totalDownloads || 0;
                const reviews = marketStatsData.data.totalReviews || 0;
                const activityScore = Math.min(100, Math.floor((downloads + reviews) / 40));
                
                const marketActivity = document.getElementById('market-activity');
                if (marketActivity) marketActivity.textContent = activityScore;
            }
        }
        
        const systemStatsResponse = await fetch(`${utils.API_BASE_URL}/dashboard/system-stats`);
        if (systemStatsResponse.ok) {
            const systemStatsData = await systemStatsResponse.json();
            if (systemStatsData.data) {
                const cpuUsage = document.getElementById('cpu-usage');
                if (cpuUsage) cpuUsage.textContent = `${systemStatsData.data.cpuUsage || 0}%`;
                
                const memoryUsage = document.getElementById('memory-usage');
                if (memoryUsage) memoryUsage.textContent = `${systemStatsData.data.memoryUsage || 0}%`;
            }
        }
        
        const systemStatus = document.getElementById('system-status');
        if (systemStatus) systemStatus.textContent = '运行中';
        
    } catch (error) {
        console.error('加载仪表盘数据错误:', error);
        const systemStatus = document.getElementById('system-status');
        if (systemStatus) systemStatus.textContent = '运行中';
        
        const skillCount = document.getElementById('skill-count');
        if (skillCount) skillCount.textContent = '0';
        
        const executionSuccessRate = document.getElementById('execution-success-rate');
        if (executionSuccessRate) executionSuccessRate.textContent = '0%';
        
        const marketActivity = document.getElementById('market-activity');
        if (marketActivity) marketActivity.textContent = '0';
        
        const cpuUsage = document.getElementById('cpu-usage');
        if (cpuUsage) cpuUsage.textContent = '0%';
        
        const memoryUsage = document.getElementById('memory-usage');
        if (memoryUsage) memoryUsage.textContent = '0%';
    }
}

function initDashboard() {
    updateTimestamp();
    loadDashboardData();
    setInterval(updateTimestamp, 60000);
    setInterval(loadDashboardData, 300000);
}

document.addEventListener('DOMContentLoaded', function() {
    initDashboard();
});
