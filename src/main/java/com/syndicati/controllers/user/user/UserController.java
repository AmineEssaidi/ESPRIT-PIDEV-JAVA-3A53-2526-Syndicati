package com.syndicati.controllers.user.user;

import com.syndicati.models.user.User;
import com.syndicati.services.user.user.UserService;
import com.syndicati.utils.notifications.GlobalNotificationPillManager;

import java.util.List;
import java.util.Optional;

/**
 * User-focused controller for Java-side CRUD orchestration.
 */
public class UserController {

    private final UserService userService;

    public UserController() {
        this.userService = new UserService();
    }

    public List<User> users() {
        return userService.listUsers();
    }

    public Optional<User> userById(int idUser) {
        return userService.findById(idUser);
    }

    public Optional<User> findById(int idUser) {
        return userService.findById(idUser);
    }

    public List<User> findAllByIds(List<Integer> ids) {
        return userService.findAllByIds(ids);
    }

    public Optional<User> userByEmail(String emailUser) {
        return userService.findByEmail(emailUser);
    }

    public Optional<User> findByEmail(String emailUser) {
        return userService.findByEmail(emailUser);
    }

    public List<User> searchByName(String query, int excludeUserId, int limit) {
        return userService.searchByName(query, excludeUserId, limit);
    }

    public int userAdd(User user) {
        int id = userService.createUser(user);
        if (id > 0) {
            GlobalNotificationPillManager.created("Account", "Account created successfully.");
        }
        return id;
    }

    public boolean userEdit(User user) {
        boolean success = userService.updateUser(user);
        if (success) {
            GlobalNotificationPillManager.updated("Account", "Account updated successfully.");
        }
        return success;
    }

    public boolean userDelete(int idUser) {
        boolean success = userService.deleteUser(idUser);
        if (success) {
            GlobalNotificationPillManager.deleted("Account", "Account deleted successfully.");
        }
        return success;
    }
}
