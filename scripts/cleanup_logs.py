#!/usr/bin/env python3
"""
清理Java文件中的System.out.println和System.err.println
替换为SLF4J Logger
"""

import os
import re
import sys

# 需要处理的文件列表
JAVA_FILES = [
    "src/main/java/net/ooder/skillcenter/market/SkillMarketManager.java",
    "src/main/java/net/ooder/skillcenter/storage/StorageManager.java",
    "src/main/java/net/ooder/skillcenter/storage/GroupStorageService.java",
    "src/main/java/net/ooder/skillcenter/storage/ExecutionStorageService.java",
    "src/main/java/net/ooder/skillcenter/market/SDKSkillStorage.java",
    "src/main/java/net/ooder/skillcenter/manager/UserManager.java",
    "src/main/java/net/ooder/skillcenter/shell/ShellConsole.java",
    "src/main/java/net/ooder/skillcenter/p2p/P2PSkillExecutor.java",
    "src/main/java/net/ooder/skillcenter/p2p/discovery/impl/SkillsAgentDiscoveryServiceImpl.java",
    "src/main/java/net/ooder/skillcenter/p2p/refresh/impl/SkillsAgentRefreshEngineImpl.java",
    "src/main/java/net/ooder/skillcenter/test/TestDataGenerator.java",
    "src/main/java/net/ooder/skillcenter/test/TestDataRunner.java",
    "src/main/java/net/ooder/skillcenter/personalai/PrivacyManager.java",
    "src/main/java/net/ooder/skillcenter/personalai/PersonalAICenter.java",
    "src/main/java/net/ooder/skillcenter/shell/lifecycle/SkillExecuteCommand.java",
    "src/main/java/net/ooder/ooder/skillcenter/manager/SystemManager.java",
]

def add_logger_to_file(filepath):
    """为Java文件添加Logger"""
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # 获取类名
    class_match = re.search(r'public class (\w+)', content)
    if not class_match:
        return False
    
    class_name = class_match.group(1)
    
    # 检查是否已有Logger
    if 'LoggerFactory.getLogger' in content:
        print(f"Skipping {filepath} - Logger already exists")
        return False
    
    # 添加import
    if 'import org.slf4j.Logger;' not in content:
        import_section = "import org.slf4j.Logger;\nimport org.slf4j.LoggerFactory;"
        # 在package声明后添加import
        content = re.sub(
            r'(package [^;]+;\n)',
            r'\1\n' + import_section + '\n',
            content
        )
    
    # 添加Logger声明
    logger_decl = f'    private static final Logger logger = LoggerFactory.getLogger({class_name}.class);\n'
    
    # 在类声明后的第一个字段或方法前添加Logger
    content = re.sub(
        r'(public class ' + class_name + r' \{)\n',
        r'\1\n' + logger_decl + '\n',
        content
    )
    
    # 替换System.out.println
    content = re.sub(
        r'System\.out\.println\(([^)]+)\);',
        r'logger.info(\1);',
        content
    )
    
    # 替换System.err.println (简单字符串)
    content = re.sub(
        r'System\.err\.println\(("[^"]+"\s*\+\s*[^)]+)\);',
        r'logger.error(\1);',
        content
    )
    
    # 替换System.err.println (带异常)
    content = re.sub(
        r'System\.err\.println\(([^)]+)\);\s*\n\s*e\.printStackTrace\(\);',
        r'logger.error(\1, e);',
        content
    )
    
    # 替换单独的e.printStackTrace()
    content = re.sub(
        r'\n\s*e\.printStackTrace\(\);',
        '',
        content
    )
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
    
    print(f"Processed {filepath}")
    return True

def main():
    base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    
    processed = 0
    for filepath in JAVA_FILES:
        full_path = os.path.join(base_dir, filepath)
        if os.path.exists(full_path):
            if add_logger_to_file(full_path):
                processed += 1
        else:
            print(f"File not found: {full_path}")
    
    print(f"\nTotal files processed: {processed}")

if __name__ == '__main__':
    main()
