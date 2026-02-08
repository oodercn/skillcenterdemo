import requests

# 测试技能管理API
try:
    print("测试技能管理API...")
    response = requests.get("http://localhost:8081/skillcenter/api/admin/skills")
    print(f"状态码: {response.status_code}")
    print(f"响应内容: {response.json()}")
except Exception as e:
    print(f"测试失败: {e}")

# 测试市场管理API
try:
    print("\n测试市场管理API...")
    response = requests.get("http://localhost:8081/skillcenter/api/admin/market/skills")
    print(f"状态码: {response.status_code}")
    print(f"响应内容: {response.json()}")
except Exception as e:
    print(f"测试失败: {e}")

# 测试技能认证API
try:
    print("\n测试技能认证API...")
    response = requests.get("http://localhost:8081/skillcenter/api/admin/authentication/requests")
    print(f"状态码: {response.status_code}")
    print(f"响应内容: {response.json()}")
except Exception as e:
    print(f"测试失败: {e}")

# 测试群组管理API
try:
    print("\n测试群组管理API...")
    response = requests.get("http://localhost:8081/skillcenter/api/admin/groups")
    print(f"状态码: {response.status_code}")
    print(f"响应内容: {response.json()}")
except Exception as e:
    print(f"测试失败: {e}")

# 测试远程托管API
try:
    print("\n测试远程托管API...")
    response = requests.get("http://localhost:8081/skillcenter/api/admin/hosting/instances")
    print(f"状态码: {response.status_code}")
    print(f"响应内容: {response.json()}")
except Exception as e:
    print(f"测试失败: {e}")

# 测试存储管理API
try:
    print("\n测试存储管理API...")
    response = requests.get("http://localhost:8081/skillcenter/api/admin/storage/list")
    print(f"状态码: {response.status_code}")
    print(f"响应内容: {response.json()}")
except Exception as e:
    print(f"测试失败: {e}")

# 测试系统管理API
try:
    print("\n测试系统管理API...")
    response = requests.get("http://localhost:8081/skillcenter/api/admin/system/info")
    print(f"状态码: {response.status_code}")
    print(f"响应内容: {response.json()}")
except Exception as e:
    print(f"测试失败: {e}")
