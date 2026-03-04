package com.closer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private int totalContacts;
    private int maxContacts;
    private double utilizationPercentage;
    private List<GroupSummary> groups;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupSummary {
        private int groupNumber;
        private String color;
        private String label;
        private int contactCount;
        private double percentage;
        private List<ContactResponse> contacts;

        public static String labelForGroup(int group) {
            return switch (group) {
                case 1 -> "Close (last 2 weeks)";
                case 2 -> "Regular (last month)";
                case 3 -> "Occasional (last 3 months)";
                case 4 -> "Distant (last 6 months)";
                default -> "Inactive (6+ months)";
            };
        }
    }
}
