export type NotificationType =
  | 'REQUEST_SUBMITTED'
  | 'REQUEST_ASSIGNED'
  | 'REQUEST_IN_PROGRESS'
  | 'REQUEST_COMPLETED'
  | 'REQUEST_FAILED'
  | 'REQUEST_CANCELLED'
  | 'NEW_ASSIGNMENT'
  | 'ASSIGNMENT_REMOVED'
  | 'REASSIGNED'
  | 'NEW_REQUEST';

export interface NotificationResponse {
  id:        string;
  type:      NotificationType;
  title:     string;
  message:   string;
  isRead:    boolean;
  requestId: string | null;
  createdAt: string;
}
