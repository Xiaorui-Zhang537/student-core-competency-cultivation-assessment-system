import { api } from './config';

export interface ChapterPayload {
  courseId: number;
  title: string;
  description?: string;
  orderIndex?: number;
}

export const chapterApi = {
  listByCourse(courseId: string | number) {
    return api.get(`/chapters/course/${courseId}`);
  },
  create(data: ChapterPayload) {
    return api.post('/chapters', data);
  },
  update(id: string | number, data: Partial<ChapterPayload>) {
    return api.put(`/chapters/${id}`, data);
  },
  remove(id: string | number) {
    return api.delete(`/chapters/${id}`);
  }
};


