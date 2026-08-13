package com.healthcare.platform.analytics;

import com.healthcare.platform.dto.DiagnosisFrequencyDto;
import com.healthcare.platform.dto.EncounterTrendPointDto;
import com.healthcare.platform.dto.FacilityActivityDto;
import com.healthcare.platform.dto.LabTrendPointDto;
import com.healthcare.platform.dto.OverviewCountsDto;
import com.healthcare.platform.dto.ProviderWorkloadDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Analytics rollups are hand-written native SQL rather than JPQL/derived
 * queries: these are aggregate, read-heavy queries where SQL is clearer than
 * an object-graph traversal, and native SQL lets us control exactly which
 * indexes/partitions get used — see docs/performance.md for the EXPLAIN
 * ANALYZE plans behind each of these. provider_name/facility_name live
 * directly on analytics.encounters (no separate dimension tables in this
 * academic scope), so these rollups are plain GROUP BY queries, no joins.
 */
@Service
public class AnalyticsQueryService {

    private final JdbcTemplate jdbc;

    public AnalyticsQueryService(JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcTemplate;
    }

    public List<EncounterTrendPointDto> encounterTrend(int months) {
        return jdbc.query("""
                SELECT to_char(date_trunc('month', encounter_date), 'YYYY-MM') AS period, COUNT(*) AS cnt
                FROM analytics.encounters
                WHERE encounter_date >= (current_date - (? || ' months')::interval)
                GROUP BY 1
                ORDER BY 1
                """, (rs, i) -> new EncounterTrendPointDto(rs.getString("period"), rs.getLong("cnt")), months);
    }

    public List<DiagnosisFrequencyDto> diagnosisFrequency(int limit) {
        return jdbc.query("""
                SELECT diagnosis_code, MAX(diagnosis_description) AS description, COUNT(*) AS cnt
                FROM analytics.diagnoses
                GROUP BY diagnosis_code
                ORDER BY cnt DESC
                LIMIT ?
                """, (rs, i) -> new DiagnosisFrequencyDto(rs.getString("diagnosis_code"), rs.getString("description"), rs.getLong("cnt")), limit);
    }

    public List<ProviderWorkloadDto> providerWorkload(int limit) {
        return jdbc.query("""
                SELECT provider_name, MAX(specialty) AS specialty, COUNT(*) AS cnt
                FROM analytics.encounters
                WHERE provider_name IS NOT NULL
                GROUP BY provider_name
                ORDER BY cnt DESC
                LIMIT ?
                """, (rs, i) -> new ProviderWorkloadDto(rs.getString("provider_name"), rs.getString("specialty"), rs.getLong("cnt")), limit);
    }

    public List<FacilityActivityDto> facilityActivity() {
        return jdbc.query("""
                SELECT facility_name, COUNT(*) AS cnt
                FROM analytics.encounters
                WHERE facility_name IS NOT NULL
                GROUP BY facility_name
                ORDER BY cnt DESC
                """, (rs, i) -> new FacilityActivityDto(rs.getString("facility_name"), rs.getLong("cnt")));
    }

    public List<LabTrendPointDto> labTrend(String testName, int months) {
        return jdbc.query("""
                SELECT test_name, to_char(date_trunc('month', collected_at), 'YYYY-MM') AS period, COUNT(*) AS cnt
                FROM analytics.labs
                WHERE (? IS NULL OR test_name = ?)
                  AND collected_at >= now() - (? || ' months')::interval
                GROUP BY test_name, period
                ORDER BY period, test_name
                """, (rs, i) -> new LabTrendPointDto(rs.getString("test_name"), rs.getString("period"), rs.getLong("cnt")),
                testName, testName, months);
    }

    public OverviewCountsDto overviewCounts() {
        return jdbc.queryForObject("""
                SELECT
                    (SELECT COUNT(*) FROM analytics.patients) AS patients,
                    (SELECT COUNT(*) FROM analytics.encounters) AS encounters,
                    (SELECT COUNT(*) FROM analytics.diagnoses) AS diagnoses,
                    (SELECT COUNT(*) FROM analytics.procedures) AS procedures,
                    (SELECT COUNT(*) FROM analytics.labs) AS labs
                """, (rs, i) -> new OverviewCountsDto(
                rs.getLong("patients"), rs.getLong("encounters"), rs.getLong("diagnoses"),
                rs.getLong("procedures"), rs.getLong("labs")));
    }

    public Map<String, Object> averageLengthOfStayByDepartment() {
        return jdbc.query("""
                SELECT department, ROUND(AVG(length_of_stay_hours)::numeric, 2) AS avg_hours
                FROM analytics.encounters
                WHERE length_of_stay_hours IS NOT NULL AND department IS NOT NULL
                GROUP BY department
                ORDER BY avg_hours DESC
                """, rs -> {
            java.util.LinkedHashMap<String, Object> result = new java.util.LinkedHashMap<>();
            while (rs.next()) {
                BigDecimal avg = rs.getBigDecimal("avg_hours");
                result.put(rs.getString("department"), avg);
            }
            return result;
        });
    }
}