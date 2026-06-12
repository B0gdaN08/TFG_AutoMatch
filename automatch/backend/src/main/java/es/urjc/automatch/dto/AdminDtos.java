package es.urjc.automatch.dto;

import jakarta.validation.constraints.NotBlank;

public class AdminDtos {

    public record UserSummary(Long id, String email, String name, String role) {
    }

    public record UpdateRoleRequest(@NotBlank String role) {
    }

    public record StatsResponse(long catalogCars, long userListings, long pendingListings,
                                  long approvedListings, long rejectedListings,
                                  long totalUsers, long totalAdmins, long totalFavorites) {
    }
}
