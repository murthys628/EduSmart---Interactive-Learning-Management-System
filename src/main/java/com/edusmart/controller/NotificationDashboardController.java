package com.edusmart.controller;

import com.edusmart.dto.NotificationDTO;
import com.edusmart.entity.Notification;
import com.edusmart.entity.User;
import com.edusmart.mapper.NotificationMapper;
import com.edusmart.service.NotificationService;
import com.edusmart.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/dashboard/notifications")
public class NotificationDashboardController {

    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationDashboardController(NotificationService notificationService,
                                           UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    // ------------------ VIEW ALL NOTIFICATIONS ------------------
    @GetMapping
    public String viewNotifications(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        // ✅ Fetch current user (ensures no Optional null issues)
        User user = userService.getUserByUsername(principal.getName());

        // ✅ Convert user notifications to DTOs
        List<NotificationDTO> notifications = notificationService.getUserNotifications(user)
                .stream()
                .map(NotificationMapper::toDTO)
                .collect(Collectors.toList());

        // ✅ Fetch unread count
        long unreadCount = notificationService.getUnreadCount(user);

        // ✅ Pass data to Thymeleaf
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("pageTitle", "My Notifications");
        model.addAttribute("username", user.getUsername());
        model.addAttribute("role", user.getRole().name());

        return "notifications"; // maps to /templates/notifications.html
    }

    // ------------------ MARK SINGLE NOTIFICATION AS READ ------------------
    @PostMapping("/mark-read/{id}")
    public String markAsRead(@PathVariable("id") Long notificationId, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        notificationService.markAsRead(notificationId);
        return "redirect:/dashboard/notifications";
    }

    // ------------------ MARK ALL NOTIFICATIONS AS READ ------------------
    @PostMapping("/mark-all-read")
    public String markAllAsRead(Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        User user = userService.getUserByUsername(principal.getName());
        List<Notification> notifications = notificationService.getUserNotifications(user);

        notifications.stream()
                .filter(n -> !n.isRead())
                .forEach(n -> notificationService.markAsRead(n.getId()));

        return "redirect:/dashboard/notifications";
    }

    // ------------------ FETCH UNREAD COUNT (AJAX ENDPOINT) ------------------
    @GetMapping("/unread-count")
    @ResponseBody
    public long getUnreadCount(Principal principal) {
        if (principal == null) {
            return 0L;
        }

        User user = userService.getUserByUsername(principal.getName());
        return notificationService.getUnreadCount(user);
    }
}