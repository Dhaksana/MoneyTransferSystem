package com.bd.controller;

import java.util.List;

import com.bd.dto.RewardHistoryDTO;
import com.bd.dto.RewardSummaryDTO;
import com.bd.service.RewardService;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(
        origins = "http://localhost:4200",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},
        allowCredentials = "true",
        maxAge = 3600
)
@RequestMapping("/api/v1/rewards")
public class RewardController {

    private final RewardService rewardService;

    public RewardController(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    @GetMapping("/account/{accountId}/history")
    public List<RewardHistoryDTO> getRewardHistory(@PathVariable String accountId) {
        return rewardService.getRewardHistory(accountId);
    }

    @GetMapping("/account/{accountId}/summary")
    public RewardSummaryDTO getRewardSummary(@PathVariable String accountId) {
        return rewardService.getRewardSummary(accountId);
    }
}
