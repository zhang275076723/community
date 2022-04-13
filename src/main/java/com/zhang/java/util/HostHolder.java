package com.zhang.java.util;

import com.zhang.java.domain.User;
import org.springframework.stereotype.Component;

/**
 * @Date 2022/4/12 23:46
 * @Author zsy
 * @Description 持有用户信息，代替session对象
 */
@Component
public class HostHolder {
    private ThreadLocal<User> threadLocal = new ThreadLocal<>();

    public void setUser(User user) {
        threadLocal.set(user);
    }

    public User getUser() {
        return threadLocal.get();
    }

    public void clear() {
        threadLocal.remove();
    }
}
