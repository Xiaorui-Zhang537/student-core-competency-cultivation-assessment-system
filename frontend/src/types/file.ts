// Based on FileInfo DTO from backend
export interface FileInfo {
  id: string;
  fileName: string;
  fileType: string;
  fileSize: number;
  uploadTimestamp: string;
  url: string;
}
