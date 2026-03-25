package com.worldmap.recommendation.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RecommendationFeedbackRepository extends JpaRepository<RecommendationFeedback, Long> {

	long countBySurveyVersionAndEngineVersion(String surveyVersion, String engineVersion);

	@Query("""
		select
			f.surveyVersion as surveyVersion,
			f.engineVersion as engineVersion,
			count(f) as responseCount,
			avg(f.satisfactionScore) as averageSatisfaction,
			sum(case when f.satisfactionScore = 1 then 1 else 0 end) as score1Count,
			sum(case when f.satisfactionScore = 2 then 1 else 0 end) as score2Count,
			sum(case when f.satisfactionScore = 3 then 1 else 0 end) as score3Count,
			sum(case when f.satisfactionScore = 4 then 1 else 0 end) as score4Count,
			sum(case when f.satisfactionScore = 5 then 1 else 0 end) as score5Count,
			max(f.createdAt) as lastSubmittedAt
		from RecommendationFeedback f
		group by f.surveyVersion, f.engineVersion
		order by max(f.createdAt) desc, avg(f.satisfactionScore) desc
		""")
	List<RecommendationFeedbackVersionSummaryProjection> summarizeByVersion();
}
