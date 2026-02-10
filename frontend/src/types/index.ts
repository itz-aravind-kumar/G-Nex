export interface User {
  userId: string
  username: string
  email: string
  fullName?: string
}

export interface AuthResponse {
  token: string
  userId: string
  username: string
  email: string
  fullName: string
  type: string
}

export interface FileMetadata {
  id: string
  fileId: string
  fileName: string
  fileSize: number
  contentType: string
  ownerId: string
  uploadedAt: string
  updatedAt: string
  status: 'ACTIVE' | 'DELETED' | 'ARCHIVED'
  version: number
  checksum?: string
  minioKey?: string
}

export interface FolderMetadata {
  id: string
  folderId: string
  name: string
  parentId?: string
  path: string
  ownerId: string
  createdAt: string
  updatedAt: string
}

export interface ShareRequest {
  resourceType: 'FILE' | 'FOLDER'
  resourceId: string
  granteeId?: string
  granteeEmail?: string
  role: 'OWNER' | 'EDITOR' | 'VIEWER'
  expiresAt?: string
}

export interface ShareLink {
  id: string
  resourceType: 'FILE' | 'FOLDER'
  resourceId: string
  token: string
  role: 'OWNER' | 'EDITOR' | 'VIEWER'
  expiresAt?: string
  createdBy: string
  createdAt: string
}

export interface Permission {
  id: string
  resourceType: 'FILE' | 'FOLDER'
  resourceId: string
  granteeId?: string
  granteeEmail?: string
  role: 'OWNER' | 'EDITOR' | 'VIEWER'
  grantedBy: string
  grantedAt: string
  expiresAt?: string
}

export interface StorageStats {
  totalFiles: number
  totalSize: number
  usedSize: number
  quotaSize: number
  filesByType: Record<string, number>
}

export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
  timestamp: string
}

export interface UploadProgress {
  fileId: string
  fileName: string
  progress: number
  status: 'pending' | 'uploading' | 'completed' | 'error'
  error?: string
}
