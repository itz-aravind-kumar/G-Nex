import { apiClient } from './apiClient'
import { FileMetadata, ApiResponse } from '@/types'

export const fileService = {
  async uploadFile(
    file: File,
    userId: string,
    onProgress?: (progress: number) => void
  ): Promise<FileMetadata> {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('fileName', file.name)
    formData.append('userId', userId)

    const response = await apiClient.upload<ApiResponse<FileMetadata>>(
      '/api/v1/files/upload',
      formData,
      onProgress
    )
    return response.data
  },

  async downloadFile(fileId: string): Promise<Blob> {
    // Get userId from localStorage
    const userStr = localStorage.getItem('user')
    const user = userStr ? JSON.parse(userStr) : null
    const userId = user?.userId || user?.id || 'unknown'
    
    const response = await apiClient.getClient().get(`/api/v1/files/${fileId}/download`, {
      responseType: 'blob',
      headers: {
        'X-User-Id': userId
      }
    })
    return response.data
  },

  async deleteFile(fileId: string): Promise<void> {
    await apiClient.delete(`/api/v1/files/${fileId}`)
  },

  async getFileMetadata(fileId: string): Promise<FileMetadata> {
    const response = await apiClient.get<ApiResponse<FileMetadata>>(
      `/api/v1/metadata/${fileId}`
    )
    return response.data
  },

  async listUserFiles(userId: string): Promise<FileMetadata[]> {
    const response = await apiClient.get<ApiResponse<any>>(
      `/api/v1/metadata/user/${userId}`
    )
    // Handle paginated response - extract content array
    if (response.data && response.data.content && Array.isArray(response.data.content)) {
      return response.data.content
    }
    // If already an array, return as is
    if (Array.isArray(response.data)) {
      return response.data
    }
    // Fallback to empty array
    return []
  },

  async getUserStorageStats(userId: string) {
    try {
      const response = await apiClient.get<ApiResponse<any>>(
        `/api/v1/metadata/user/${userId}/storage-stats`
      )
      return response.data
    } catch (error) {
      // Endpoint doesn't exist yet, return mock data
      return { totalSize: 0, fileCount: 0 }
    }
  },

  async getThumbnail(fileId: string, size: 'SMALL' | 'GRID' | 'PREVIEW' = 'GRID'): Promise<string | null> {
    try {
      const response = await apiClient.get<ApiResponse<any>>(
        `/api/v1/thumbnails/${fileId}?size=${size}`
      )
      // Return the thumbnail URL if available
      if (response.data && response.data.url) {
        return response.data.url
      }
      return null
    } catch (error) {
      // Thumbnail not ready or failed
      return null
    }
  },

  async getThumbnailStatus(fileId: string): Promise<any> {
    try {
      const response = await apiClient.get<ApiResponse<any>>(
        `/api/v1/thumbnails/${fileId}/status`
      )
      return response.data
    } catch (error) {
      return null
    }
  },
}
