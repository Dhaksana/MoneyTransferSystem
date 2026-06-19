package com.bd.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bd.dto.RewardHistoryDTO;
import com.bd.dto.RewardSummaryDTO;
import com.bd.service.CurrentUserService;
import com.bd.service.RewardService;

@RestController
@RequestMapping("/api/v1/rewards")
public class RewardController {
    private final RewardService rewards;
    private final CurrentUserService currentUser;

    public RewardController(RewardService rewards, CurrentUserService currentUser) {
        this.rewards = rewards;
        this.currentUser = currentUser;
    }

    @GetMapping("/summary")
    public RewardSummaryDTO summary() {
        return rewards.summary(currentUser.username());
    }

    @GetMapping("/history")
    public List<RewardHistoryDTO> history() {
        return rewards.history(currentUser.username());
    }

    @GetMapping("/admin/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public RewardSummaryDTO adminStatistics() {
        return new RewardSummaryDTO(0, (int) rewards.totalDistributed());
    }
}
