export type RequestStatus =
  | 'PENDING'
  | 'ASSIGNED'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'FAILED';

export interface PickupRequestResponse {
  id:            string;
  status:        RequestStatus;
  subCity:       string;
  kebele:        string | null;
  address:       string;
  latitude:      number | null;
  longitude:     number | null;
  preferredDate: string;
  notes:         string | null;
  collectorName: string | null;
  failureReason: string | null;
  assignedAt:    string | null;
  startedAt:     string | null;
  completedAt:   string | null;
  createdAt:     string;
}

export interface SubmitRequestPayload {
  subCity:       string;
  kebele?:       string;
  address:       string;
  latitude?:     number;
  longitude?:    number;
  preferredDate: string;
  notes?:        string;
}

export interface StatusHistoryEntry {
  oldStatus: RequestStatus | null;
  newStatus: RequestStatus;
  changedBy: string;
  note:      string | null;
  changedAt: string;
}
