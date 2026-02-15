package net.ooder.skillcenter.service.impl;

import net.ooder.skillcenter.dto.PageResult;
import net.ooder.skillcenter.dto.UserDTO;
import net.ooder.skillcenter.service.UserService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "skillcenter.sdk.mode", havingValue = "mock", matchIfMissing = true)
public class UserServiceMockImpl implements UserService {

    private final Map<String, UserDTO> userStore = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        initMockData();
    }

    private void initMockData() {
        addMockUser("user-001", "admin", "admin@example.com", "管理员", true);
        addMockUser("user-002", "developer", "dev@example.com", "开发者", true);
        addMockUser("user-003", "tester", "test@example.com", "测试员", true);
        addMockUser("user-004", "guest", "guest@example.com", "访客", false);
    }

    private void addMockUser(String id, String username, String email, String displayName, boolean active) {
        UserDTO user = new UserDTO();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setDisplayName(displayName);
        user.setActive(active);
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        userStore.put(id, user);
    }

    @Override
    public int getUserCount() {
        return userStore.size();
    }

    @Override
    public int getActiveUserCount() {
        return (int) userStore.values().stream()
            .filter(user -> Boolean.TRUE.equals(user.isActive()))
            .count();
    }

    @Override
    public PageResult<UserDTO> getAllUsers(int pageNum, int pageSize) {
        List<UserDTO> list = new ArrayList<>(userStore.values());
        list.sort(Comparator.comparing(UserDTO::getCreatedAt).reversed());
        return paginate(list, pageNum, pageSize);
    }

    @Override
    public PageResult<UserDTO> searchUsers(String keyword, int pageNum, int pageSize) {
        List<UserDTO> filtered = userStore.values().stream()
            .filter(user -> keyword == null || keyword.isEmpty() ||
                user.getUsername().toLowerCase().contains(keyword.toLowerCase()) ||
                user.getEmail().toLowerCase().contains(keyword.toLowerCase()) ||
                (user.getDisplayName() != null && user.getDisplayName().toLowerCase().contains(keyword.toLowerCase())))
            .sorted(Comparator.comparing(UserDTO::getCreatedAt).reversed())
            .collect(Collectors.toList());
        return paginate(filtered, pageNum, pageSize);
    }

    @Override
    public UserDTO getUserById(String userId) {
        return userStore.get(userId);
    }

    @Override
    public UserDTO createUser(UserDTO userDTO) {
        String id = "user-" + UUID.randomUUID().toString().substring(0, 8);
        userDTO.setId(id);
        userDTO.setCreatedAt(new Date());
        userDTO.setUpdatedAt(new Date());
        userStore.put(id, userDTO);
        return userDTO;
    }

    @Override
    public UserDTO updateUser(String userId, UserDTO userDTO) {
        UserDTO existing = userStore.get(userId);
        if (existing == null) return null;
        userDTO.setId(userId);
        userDTO.setCreatedAt(existing.getCreatedAt());
        userDTO.setUpdatedAt(new Date());
        userStore.put(userId, userDTO);
        return userDTO;
    }

    @Override
    public boolean deleteUser(String userId) {
        return userStore.remove(userId) != null;
    }

    private PageResult<UserDTO> paginate(List<UserDTO> list, int pageNum, int pageSize) {
        int total = list.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);

        if (start >= total) {
            return PageResult.empty();
        }

        List<UserDTO> pageList = list.subList(start, end);
        return PageResult.of(pageList, total, pageNum, pageSize);
    }
}
