// Generic paginated API response — matches Spring Boot's Page<T>
export interface PagedResponse<T> {
  content:       T[];
  totalElements: number;
  totalPages:    number;
  currentPage:   number;
  size:          number;
}

// Standard error response from GlobalExceptionHandler
export interface ApiError {
  status:    number;
  message:   string;
  timestamp: string;
}

// Validation error response (has per-field errors map)
export interface ValidationError extends ApiError {
  errors: Record<string, string>;
}
