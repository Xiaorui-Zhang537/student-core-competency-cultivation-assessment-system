// Based on FileInfo DTO from backend
export interface FileInfo {
  id: number;
  fileName: string;
  fileType: string;
  fileSize: number;
  uploadTimestamp: string;
  url: string;
}
