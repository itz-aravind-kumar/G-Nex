import { apiClient } from './apiClient';

export interface ShareRequestDto {
  resourceId: string;
  resourceType: 'FILE' | 'FOLDER';
  granteeEmail: string;
  role: 'VIEWER' | 'EDITOR';
  expiresAt?: string;
  notifyUser?: boolean;
  message?: string;
}

export interface ShareLinkRequestDto {
  resourceId: string;
  resourceType: 'FILE' | 'FOLDER';
  role?: 'VIEWER' | 'EDITOR';
  expiresAt?: string;
  password?: string;
  maxDownloads?: number;
}

export interface PermissionDto {
  permissionId: string;
  resourceType: string;
  resourceId: string;
  resourceName: string;
  granteeId?: string;
  granteeEmail: string;
  granteeName?: string;
  role: string;
  grantedBy: string;
  grantedByEmail?: string;
  grantedAt: string;
  expiresAt?: string;
  isInherited: boolean;
}

export interface ShareLinkDto {
  linkId: string;
  token: string;
  url: string;
  resourceType: string;
  resourceId: string;
  resourceName: string;
  role: string;
  createdBy: string;
  createdAt: string;
  expiresAt?: string;
  isPasswordProtected: boolean;
  maxDownloads?: number;
  downloadCount: number;
  isActive: boolean;
  accessCount: number;
  lastAccessedAt?: string;
}

export const shareService = {
  /**
   * Share a file or folder with a user
   */
  async shareResource(request: ShareRequestDto): Promise<PermissionDto> {
    const response = await apiClient.getClient().post('/api/v1/share', request);
    return response.data.data;
  },

  /**
   * Revoke a permission
   */
  async revokePermission(permissionId: string): Promise<void> {
    await apiClient.getClient().delete(`/api/v1/share/permission/${permissionId}`);
  },

  /**
   * Get permissions for a resource
   */
  async getResourcePermissions(resourceType: string, resourceId: string): Promise<PermissionDto[]> {
    const response = await apiClient.getClient().get(`/api/v1/share/${resourceType}/${resourceId}/permissions`);
    return response.data.data;
  },

  /**
   * Get files shared with current user
   */
  async getFilesSharedWithMe(): Promise<any[]> {
    const response = await apiClient.getClient().get('/api/v1/share/shared-with-me/files');
    return response.data.data;
  },

  /**
   * Get folders shared with current user
   */
  async getFoldersSharedWithMe(): Promise<any[]> {
    const response = await apiClient.getClient().get('/api/v1/share/shared-with-me/folders');
    return response.data.data;
  },

  /**
   * Check if user can access a resource
   */
  async checkAccess(resourceType: string, resourceId: string): Promise<boolean> {
    const response = await apiClient.getClient().get(`/api/v1/share/${resourceType}/${resourceId}/access`);
    return response.data.data;
  },

  /**
   * Update permission role
   */
  async updatePermissionRole(permissionId: string, role: string): Promise<PermissionDto> {
    const response = await apiClient.getClient().patch(`/api/v1/share/permission/${permissionId}?role=${role}`);
    return response.data.data;
  },

  /**
   * Create a share link
   */
  async createShareLink(request: ShareLinkRequestDto): Promise<ShareLinkDto> {
    const response = await apiClient.getClient().post('/api/v1/share/link', request);
    return response.data.data;
  },

  /**
   * Get a share link by token
   */
  async getShareLinkByToken(token: string, password?: string): Promise<ShareLinkDto> {
    const params = password ? `?password=${encodeURIComponent(password)}` : '';
    const response = await apiClient.getClient().get(`/api/v1/share/link/${token}${params}`);
    return response.data.data;
  },

  /**
   * Get share links for a resource
   */
  async getResourceShareLinks(resourceType: string, resourceId: string): Promise<ShareLinkDto[]> {
    const response = await apiClient.getClient().get(`/api/v1/share/${resourceType}/${resourceId}/links`);
    return response.data.data;
  },

  /**
   * Deactivate a share link
   */
  async deactivateShareLink(linkId: string): Promise<void> {
    await apiClient.getClient().delete(`/api/v1/share/link/${linkId}`);
  },

  /**
   * Generate full share link URL
   */
  getShareLinkUrl(token: string): string {
    return `${window.location.origin}/share/${token}`;
  }
};

export default shareService;
