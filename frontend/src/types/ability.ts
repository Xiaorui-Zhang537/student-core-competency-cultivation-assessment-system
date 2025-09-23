// Based on AbilityDimension.java
export interface AbilityDimension {
  id: number;
  name: string;
  code?: string;
  description: string;
}

// Based on data returned from /api/ability/student/dashboard
export interface AbilityDashboardData {
  radarChartData: {
    labels: string[];
    scores: number[];
  };
  overallScore: number;
  strongestDimension: string;
  weakestDimension: string;
}

// Based on data returned from /api/ability/student/trends
export interface AbilityTrendData {
    dates: string[];
    dimensions: {
        name: string;
        scores: number[];
    }[];
}

// Based on data returned from /api/ability/student/recommendations
export interface AbilityRecommendation {
    id: number;
    dimension: string;
    recommendationText: string;
    relatedCourses: {
        id: number;
        title: string;
    }[];
}
