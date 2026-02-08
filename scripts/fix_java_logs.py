#!/usr/bin/env python3
"""
批量修复Java文件中的System.out.println和System.err.println
"""

import os
import re

# 需要处理的文件列表
files_to_process = [
    "src/main/java/net/ooder/skillcenter/market/SkillMarketManager.java",
    "src/main/java/net/ooder/skillcenter/manager/UserManager.java",
    "src/main/java/net/ooder/skillcenter/shell/lifecycle/SkillExecuteCommand.java",
    "src/main/java/net/ooder/skillcenter/p2p/refresh/impl/SkillsAgentRefreshEngineImpl.java",
    "src/main/java/net/ooder/skillcenter/p2p/P2PSkillExecutor.java",
    "src/main/java/net/ooder/skillcenter/p2p/discovery/impl/SkillsAgentDiscoveryServiceImpl.java",
    "src/main/java/net/ooder/skillcenter/test/TestDataRunner.java",
    "src/main/java/net/ooder/skillcenter/test/TestDataGenerator.java",
    "src/main/java/net/ooder/skillcenter/personalai/PrivacyManager.java",
    "src/main/java/net/ooder/skillcenter/personalai/PersonalAICenter.java",
    "src/main/java/net/ooder/skillcenter/personalai/IdentityManager.java",
    "src/main/java/net/ooder/skillcenter/personalai/DeviceManager.java",
    "src/main/java/net/ooder/skillcenter/personalai/DataSyncManager.java",
    "src/main/java/net/ooder/skillcenter/model/impl/LocalDeploymentSkill.java",
    "src/main/java/net/ooder/skillcenter/identity/IdentityStorage.java",
    "src/main/java/net/ooder/skillcenter/identity/DecentralizedIdentityManager.java",
    "src/main/java/net/ooder/skillcenter/ide/TestingPlugin.java",
    "src/main/java/net/ooder/skillcenter/ide/SkillDevelopmentPlugin.java",
    "src/main/java/net/ooder/skillcenter/ide/DebuggingPlugin.java",
    "src/main/java/net/ooder/skillcenter/ide/AIIDEIntegrationManager.java",
    "src/main/java/net/ooder/skillcenter/storage/VfsStorageService.java",
    "src/main/java/net/ooder/skillcenter/storage/JsonStorageService.java",
    "src/main/java/net/ooder/skillcenter/shell/ExecuteAsyncCommand.java",
    "src/main/java/net/ooder/skillcenter/shell/CommandOutput.java",
    "src/main/java/net/ooder/skillcenter/manager/StorageManager.java",
    "src/main/java/net/ooder/skillcenter/manager/HostingManager.java",
    "src/main/java/net/ooder/skillcenter/manager/GroupManager.java",
    "src/main/java/net/ooder/skillcenter/manager/AuthenticationManager.java",
    "src/main/java/net/ooder/skillcenter/ide/CodeGenerationPlugin.java",
]

def process_file(filepath):
    """处理单个Java文件"""
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # 获取类名
        class_match = re.search(r'public class (\w+)', content)
        if not class_match:
            print(f"Skipping {filepath} - No public class found")
            return False
        
        class_name = class_match.group(1)
        
        # 检查是否已有Logger
        if 'LoggerFactory.getLogger' in content:
            print(f"Skipping {filepath} - Logger already exists")
            return False
        
        # 检查是否有System.out/err
        if 'System.out.println' not in content and 'System.err.println' not in content:
            print(f"Skipping {filepath} - No System.out/err found")
            return False
        
        # 添加import
        if 'import org.slf4j.Logger;' not in content:
            import_section = "import org.slf4j.Logger;\nimport org.slf4j.LoggerFactory;"
            content = re.sub(r'(package [^;]+;\n)', r'\1\n' + import_section + '\n', content)
        
        # 添加Logger声明
        logger_decl = f'    private static final Logger logger = LoggerFactory.getLogger({class_name}.class);\n'
        content = re.sub(r'(public class ' + class_name + r' \{)\n', r'\1\n' + logger_decl + '\n', content)
        
        # 替换System.out.println
        content = re.sub(r'System\.out\.println\(([^)]+)\);', r'logger.info(\1);', content)
        
        # 替换System.err.println
        content = re.sub(r'System\.err\.println\(([^)]+)\);', r'logger.error(\1);', content)
        
        # 删除e.printStackTrace()
        content = re.sub(r'\n\s*e\.printStackTrace\(\);', '', content)
        
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        
        print(f"Processed {filepath}")
        return True
    except Exception as e:
        print(f"Error processing {filepath}: {e}")
        return False

def main():
    base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    
    processed = 0
    for filepath in files_to_process:
        full_path = os.path.join(base_dir, filepath)
        if os.path.exists(full_path):
            if process_file(full_path):
                processed += 1
        else:
            print(f"File not found: {full_path}")
    
    print(f"\nTotal files processed: {processed}")

if __name__ == '__main__':
    main()
