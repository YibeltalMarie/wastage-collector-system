export interface DashboardStats {
  totalRequestsToday:    number;
  pendingRequests:       number;
  assignedRequests:      number;
  inProgressRequests:    number;
  completedToday:        number;
  failedToday:           number;
  totalCollectors:       number;
  availableCollectors:   number;
  onDutyCollectors:      number;
  unavailableCollectors: number;
}

export interface CollectorResponse {
  id:               string;
  fullName:         string;
  phoneNumber:      string;
  subCity:          string | null;
  availability:     'AVAILABLE' | 'UNAVAILABLE' | 'ON_DUTY';
  assignedSubCity:  string | null;
  vehicleType:      string | null;
  isActive:         boolean;
}
