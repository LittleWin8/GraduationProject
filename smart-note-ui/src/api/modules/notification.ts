import http from "@/api";

export interface NotificationDTO {
  title: string;
  content: string;
  type: number;
  userIds?: number[];
  noteId?: number;
}

export const sendNotification = (data: NotificationDTO) => {
  return http.post(`/admin/notifications/send`, data);
};
