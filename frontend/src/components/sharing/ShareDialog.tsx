import { useState, useEffect } from 'react';
import { X, Copy, Link, Mail, Trash2, Check, Clock, Shield, Eye, Edit } from 'lucide-react';
import shareService, { PermissionDto, ShareLinkDto, ShareRequestDto, ShareLinkRequestDto } from '../../services/shareService';

interface ShareDialogProps {
  isOpen: boolean;
  onClose: () => void;
  resourceId: string;
  resourceType: 'FILE' | 'FOLDER';
  resourceName: string;
}

export default function ShareDialog({ isOpen, onClose, resourceId, resourceType, resourceName }: ShareDialogProps) {
  const [activeTab, setActiveTab] = useState<'people' | 'link'>('people');
  const [email, setEmail] = useState('');
  const [role, setRole] = useState<'VIEWER' | 'EDITOR'>('VIEWER');
  const [permissions, setPermissions] = useState<PermissionDto[]>([]);
  const [shareLinks, setShareLinks] = useState<ShareLinkDto[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [copiedLink, setCopiedLink] = useState<string | null>(null);

  // Link creation options
  const [linkRole, setLinkRole] = useState<'VIEWER' | 'EDITOR'>('VIEWER');
  const [linkExpiry, setLinkExpiry] = useState('');
  const [linkPassword, setLinkPassword] = useState('');
  const [linkMaxDownloads, setLinkMaxDownloads] = useState<number | ''>('');

  useEffect(() => {
    if (isOpen) {
      loadPermissions();
      loadShareLinks();
    }
  }, [isOpen, resourceId, resourceType]);

  const loadPermissions = async () => {
    try {
      const perms = await shareService.getResourcePermissions(resourceType, resourceId);
      setPermissions(perms);
    } catch (err) {
      console.error('Error loading permissions:', err);
    }
  };

  const loadShareLinks = async () => {
    try {
      const links = await shareService.getResourceShareLinks(resourceType, resourceId);
      setShareLinks(links);
    } catch (err) {
      console.error('Error loading share links:', err);
    }
  };

  const handleShareWithUser = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email) return;

    setLoading(true);
    setError('');
    setSuccess('');

    try {
      const request: ShareRequestDto = {
        resourceId,
        resourceType,
        granteeEmail: email,
        role,
        notifyUser: true
      };
      await shareService.shareResource(request);
      setSuccess(`Shared with ${email}`);
      setEmail('');
      loadPermissions();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to share');
    } finally {
      setLoading(false);
    }
  };

  const handleRevokePermission = async (permissionId: string) => {
    try {
      await shareService.revokePermission(permissionId);
      loadPermissions();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to revoke permission');
    }
  };

  const handleUpdateRole = async (permissionId: string, newRole: string) => {
    try {
      await shareService.updatePermissionRole(permissionId, newRole);
      loadPermissions();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to update role');
    }
  };

  const handleCreateLink = async () => {
    setLoading(true);
    setError('');

    try {
      const request: ShareLinkRequestDto = {
        resourceId,
        resourceType,
        role: linkRole,
        expiresAt: linkExpiry || undefined,
        password: linkPassword || undefined,
        maxDownloads: linkMaxDownloads ? Number(linkMaxDownloads) : undefined
      };
      await shareService.createShareLink(request);
      loadShareLinks();
      // Reset form
      setLinkPassword('');
      setLinkMaxDownloads('');
      setLinkExpiry('');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to create link');
    } finally {
      setLoading(false);
    }
  };

  const handleDeactivateLink = async (linkId: string) => {
    try {
      await shareService.deactivateShareLink(linkId);
      loadShareLinks();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to deactivate link');
    }
  };

  const copyToClipboard = (link: ShareLinkDto) => {
    const url = shareService.getShareLinkUrl(link.token);
    navigator.clipboard.writeText(url);
    setCopiedLink(link.linkId);
    setTimeout(() => setCopiedLink(null), 2000);
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg w-full max-w-lg mx-4 max-h-[90vh] overflow-hidden flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b">
          <h2 className="text-lg font-semibold">Share "{resourceName}"</h2>
          <button onClick={onClose} className="text-gray-500 hover:text-gray-700">
            <X size={20} />
          </button>
        </div>

        {/* Tabs */}
        <div className="flex border-b">
          <button
            className={`flex-1 py-3 px-4 text-sm font-medium flex items-center justify-center gap-2 ${
              activeTab === 'people' ? 'text-blue-600 border-b-2 border-blue-600' : 'text-gray-500'
            }`}
            onClick={() => setActiveTab('people')}
          >
            <Mail size={16} />
            Share with People
          </button>
          <button
            className={`flex-1 py-3 px-4 text-sm font-medium flex items-center justify-center gap-2 ${
              activeTab === 'link' ? 'text-blue-600 border-b-2 border-blue-600' : 'text-gray-500'
            }`}
            onClick={() => setActiveTab('link')}
          >
            <Link size={16} />
            Get Link
          </button>
        </div>

        {/* Content */}
        <div className="flex-1 overflow-y-auto p-4">
          {error && (
            <div className="mb-4 p-3 bg-red-50 text-red-700 rounded-lg text-sm">
              {error}
            </div>
          )}
          {success && (
            <div className="mb-4 p-3 bg-green-50 text-green-700 rounded-lg text-sm">
              {success}
            </div>
          )}

          {activeTab === 'people' ? (
            <div>
              {/* Add people form */}
              <form onSubmit={handleShareWithUser} className="mb-6">
                <div className="flex gap-2">
                  <input
                    type="email"
                    placeholder="Enter email address"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    className="flex-1 px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                  <select
                    value={role}
                    onChange={(e) => setRole(e.target.value as 'VIEWER' | 'EDITOR')}
                    className="px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500"
                  >
                    <option value="VIEWER">Viewer</option>
                    <option value="EDITOR">Editor</option>
                  </select>
                  <button
                    type="submit"
                    disabled={loading || !email}
                    className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
                  >
                    Share
                  </button>
                </div>
              </form>

              {/* Existing permissions */}
              <div>
                <h3 className="text-sm font-medium text-gray-700 mb-3">People with access</h3>
                {permissions.length === 0 ? (
                  <p className="text-sm text-gray-500">No one else has access</p>
                ) : (
                  <div className="space-y-2">
                    {permissions.map((perm) => (
                      <div key={perm.permissionId} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                        <div className="flex items-center gap-3">
                          <div className="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
                            <span className="text-blue-600 font-medium text-sm">
                              {perm.granteeEmail[0].toUpperCase()}
                            </span>
                          </div>
                          <div>
                            <p className="text-sm font-medium">{perm.granteeEmail}</p>
                            {perm.expiresAt && (
                              <p className="text-xs text-gray-500 flex items-center gap-1">
                                <Clock size={12} />
                                Expires {new Date(perm.expiresAt).toLocaleDateString()}
                              </p>
                            )}
                          </div>
                        </div>
                        <div className="flex items-center gap-2">
                          <select
                            value={perm.role}
                            onChange={(e) => handleUpdateRole(perm.permissionId, e.target.value)}
                            className="text-sm border rounded px-2 py-1"
                          >
                            <option value="VIEWER">Viewer</option>
                            <option value="EDITOR">Editor</option>
                          </select>
                          <button
                            onClick={() => handleRevokePermission(perm.permissionId)}
                            className="text-red-500 hover:text-red-700 p-1"
                          >
                            <Trash2 size={16} />
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          ) : (
            <div>
              {/* Create new link */}
              <div className="mb-6 p-4 bg-gray-50 rounded-lg">
                <h3 className="text-sm font-medium text-gray-700 mb-3">Create a new link</h3>
                <div className="space-y-3">
                  <div className="flex gap-2">
                    <div className="flex-1">
                      <label className="text-xs text-gray-500">Access Level</label>
                      <select
                        value={linkRole}
                        onChange={(e) => setLinkRole(e.target.value as 'VIEWER' | 'EDITOR')}
                        className="w-full px-3 py-2 border rounded-lg"
                      >
                        <option value="VIEWER">Can view</option>
                        <option value="EDITOR">Can edit</option>
                      </select>
                    </div>
                    <div className="flex-1">
                      <label className="text-xs text-gray-500">Expires</label>
                      <input
                        type="datetime-local"
                        value={linkExpiry}
                        onChange={(e) => setLinkExpiry(e.target.value)}
                        className="w-full px-3 py-2 border rounded-lg"
                      />
                    </div>
                  </div>
                  <div className="flex gap-2">
                    <div className="flex-1">
                      <label className="text-xs text-gray-500">Password (optional)</label>
                      <input
                        type="password"
                        value={linkPassword}
                        onChange={(e) => setLinkPassword(e.target.value)}
                        placeholder="Set a password"
                        className="w-full px-3 py-2 border rounded-lg"
                      />
                    </div>
                    <div className="flex-1">
                      <label className="text-xs text-gray-500">Max downloads</label>
                      <input
                        type="number"
                        value={linkMaxDownloads}
                        onChange={(e) => setLinkMaxDownloads(e.target.value ? Number(e.target.value) : '')}
                        placeholder="Unlimited"
                        min="1"
                        className="w-full px-3 py-2 border rounded-lg"
                      />
                    </div>
                  </div>
                  <button
                    onClick={handleCreateLink}
                    disabled={loading}
                    className="w-full py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
                  >
                    Create Link
                  </button>
                </div>
              </div>

              {/* Existing links */}
              <div>
                <h3 className="text-sm font-medium text-gray-700 mb-3">Active links</h3>
                {shareLinks.length === 0 ? (
                  <p className="text-sm text-gray-500">No active links</p>
                ) : (
                  <div className="space-y-2">
                    {shareLinks.map((link) => (
                      <div key={link.linkId} className="p-3 bg-gray-50 rounded-lg">
                        <div className="flex items-center justify-between mb-2">
                          <div className="flex items-center gap-2">
                            {link.role === 'VIEWER' ? (
                              <Eye size={16} className="text-gray-500" />
                            ) : (
                              <Edit size={16} className="text-gray-500" />
                            )}
                            <span className="text-sm font-medium">{link.role}</span>
                            {link.isPasswordProtected && (
                              <Shield size={14} className="text-yellow-600" />
                            )}
                          </div>
                          <div className="flex items-center gap-2">
                            <button
                              onClick={() => copyToClipboard(link)}
                              className="text-blue-600 hover:text-blue-800 p-1"
                            >
                              {copiedLink === link.linkId ? (
                                <Check size={16} className="text-green-600" />
                              ) : (
                                <Copy size={16} />
                              )}
                            </button>
                            <button
                              onClick={() => handleDeactivateLink(link.linkId)}
                              className="text-red-500 hover:text-red-700 p-1"
                            >
                              <Trash2 size={16} />
                            </button>
                          </div>
                        </div>
                        <div className="text-xs text-gray-500 space-y-1">
                          <p>Created: {new Date(link.createdAt).toLocaleString()}</p>
                          {link.expiresAt && (
                            <p>Expires: {new Date(link.expiresAt).toLocaleString()}</p>
                          )}
                          <p>
                            {link.accessCount} views
                            {link.maxDownloads && ` • ${link.downloadCount}/${link.maxDownloads} downloads`}
                          </p>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
